package pokemon;

import battle.Battle;
import battle.BattleAttributes;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.attack.MultiTurnMove;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.AbsorbDamageEffect;
import battle.effect.generic.EffectInterfaces.BracingEffect;
import battle.effect.generic.EffectInterfaces.ChangeMoveListEffect;
import battle.effect.generic.EffectInterfaces.ChangeTypeEffect;
import battle.effect.generic.EffectInterfaces.DamageTakenEffect;
import battle.effect.generic.EffectInterfaces.DifferentStatEffect;
import battle.effect.generic.EffectInterfaces.GroundedEffect;
import battle.effect.generic.EffectInterfaces.HalfWeightEffect;
import battle.effect.generic.EffectInterfaces.ItemSwapperEffect;
import battle.effect.generic.EffectInterfaces.LevitationEffect;
import battle.effect.generic.EffectInterfaces.MurderEffect;
import battle.effect.generic.EffectInterfaces.NameChanger;
import battle.effect.generic.EffectInterfaces.OpponentTrappingEffect;
import battle.effect.generic.EffectInterfaces.StallingEffect;
import battle.effect.generic.EffectInterfaces.SwapOpponentEffect;
import battle.effect.generic.EffectInterfaces.TrappingEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import battle.effect.holder.AbilityHolder;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import draw.DrawUtils;
import item.Item;
import item.ItemNamesies;
import item.berry.Berry;
import item.berry.GainableEffectBerry;
import item.hold.EVItem;
import item.hold.HoldItem;
import main.Game;
import main.Global;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pattern.PokemonMatcher;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.breeding.Breeding;
import pokemon.evolution.BaseEvolution;
import pokemon.evolution.EvolutionMethod;
import sound.SoundTitle;
import trainer.Team;
import trainer.Trainer;
import trainer.WildPokemon;
import trainer.player.medal.MedalTheme;
import type.Type;
import util.RandomUtils;
import util.StringUtils;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class ActivePokemon implements Serializable {
    private static final long serialVersionUID = 1L;

	public static final int MAX_LEVEL = 100;
	public static final int MAX_NAME_LENGTH = 12;

	private static final String TINY_EGG_IMAGE_NAME = "egg-small";
	private static final String BASE_EGG_IMAGE_NAME = "egg";
	public static final String SPRITE_EGG_IMAGE_NAME = "EggSprite";

	private static final String[][] characteristics =
		{{"Loves to eat",            "Proud of its power",      "Sturdy body",            "Highly curious",        "Strong willed",     "Likes to run"},
		 {"Takes plenty of siestas", "Likes to thrash about",   "Capable of taking hits", "Mischievous",           "Somewhat vain",     "Alert to sounds"},
		 {"Nods off a lot",          "A little quick tempered", "Highly persistent",      "Thoroughly cunning",    "Strongly defiant",  "Impetuous and silly"},
		 {"Scatters things often",   "Likes to fight",          "Good endurance",         "Often lost in thought", "Hates to lose",     "Somewhat of a clown"},
		 {"Likes to relax",          "Quick tempered",          "Good perseverance",      "Very finicky",          "Somewhat stubborn", "Quick to flee"}};
	
	private PokemonNamesies pokemon;
	private String nickname;
	private int[] stats;
	private int[] IVs;
	private List<Move> moves;
	private int hp;
	private int level;
	private boolean isPlayer;
	private Status status;
	private int totalEXP;
	private int[] EVs;
	private HoldItem heldItem;
	private Ability ability;
	private Gender gender;
	private Nature nature;
	private String characteristic;
	private boolean shiny;
	private BattleAttributes attributes;
	private boolean isEgg;
	private int eggSteps;

	// General constructor for an active Pokemon (isPlayer is true if it is the player's pokemon and false if it is wild, enemy trainer, etc.)
	public ActivePokemon(PokemonNamesies pokemonNamesies, int level, boolean isWild, boolean isPlayer) {
		this.pokemon = pokemonNamesies;
		PokemonInfo pokemon = this.getPokemonInfo();

		this.nickname = this.pokemon.getName();
		this.level = level;

		this.nature = new Nature();
		this.EVs = new int[Stat.NUM_STATS];
		this.stats = new int[Stat.NUM_STATS];
		this.setIVs();

		this.isPlayer = isPlayer;
		this.attributes = new BattleAttributes(this);
		this.shiny = (isPlayer || isWild) && RandomUtils.chanceTest(1, 8192);

		this.setMoves();
		this.setGender(Gender.getGender(pokemon.getMaleRatio()));
		this.setAbility(Ability.assign(pokemon));
		
		this.heldItem = (HoldItem)ItemNamesies.NO_ITEM.getItem();
		
		this.isEgg = false;
		this.eggSteps = 0;

		this.totalEXP = pokemon.getGrowthRate().getEXP(this.level);
		this.totalEXP += RandomUtils.getRandomInt(expToNextLevel());

		this.fullyHeal();
	}
	
	// Constructor for Eggs
	public ActivePokemon(PokemonNamesies pokemonNamesies) {
		this(pokemonNamesies, 1, false, true);

		this.isEgg = true;
		this.eggSteps = this.getPokemonInfo().getEggSteps();
		this.nickname = "Egg";
	}
	
	public ActivePokemon(ActivePokemon daddy, ActivePokemon mommy, PokemonNamesies pokemonNamesies) {
		this(pokemonNamesies);
		
		moves = Breeding.getBabyMoves(daddy, mommy, pokemonNamesies);
		this.setNature(Breeding.getBabyNature(daddy, mommy));
		this.setIVs(Breeding.getBabyIVs(daddy, mommy));
	}
	
	/*
	 * Format: Name Level Parameters
	 * Possible parameters:
	 * 		Moves: Move1, Move2, Move3, Move4*
	 * 		Shiny
	 * 		Egg
	 * 		Item: item name*
	 */
	// Constructor for triggers
	public static ActivePokemon createActivePokemon(PokemonMatcher pokemonMatcher, boolean user) {
		
		// Random Starter Egg
		if (pokemonMatcher.isStarterEgg()) {
			if (!user) {
				Global.error("Trainers cannot have eggs.");
			}

			return new ActivePokemon(PokemonInfo.getRandomStarterPokemon());
		}

		final PokemonNamesies namesies = pokemonMatcher.getNamesies();

		ActivePokemon pokemon;
		if (pokemonMatcher.isEgg()) {
			if(!user) {
				Global.error("Trainers cannot have eggs.");
			}

			pokemon = new ActivePokemon(namesies);
		}
		else {
			pokemon = new ActivePokemon(namesies, pokemonMatcher.getLevel(), false, user);
			String nickname = pokemonMatcher.getNickname();
			if (!StringUtils.isNullOrEmpty(nickname)) {
				pokemon.setNickname(nickname);
			}
		}

		if (pokemonMatcher.isShiny()) {
			pokemon.setShiny();
		}

		if (pokemonMatcher.hasMoves()) {
			pokemon.setMoves(pokemonMatcher.getMoves());
		}

		if (pokemonMatcher.hasHoldItem()) {
			pokemon.giveItem(pokemonMatcher.getHoldItem());
		}

		return pokemon;
	}
	
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public void setAbility(AbilityNamesies ability) {
		this.ability = ability.getNewAbility();
	}

	public void setNature(Nature nature) {
		this.nature = nature;
		this.setStats();
	}
	
	// Does not include shiny -- this is for the small party tiles
	public String getTinyImageName() {
		return this.isEgg ? TINY_EGG_IMAGE_NAME : this.getPokemonInfo().getTinyImageName();
	}

	// Does not include shiny -- this is for the small party tiles
	public String getBaseImageName() {
		return this.isEgg ? BASE_EGG_IMAGE_NAME : this.getPokemonInfo().getBaseImageName();
	}

	public String getImageName() {
		return this.getImageName(true);
	}

	// Larger image index
	public String getImageName(boolean front) {
		return this.isEgg() ? SPRITE_EGG_IMAGE_NAME : this.getPokemonInfo().getImageName(this.isShiny(), front);
	}
	
	public boolean isEgg() {
		return isEgg;
	}
	
	public boolean hatch() {
		if (!isEgg()) {
			Global.error("Only eggs can hatch!");
		}
		
		eggSteps--;
		
		if (eggSteps > 0) {
			return false;
		}
		
		this.isEgg = false;
		this.nickname = pokemon.getName();
		
		return true;
	}
	
	public String getEggMessage() {
		if (!isEgg()) {
			Global.error("Only Eggs can have egg messages.");
		}
		
		if (eggSteps > 10*255) {
			return "Wonder what's inside? It needs more time though.";
		}
		else if (eggSteps > 5*255) {
			return "It moves around inside sometimes. It must be close to hatching.";
		}
		else {
			return "It's making sounds inside! It's going to hatch soon!";
		}
	}

	// Returns the moves this Pokemon could have learned up to its current level
	public List<AttackNamesies> getLearnableMoves() {
		List<AttackNamesies> moves = new ArrayList<>();
		List<Entry<Integer, AttackNamesies>> levelUpMoves = this.getPokemonInfo().getLevelUpMoves();
		for (Entry<Integer, AttackNamesies> entry : levelUpMoves) {
			if (entry.getKey() > level) {
				break;
			}

			if (!this.hasActualMove(entry.getValue())) {
				moves.add(entry.getValue());
			}
		}

		return moves;
	}
	
	private void setMoves() {
		moves = new ArrayList<>();
		List<Entry<Integer, AttackNamesies>> levelUpMoves = this.getPokemonInfo().getLevelUpMoves();
		for (Entry<Integer, AttackNamesies> entry : levelUpMoves) {
			int levelLearned = entry.getKey();
			AttackNamesies attackNamesies = entry.getValue();
			if (levelLearned > level || this.hasActualMove(attackNamesies)) {
				continue;
			}

			moves.add(new Move(attackNamesies.getAttack()));

			// This can be an 'if' statement, but just to be safe...
			while (moves.size() > Move.MAX_MOVES) {
				moves.remove(0);
			}
		}
	}
	
	public void setMoves(List<Move> list) {
		moves = list;
	}
	
	public void setShiny() {
		shiny = true;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	// Random value between 0 and 31
	private void setIVs() {
		int[] IVs = new int[Stat.NUM_STATS];
		for (int i = 0; i < IVs.length; i++) {
			IVs[i] = Stat.getRandomIv();
		}

		this.setIVs(IVs);
	}

	// Random value between 0 and 31
	private void setIVs(int[] IVs) {
		this.IVs = IVs;

		int maxIndex = 0;
		for (int i = 0; i < this.IVs.length; i++) {
			if (this.IVs[i] > this.IVs[maxIndex]) {
				maxIndex = i;
			}
		}

		this.characteristic = characteristics[this.IVs[maxIndex]%5][maxIndex];
		this.setStats();
	}
	
	private void setStats() {
		int prevHP = stats[Stat.HP.index()];
		PokemonInfo pokemon = this.getPokemonInfo();
		
		stats = new int[Stat.NUM_STATS];
		for (int i = 0; i < stats.length; i++) {
			stats[i] = Stat.getStat(i, level, pokemon.getStat(i), IVs[i], EVs[i], nature.getNatureVal(i));
		}

		setHP(hp + stats[Stat.HP.index()] - prevHP);
	}
	
	public Type computeHiddenPowerType() {
		return Type.getHiddenType(((
				IVs[Stat.HP.index()]%2 +
				2*(IVs[Stat.ATTACK.index()]%2) +
				4*(IVs[Stat.DEFENSE.index()]%2) +
				8*(IVs[Stat.SPEED.index()]%2) +
				16*(IVs[Stat.SP_ATTACK.index()]%2) +
				32*(IVs[Stat.SP_DEFENSE.index()]%2)
			)*15)/63);
	}
	
	public String getCharacteristic() {
		return characteristic;
	}
	
	public int[] getStats() {
		return stats;
	}
	
	public int[] getIVs() {
		return IVs;
	}
	
	public int[] getEVs() {
		return EVs;
	}
	
	public int getIV(int index) {
		return IVs[index];
	}
	
	public int getEV(int index) {
		return EVs[index];
	}
	
	public Nature getNature() {
		return nature;
	}
	
	public Ability getActualAbility() {
		return ability;
	}
	
	public Ability getAbility() {

		// Check if the Pokemon has had its ability changed during the battle
		PokemonEffect effect = getEffect(EffectNamesies.CHANGE_ABILITY);
		if (effect != null) {
			return ((AbilityHolder)effect).getAbility();
		}
		
		return this.ability;
	}

	public int getStage(Stat stat) {
		return attributes.getStage(stat);
	}
	
	public Move getMove(Battle b, int index) {
		return getMoves(b).get(index);
	}
	
	public int getMaxHP() {
		return stats[Stat.HP.index()];
	}
	
	public int getStat(Battle b, Stat s) {
		Integer stat = DifferentStatEffect.getStat(b, this, s);
		if (stat != null) {
			return stat;
		}
		
		return stats[s.index()];
	}
	
	public List<Move> getMoves(Battle b) {
		List<Move> changedMoveList = ChangeMoveListEffect.getMoveList(b, this, this.moves);
		if (changedMoveList != null) {
			return changedMoveList;
		}

		return this.moves;
	}
	
	public List<Move> getActualMoves() {
		return moves;
	}
	
	public int getTotalEXP() {
		return totalEXP;
	}
	
	public int expToNextLevel() {
		if (level == MAX_LEVEL) {
			return 0;	
		}
		
		return this.getPokemonInfo().getGrowthRate().getEXP(level + 1) - totalEXP;
	}
	
	public float expRatio() {
		if (level == MAX_LEVEL) {
			return 0;
		}

		PokemonInfo pokemon = this.getPokemonInfo();

		int totalNextLevel = pokemon.getGrowthRate().getEXP(level + 1);
		int totalCurrentLevel = pokemon.getGrowthRate().getEXP(level);
		
		int currentToNextLevel = expToNextLevel();
		int totalToNextLevel = totalNextLevel - totalCurrentLevel;
		
		return 1.0f - (float)currentToNextLevel/totalToNextLevel;
	}
	
	public void gainEXP(Battle b, int gain, ActivePokemon dead) {
		boolean front = b.getPlayer().front() == this;
		
		// Add EXP
		totalEXP += gain;
		Messages.add(getActualName() + " gained " + gain + " EXP points!");
		if (front) {
			Messages.add(new MessageUpdate().withExpGain(b, this, Math.min(1, expRatio()), false));
		}

		// Add EVs
		Item item = getHeldItem(b);
		int[] vals = dead.getPokemonInfo().getGivenEVs();
		if (item instanceof EVItem) {
			vals = ((EVItem)item).getEVs(vals);
		}
		
		addEVs(vals);
		
		// Level up if applicable
		while (totalEXP >= this.getPokemonInfo().getGrowthRate().getEXP(level + 1)) {
			levelUp(b);
		}
	}

	public boolean levelUp(Battle b) {
		if (level == MAX_LEVEL) {
			return false;
		}

		boolean inBattle = b != null;
		boolean front = inBattle && b.getPlayer().front() == this;
		
		// Grow to the next level
		level++;
		Messages.add(new MessageUpdate(getActualName() + " grew to level " + level + "!").withSoundEffect(SoundTitle.LEVEL_UP));

		if (front) {
			Messages.add(new MessageUpdate().withExpGain(b, this, Math.min(1, expRatio()), true));
		}
		
		// Change stats -- keep track of the gains
		int[] prevStats = stats.clone();
		int[] gain = new int[Stat.NUM_STATS];
		setStats();
		for (int i = 0; i < Stat.NUM_STATS; i++) {
			gain[i] = stats[i] - prevStats[i];
		}

		if (front) {
			Messages.add(new MessageUpdate().updatePokemon(b, this));
		}

		Messages.add(new MessageUpdate().withStatGains(gain, stats));

		// Learn new moves
		this.getPokemonInfo().getMoves(level).forEach(attackNamesies -> learnMove(attackNamesies, inBattle));
		
		// Maybe you'll evolve?!
		// Can only evolve outside of battle
		if (!inBattle) {
			checkEvolution(EvolutionMethod.LEVEL);
		}
		
		return true;
	}

	public boolean checkEvolution() {
		return this.checkEvolution(EvolutionMethod.LEVEL) || this.checkEvolution(EvolutionMethod.MOVE);
	}

	public boolean checkEvolution(ItemNamesies itemNamesies) {
		return checkEvolution(EvolutionMethod.ITEM, itemNamesies);
	}

	private boolean checkEvolution(EvolutionMethod method) {
		return checkEvolution(method, null);
	}

	private boolean checkEvolution(EvolutionMethod method, ItemNamesies itemNamesies) {
		if (getActualHeldItem().namesies() == ItemNamesies.EVERSTONE) {
			return false;
		}

		BaseEvolution evolution = this.getPokemonInfo().getEvolution().getEvolution(method, this, itemNamesies);
		if (evolution != null) {
			Game.getPlayer().getEvolutionInfo().setEvolution(this, evolution);
			return true;
		}

		return false;
	}

	// Returns stat gains
	public int[] evolve(BaseEvolution evolution) {
		Game.getPlayer().getMedalCase().increase(MedalTheme.POKEMON_EVOLVED);

		boolean sameName = nickname.equals(pokemon.getName());
		PokemonInfo evolutionInfo = evolution.getEvolution();

		this.setAbility(Ability.evolutionAssign(this, evolutionInfo));
		pokemon = evolutionInfo.namesies();

		// Set name if it was not given a nickname
		if (sameName) {
			nickname = pokemon.getName();
		}
		
		// Change stats
		int[] prevStats = stats.clone();
		setStats();

		int[] gain = new int[Stat.NUM_STATS];
		for (int i = 0; i < Stat.NUM_STATS; i++) {
			gain[i] = stats[i] - prevStats[i];
		}

		// Learn new moves
		List<AttackNamesies> levelMoves = this.getPokemonInfo().getMoves(PokemonInfo.EVOLUTION_LEVEL_LEARNED);
		levelMoves.forEach(attack -> learnMove(attack, false));

		levelMoves = this.getPokemonInfo().getMoves(level);
		levelMoves.forEach(attack -> learnMove(attack, false));

		return gain;
	}
	
	private void learnMove(AttackNamesies attackName, boolean inBattle) {
		// Don't want to learn a move you already know!
		if (hasActualMove(attackName)) {
			return;
		}
		
		Move move = new Move(attackName.getAttack());
		if (moves.size() < Move.MAX_MOVES) {
			addMove(move, moves.size() - 1, inBattle);
		} else {
			// Need a non-empty message so that it doesn't get absorbed
			Messages.add(new MessageUpdate(" ").withLearnMove(this, move));
		}
	}

	public void addMove(Move m, int index, boolean inBattle) {
		Messages.add(getActualName() + " learned " + m.getAttack().getName() + "!");
		if (moves.size() < Move.MAX_MOVES) {
			moves.add(m);
		}
		else {
			moves.set(index, m);
		}

		if (!inBattle) {
			checkEvolution(EvolutionMethod.MOVE);
		}
	}
	
	public int getLevel() {
		return level;
	}
	
	public void callNewMove(Battle b, ActivePokemon opp, Move m) {
		Move temp = getMove();
		m.setAttributes(b, this);
		setMove(m);
		b.printAttacking(this);
		getAttack().apply(this, opp, b);
		setMove(temp);
	}

	// Wild Pokemon if in a wild battle and not the player's pokemon
	public boolean isWildPokemon(Battle b) {
		return b.isWildBattle() && !this.isPlayer();
	}

	public boolean canStealItem(Battle b, ActivePokemon victim) {
		return !this.isHoldingItem(b)
				&& victim.isHoldingItem(b)
				&& this.canSwapItems(b, victim);
	}

	public boolean canGiftItem(Battle b, ActivePokemon receiver) {
		return this.isHoldingItem(b) && !receiver.isHoldingItem(b);
	}

	public boolean canRemoveItem(Battle b, ActivePokemon victim) {
		return victim.isHoldingItem(b) && canSwapItems(b, victim);
	}

	public boolean canSwapItems(Battle b, ActivePokemon swapster) {
		return (this.isHoldingItem(b) || swapster.isHoldingItem(b))
				&& !this.isWildPokemon(b)
				&& !(swapster.hasAbility(AbilityNamesies.STICKY_HOLD) && !this.breaksTheMold());
	}

	public void swapItems(Battle b, ActivePokemon swapster, ItemSwapperEffect swapsicles) {
		Item userItem = this.getHeldItem(b);
		Item victimItem = swapster.getHeldItem(b);

		Messages.add(swapsicles.getSwitchMessage(this, userItem, swapster, victimItem));

		// For wild battles, an actual switch occurs
		if (b.isWildBattle()) {
			this.giveItem((HoldItem)victimItem);
			swapster.giveItem((HoldItem)userItem);
		} else {
			this.getAttributes().setCastSource(victimItem);
			EffectNamesies.CHANGE_ITEM.getEffect().apply(b, this, this, CastSource.CAST_SOURCE, false);

			this.getAttributes().setCastSource(userItem);
			EffectNamesies.CHANGE_ITEM.getEffect().apply(b, this, swapster, CastSource.CAST_SOURCE, false);
		}
	}

	public boolean canSwapOpponent(Battle b, ActivePokemon victim) {
		if (b.isFirstAttack() || victim.hasEffect(EffectNamesies.INGRAIN)) {
			return false;
		}

		if (victim.hasAbility(AbilityNamesies.SUCTION_CUPS) && !this.breaksTheMold()) {
			return false;
		}

		Team opponent = b.getTrainer(victim);
		if (opponent instanceof WildPokemon) {
			// Fails against wild Pokemon of higher levels
			return victim.getLevel() <= this.getLevel();
		}
		else {
			// Fails against trainers on their last Pokemon
			Trainer trainer = (Trainer)opponent;
			return trainer.hasRemainingPokemon(b);
		}
	}

	public void swapOpponent(Battle b, ActivePokemon victim, SwapOpponentEffect swapster) {
		if (!canSwapOpponent(b, victim)) {
			return;
		}

		Messages.add(swapster.getSwapMessage(this, victim));

		Team opponent = b.getTrainer(victim);
		if (opponent instanceof WildPokemon) {
			// End the battle against a wild Pokemon
			Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
		}
		else {
			Trainer trainer = (Trainer)opponent;

			// Swap to a random Pokemon!
			trainer.switchToRandom(b);
			victim = trainer.front();
			b.enterBattle(victim, "...and " + victim.getName() + " was dragged out!");
		}
	}

	public boolean switcheroo(Battle b, ActivePokemon caster, CastSource source, boolean wildExit) {
		Team team = b.getTrainer(this);
		String sourceName = source.getSourceName(b, this);
		String selfReference = caster == this ? "it" : this.getName();

		// End the battle against a wild Pokemon
		if (team instanceof WildPokemon) {
			if (!wildExit) {
				return false;
			}

			final String message;
			if (!StringUtils.isNullOrEmpty(sourceName)) {
				message = caster.getName() + "'s " + sourceName + " caused " + selfReference + " to leave the battle!";
			} else {
				message = this.getName() + " left the battle!";
			}

			Messages.add(message);
			Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
			return true;
		}

		Trainer trainer = (Trainer)team;
		if (!trainer.hasRemainingPokemon(b)) {
			// Don't switch if no one to switch to
			return false;
		}

		// Send this Pokemon back to the trainer and send out the next one
		final String message;
		if (!StringUtils.isNullOrEmpty(sourceName)) {
			message = caster.getName() + "'s " + sourceName + " sent " + selfReference + " back to " + trainer.getName() + "!";
		} else {
			message = this.getName() + " went back to " + trainer.getName() + "!";
		}

		Messages.add(message);

		// TODO: Prompt a legit switch fo user
		// TODO: Once this happens, this should take in a random parameter since this is still correct for Red Card, I believe and should have the message "name was sent out!"
		// TODO: Check if trainer action needs to be set to Switch
		trainer.switchToRandom(b);
		ActivePokemon front = trainer.front();
		b.enterBattle(front, trainer.getName() + " sent out " + front.getName() + "!");

		return true;
	}
	
	// Pangoro breaks the mold!
	public boolean breaksTheMold() {
		switch (getAbility().namesies()) {
			case MOLD_BREAKER:
			case TURBOBLAZE:
			case TERAVOLT:
				return true;
		}

		return this.hasEffect(EffectNamesies.BREAKS_THE_MOLD);
	}
	
	public boolean canFight() {
		return !hasStatus(StatusCondition.FAINTED) && !isEgg();
	}
	
	// Returns if the Pokemon is stalling -- that is that it will move last within its priority bracket
	public boolean isStalling(Battle b) {
		// TODO: Why is this only checking ability and item?
		return getAbility() instanceof StallingEffect || getHeldItem(b) instanceof StallingEffect;
	}
	
	public boolean hasAbility(AbilityNamesies a) {
		return getAbility().namesies() == a;
	}
	
	public void setStatus(Status s) {
		status = s;
	}

	public void addEffect(PokemonEffect e) {
		attributes.addEffect(e);
	}

	public void removeEffect(PokemonEffect effect) {
		attributes.removeEffect(effect);
	}
	
	public void setMove(Move m) {
		attributes.setMove(m);
	}
	
	public Move getMove() {
		return attributes.getMove();
	}
	
	public Attack getAttack() {
		Move m = attributes.getMove();
		if (m == null) {
			return null;
		}
		
		return m.getAttack();
	}
	
	public boolean isAttackType(Type t) {
		return getAttackType() == t;
	}
	
	public Type getAttackType() {
		return getMove().getType();
	}
	
	// Returns whether or not this Pokemon knows this move already
	public boolean hasActualMove(AttackNamesies name) {
		return hasMove(getActualMoves(), name);
	}
	
	public boolean hasMove(Battle b, AttackNamesies name) {
		return hasMove(getMoves(b), name);
	}
	
	private boolean hasMove(List<Move> moveList, AttackNamesies name) {
		for (Move m : moveList) {
			if (m.getAttack().namesies() == name) {
				return true;
			}
		}

		return false;
	}

	public String getGenderString() {
		if (this.isEgg()) {
			return StringUtils.empty();
		}

		return this.getGender().getCharacter();
	}
	
	public Gender getGender() {
		return gender;
	}
	
	public boolean isSemiInvulnerable() {
		final Move move = this.getMove();
		return move != null && !move.isReady() && ((MultiTurnMove) getAttack()).semiInvulnerability();
	}
	
	public boolean isSemiInvulnerableFlying() {
		return isSemiInvulnerable() && (getAttack().namesies() == AttackNamesies.FLY || getAttack().namesies() == AttackNamesies.BOUNCE);
	}
	
	public boolean isSemiInvulnerableDigging() {
		return isSemiInvulnerable() && getAttack().namesies() == AttackNamesies.DIG;
	}
	
	private int totalEVs() {
		int sum = 0;
		for (int EV : EVs) {
			sum += EV;
		}

		return sum;
	}
	
	// Adds Effort Values to a Pokemon, returns true if they were successfully added
	public boolean addEVs(int[] vals) {
		if (totalEVs() == Stat.MAX_EVS) {
			return false;
		}
		
		boolean added = false;
		for (int i = 0; i < EVs.length; i++) {
			if (vals[i] > 0 && EVs[i] < Stat.MAX_STAT_EVS) {
				added = true;
				EVs[i] = Math.min(Stat.MAX_STAT_EVS, EVs[i] + vals[i]); // Don't exceed stat EV amount
				
				// Don't exceed total EV amount
				if (totalEVs() > Stat.MAX_EVS) {
					EVs[i] -= (Stat.MAX_EVS - totalEVs());
					break;
				}
			}
			else if (vals[i] < 0 && EVs[i] > 0) {
				added = true;
				EVs[i] = Math.max(0, EVs[i] + vals[i]); // Don't drop below zero
			}
		}
		
		setStats();
		return added;
	}

	public boolean isDualTyped() {
		return this.getActualType()[1] != Type.NO_TYPE;
	}
	
	public Type[] getActualType() {
		return this.getPokemonInfo().getType();
	}
	
	public Type[] getDisplayType(Battle b) {
		return getType(b, true);
	}
	
	public Type[] getType(Battle b) {
		return getType(b, false);
	}
	
	private Type[] getType(Battle b, boolean displayOnly) {
		Type[] changeType = ChangeTypeEffect.getChangedType(b, this, displayOnly);
		if (changeType != null) {
			return changeType;
		}
		
		return getActualType();
	}
	
	public boolean isType(Battle b, Type type) {
		Type[] types = getType(b);
		return types[0] == type || types[1] == type; 
	}
	
	public int getHP() {
		return hp;
	}
	
	public void setHP(int amount) {
		hp = Math.min(getMaxHP(), Math.max(0, amount));
	}
	
	public boolean fullHealth() {
		return hp == getMaxHP();
	}
	
	public double getHPRatio() {
		return (double)hp/getMaxHP();
	}
	
	public Color getHPColor() {
		return DrawUtils.getHPColor(getHPRatio());
	}
	
	public String getActualName() {
		return nickname;
	}

	public void setNickname(String nickname) {
		if (!StringUtils.isNullOrEmpty(nickname)) {
			this.nickname = nickname;
		}
	}
	
	public String getName() {
		String changedName = NameChanger.getChangedName(this);
		if (changedName != null) {
			return changedName;
		}
		
		return getActualName();
	}
	
	public BattleAttributes getAttributes() {
		return attributes;
	}
	
	public boolean isPlayer() {
		return isPlayer;
	}
	
	public void resetAttributes() {
		attributes = new BattleAttributes(this);
		moves.forEach(Move::resetReady);
		ability = ability.namesies().getNewAbility();
	}
	
	public void setCaught() {
		isPlayer = true;
	}

	public boolean isActuallyDead() {
		return this.hasStatus(StatusCondition.FAINTED);
	}

	public boolean isFainted(Battle b) {
		// We have already checked that this Pokemon is fainted -- don't print/apply effects more than once
		if (isActuallyDead()) {
			if (hp == 0) {
				return true;
			}

			Global.error("Pokemon should only have the Fainted Status Condition when HP is zero.");
		}
		
		// Deady
		if (hp == 0) {
			Messages.add(new MessageUpdate().updatePokemon(b, this));

			ActivePokemon murderer = b.getOtherPokemon(this);
			Status.die(b, murderer, this);

			// If the pokemon fainted via murder (by direct result of an attack) -- apply kill wishes
			if (murderer.getAttributes().isAttacking()) {
				MurderEffect.killKillKillMurderMurderMurder(b, this, murderer);
			}
			
			b.getEffects(isPlayer).add((TeamEffect)EffectNamesies.DEAD_ALLY.getEffect());
			
			return true;	
		}
		
		// Still kickin' it
		return false;
	}
	
	public boolean canEscape(Battle b) {
		// Shed Shell always allows escape
		if (isHoldingItem(b, ItemNamesies.SHED_SHELL)) {
			return true;
		}
		
		// Check if the user is under an effect that prevents escape
		if (TrappingEffect.isTrapped(b, this)) {
			return false;
		}
		
		// The opponent has an effect that prevents escape
		ActivePokemon other = b.getOtherPokemon(this);
		if (OpponentTrappingEffect.isTrapped(b, this, other)) {
			return false;
		}
		
		// Safe and sound
		return true;
	}
	
	public boolean hasEffect(EffectNamesies effect) {
		return attributes.hasEffect(effect);
	}
	
	// Returns null if the Pokemon is not under the effects of the input effect, otherwise returns the Condition
	public PokemonEffect getEffect(EffectNamesies effect) {
		return attributes.getEffect(effect);
	}
	
	public List<PokemonEffect> getEffects() {
		return attributes.getEffects();
	}

	public List<Object> getAllEffects(final Battle b) {
		List<Object> list = new ArrayList<>();
		list.add(this.getStatus());
		list.add(this.getAbility());
		list.add(this.getHeldItem(b));
		list.addAll(this.getEffects());

		return list;
	}
	
	public void modifyStages(Battle b, ActivePokemon modifier, int[] mod, CastSource source) {
		for (int i = 0; i < mod.length; i++) {
			if (mod[i] == 0) {
				continue;
			}

			attributes.modifyStage(modifier, this, mod[i], Stat.getStat(i, true), b, source);
		}
	}
	
	public Status getStatus() {
		return status;
	}
	
	// Returns whether or not the Pokemon is afflicted with a status condition
	public boolean hasStatus() {
		return !this.hasStatus(StatusCondition.NO_STATUS);
	}
	
	public boolean hasStatus(StatusCondition type) {
		return status.isType(type);
	}
	
	// Sets the Pokemon's status condition to be None
	public void removeStatus() {
		Status.removeStatus(this);
	}

	// Don't think you'll make it out alive
	public void killKillKillMurderMurderMurder(Battle b) {
		reduceHealth(b, this.hp, false);
	}

	public int reduceHealth(Battle b, int amount) {
		return this.reduceHealth(b, amount, true);
	}
	
	// Reduces hp by amount, returns the actual amount of hp that was reduced
	public int reduceHealth(Battle b, int amount, boolean checkEffects) {

		// Not actually reducing health...
		if (amount == 0) {
			return 0;
		}

		// Check if the damage will be absorbed by an effect
		if (checkEffects && AbsorbDamageEffect.checkAbsorbDamageEffect(b, this, amount)) {
			return 0;
		}
		
		boolean fullHealth = fullHealth();
		
		// Reduce HP and record damage
		int prev = hp;
		setHP(hp - amount);
		int taken = prev - hp;
		attributes.takeDamage(taken);
		
		// Enduring the hit
		if (hp == 0 && checkEffects) {
			BracingEffect brace = BracingEffect.getBracingEffect(b, this, fullHealth);
			if (brace != null) {
				taken -= heal(1);

				Messages.add(new MessageUpdate().updatePokemon(b, this));
				Messages.add(brace.braceMessage(this));
			}
		}
		
		if (!isFainted(b)) {
			Messages.add(new MessageUpdate().updatePokemon(b, this));
			DamageTakenEffect.invokeDamageTakenEffect(b, this);
		}
		
		return taken;
	}
	
	// Reduces the amount of health that corresponds to fraction of the pokemon's total health and returns this amount
	public int reduceHealthFraction(Battle b, double fraction) {
		return reduceHealth(b, (int)Math.max(stats[Stat.HP.index()]*fraction, 1));
	}
	
	// Restores hp by amount, returns the actual amount of hp that was restored
	public int heal(int amount) {

		// Dead Pokemon can't heal
		if (hasStatus(StatusCondition.FAINTED)) {
			return 0;
		}
		
		int prev = hp;
		setHP(hp + amount);
		return hp - prev;
	}
	
	// Restores the amount of health that corresponds to fraction of the pokemon's total health and returns this amount
	public int healHealthFraction(double fraction) {
		return heal((int)Math.max(getMaxHP()*fraction, 1));
	}
	
	// Removes status, restores PP for all moves, restores to full health
	public void fullyHeal() {
		removeStatus();
		getActualMoves().forEach(Move::resetPP);
		healHealthFraction(1);
	}

	public Stat getBestBattleStat() {
		Stat bestStat = Stat.ATTACK;
		for (Stat stat : Stat.STATS) {
			if (stat == Stat.HP) {
				continue;
			}

			if (this.stats[stat.index()] > this.stats[bestStat.index()]) {
				bestStat = stat;
			}
		}

		return bestStat;
	}

	public boolean isGrounded(Battle b) {
		return GroundedEffect.containsGroundedEffect(b, this);
	}

    public boolean isLevitating(Battle b) {
        return isLevitating(b, null);
    }
	
	// Returns true if the Pokemon is currently levitating for any reason
	public boolean isLevitating(Battle b, ActivePokemon moldBreaker) {
		if (isGrounded(b)) {
			return false;
		}

		// Flyahs gon' Fly
		return isLevitatingWithoutTypeCheck(b, moldBreaker) || isType(b, Type.FLYING);
	}

	// Returns true if the Pokemon is currently levitating for any reason besides being a flying type pokemon
	// Grounded effect take precedence over levitation effects
	// Obvs levitating if you have a levitation effect
	// Stupid motherfucking Mold Breaker not allowing me to make Levitate a Levitation effect, fuck you Mold Breaker. -- NOT ANYMORE NOW WE HAVE Battle.hasInvoke FUCK YES YOU GO GLENN COCO
	public boolean isLevitatingWithoutTypeCheck(Battle b, ActivePokemon moldBreaker) {
		return !isGrounded(b) && LevitationEffect.containsLevitationEffect(b, this, moldBreaker);

	}

	public void giveItem(ItemNamesies itemName) {
		Item item = itemName.getItem();
		if (item.isHoldable()) {
			this.giveItem((HoldItem)item);
		}
	}

	public void giveItem(HoldItem item) {
		heldItem = item;
	}
	
	public void removeItem() {
		heldItem = (HoldItem)ItemNamesies.NO_ITEM.getItem();
	}

	public void stealBerry(Battle b, ActivePokemon victim) {
		Item item = victim.getHeldItem(b);
		if (item instanceof Berry && !victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
			Messages.add(this.getName() + " ate " + victim.getName() + "'s " + item.getName() + "!");
			victim.consumeItemWithoutEffects(b);

			if (item instanceof GainableEffectBerry) {
				((GainableEffectBerry)item).gainBerryEffect(b, this, CastSource.USE_ITEM);
				this.consumeBerry((Berry)item, b);
			}
		}
	}

	private void consumeBerry(Berry consumed, Battle b) {
		// Eat dat berry!!
		EffectNamesies.EATEN_BERRY.getEffect().cast(b, this, this, CastSource.HELD_ITEM, false);

		if (consumed instanceof GainableEffectBerry
				&& this.hasAbility(AbilityNamesies.CHEEK_POUCH)
				&& !this.fullHealth()) {
			Messages.add(this.getName() + "'s " + this.getAbility().getName() + " restored its health!");
			this.healHealthFraction(1/3.0);
			Messages.add(new MessageUpdate().updatePokemon(b, this));
		}
	}

	private Item consumeItemWithoutEffects(Battle b) {
		Item consumed = getHeldItem(b);
		EffectNamesies.CONSUMED_ITEM.getEffect().cast(b, this, this, CastSource.HELD_ITEM, false);

		return consumed;
	}
	
	public void consumeItem(Battle b) {
		Item consumed = this.consumeItemWithoutEffects(b);

		if (consumed instanceof Berry) {
			this.consumeBerry((Berry)consumed, b);
		}

		ActivePokemon other = b.getOtherPokemon(this);
		if (other.hasAbility(AbilityNamesies.PICKUP) && !other.isHoldingItem(b)) {
			other.giveItem((HoldItem)consumed);
			Messages.add(other.getName() + " picked up " + getName() + "'s " + consumed.getName() + "!");
		}
	}
	
	public Item getActualHeldItem() {
		return (Item)heldItem;
	}
	
	public Item getHeldItem(Battle b) {
		if (b == null) {
			return getActualHeldItem();
		}

		// TODO: Make effect interface for this
		if (hasAbility(AbilityNamesies.KLUTZ) || b.hasEffect(EffectNamesies.MAGIC_ROOM) || hasEffect(EffectNamesies.EMBARGO)) {
			return ItemNamesies.NO_ITEM.getItem();
		}
		
		// Check if the Pokemon has had its item changed during the battle
		PokemonEffect changeItem = getEffect(EffectNamesies.CHANGE_ITEM);
		Item item = changeItem == null ? getActualHeldItem() : ((ItemHolder)changeItem).getItem();
		
		if (item instanceof Berry && b.getOtherPokemon(this).hasAbility(AbilityNamesies.UNNERVE)) {
			return ItemNamesies.NO_ITEM.getItem();
		}
		
		return item;
	}
	
	public boolean isHoldingItem(Battle b, ItemNamesies itemName) {
		return getHeldItem(b).namesies() == itemName;
	}
	
	public boolean isHoldingItem(Battle b) {
		return getHeldItem(b).namesies() != ItemNamesies.NO_ITEM;
	}
	
	public boolean isShiny() {
		return shiny;
	}
	
	public PokemonInfo getPokemonInfo() {
		return PokemonInfo.getPokemonInfo(pokemon);
	}
	
	public boolean isPokemon(PokemonNamesies name) {
		return pokemon == name;
	}
	
	public double getWeight(Battle b) {
		int halfAmount = 0;
		halfAmount = HalfWeightEffect.updateHalfAmount(b, this, halfAmount);

		return this.getPokemonInfo().getWeight()/Math.pow(2, halfAmount);
	}
	
	public void startAttack(Battle b) {
		this.getAttributes().setAttacking(true);
		this.getMove().switchReady(b, this); // TODO: I don't think this works right because this is happening before you check if they're able to attack and honestly they shouldn't really switch until the end of the turn
		this.getMove().setAttributes(b, this);
	}
	
	public void endAttack(ActivePokemon opp, boolean success) {
		if (!success) {
			this.getAttributes().removeEffect(EffectNamesies.SELF_CONFUSION);
			this.getAttributes().resetCount();
		}
		
		this.getAttributes().setLastMoveUsed();
		
		if (this.getAttributes().shouldReducePP()) {
			this.getMove().reducePP(opp.hasAbility(AbilityNamesies.PRESSURE) ? 2 : 1);
		}
		
		this.getAttributes().setAttacking(false);
	}
	
	public boolean canBreed() {
		return !isEgg && this.getPokemonInfo().canBreed();
	}
}
