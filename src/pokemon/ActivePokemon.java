package pokemon;

import battle.Battle;
import battle.BattleAttributes;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.StallingEffect;
import battle.effect.attack.MultiTurnMove;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.BracingEffect;
import battle.effect.generic.EffectInterfaces.ChangeMoveListEffect;
import battle.effect.generic.EffectInterfaces.ChangeTypeEffect;
import battle.effect.generic.EffectInterfaces.DifferentStatEffect;
import battle.effect.generic.EffectInterfaces.FaintEffect;
import battle.effect.generic.EffectInterfaces.GroundedEffect;
import battle.effect.generic.EffectInterfaces.HalfWeightEffect;
import battle.effect.generic.EffectInterfaces.LevitationEffect;
import battle.effect.generic.EffectInterfaces.MurderEffect;
import battle.effect.generic.EffectInterfaces.NameChanger;
import battle.effect.generic.EffectInterfaces.OpponentTrappingEffect;
import battle.effect.generic.EffectInterfaces.TrappingEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import battle.effect.holder.AbilityHolder;
import battle.effect.holder.IntegerHolder;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.Item;
import item.ItemNamesies;
import item.berry.Berry;
import item.berry.HealthTriggeredBerry;
import item.hold.EVItem;
import item.hold.HoldItem;
import main.Global;
import main.Type;
import message.Messages;
import pattern.PokemonMatcher;
import pokemon.Evolution.EvolutionCheck;
import pokemon.PokemonInfo.WildHoldItem;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import util.DrawUtils;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivePokemon implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int MAX_LEVEL = 100;
	
	private static final String[][] characteristics =
		{{"Loves to eat",            "Proud of its power",      "Sturdy body",            "Highly curious",        "Strong willed",     "Likes to run"},
		 {"Takes plenty of siestas", "Likes to thrash about",   "Capable of taking hits", "Mischievous",           "Somewhat vain",     "Alert to sounds"},
		 {"Nods off a lot",          "A little quick tempered", "Highly persistent",      "Thoroughly cunning",    "Strongly defiant",  "Impetuous and silly"},
		 {"Scatters things often",   "Likes to fight",          "Good endurance",         "Often lost in thought", "Hates to lose",     "Somewhat of a clown"},
		 {"Likes to relax",          "Quick tempered",          "Good perseverance",      "Very finicky",          "Somewhat stubborn", "Quick to flee"}};
	
	private PokemonInfo pokemon;
	private String nickname;
	private int[] stats;
	private int[] IVs;
	private List<Move> moves;
	private int hp;
	private int level;
	private boolean playerPokemon;
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
	private Type hiddenPowerType;
	private boolean isEgg;
	private int eggSteps;

	// General constructor for an active Pokemon (user is true if it is the player's pokemon and false if it is wild, enemy trainer, etc.)
	public ActivePokemon(PokemonNamesies pokemonNamesies, int level, boolean isWild, boolean user) {
		this.pokemon = PokemonInfo.getPokemonInfo(pokemonNamesies);
		this.nickname = this.pokemon.getName();
		this.level = level;
		
		setIVs();
		
		this.nature = new Nature();
		setCharacteristic();
		
		this.EVs = new int[Stat.NUM_STATS];
		this.stats = new int[Stat.NUM_STATS];
		setStats();
		
		this.hp = stats[Stat.HP.index()];
		this.playerPokemon = user;
		this.attributes = new BattleAttributes();
		
		removeStatus();
		
		this.totalEXP = pokemon.getGrowthRate().getEXP(this.level);
		this.totalEXP += Global.getRandomInt(expToNextLevel());
		this.gender = Gender.getGender(pokemon.getMaleRatio());
		this.shiny = (user || isWild) && Global.chanceTest(1, 8192);
		
		setMoves();
		
		this.ability = Ability.assign(this.pokemon);
		this.hiddenPowerType = computeHiddenPowerType();
		
		this.heldItem = isWild ? WildHoldItem.getWildHoldItem(this.pokemon.getWildItems()) : (HoldItem)ItemNamesies.NO_ITEM.getItem();
		
		this.isEgg = false;
		this.eggSteps = 0;
	}
	
	// Constructor for Eggs
	public ActivePokemon(PokemonNamesies pokemonNamesies) {
		this(pokemonNamesies, 1, false, true);

		this.isEgg = true;
		this.eggSteps = this.pokemon.getEggSteps();
		this.nickname = "Egg";
	}
	
	public ActivePokemon(ActivePokemon daddy, ActivePokemon mommy, PokemonNamesies pokemonNamesies) {
		this(pokemonNamesies, 1, false, true);
		
		moves = Breeding.getBabyMoves(daddy, mommy, pokemonNamesies);
		IVs = Breeding.getBabyIVs(daddy, mommy);
		nature = Breeding.getBabyNature(daddy, mommy);
		hiddenPowerType = computeHiddenPowerType();
		setStats();
		setCharacteristic();
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
		
		// Random Egg
		if (pokemonMatcher.isRandomEgg()) {
			if (!user) {
				Global.error("Trainers cannot have eggs.");
			}

			return new ActivePokemon(PokemonInfo.getRandomBaseEvolution());
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
	
	// Does not include shiny -- this is for the small party tiles
	public int getTinyImageIndex() {
		return this.isEgg ? PokemonInfo.EGG_IMAGE : pokemon.getNumber();
	}
	
	// Larger image index
	public int getImageIndex() {
		return this.isEgg() ? PokemonInfo.EGG_IMAGE : this.pokemon.getImageNumber(this.isShiny());
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
	
	private void setMoves() {
		moves = new ArrayList<>();
		Map<Integer, Set<AttackNamesies>> map = pokemon.getLevelUpMoves();
		for (Integer levelLearned : map.keySet()) {
			if (levelLearned > level) {
				continue;
			}
			
			for (AttackNamesies attackNamesies : map.get(levelLearned)) {
				if (hasActualMove(attackNamesies)) {
					continue;
				}
				
				moves.add(new Move(attackNamesies.getAttack()));
				
				// This can be an 'if' statement, but just to be safe...
				while (moves.size() > Move.MAX_MOVES) {
					moves.remove(0);
				}
			}
		}
	}
	
	public void setMoves(List<Move> list) {
		moves = list;
	}
	
	public void setShiny() {
		shiny = true;
	}
	
	// Random value between 0 and 31
	private void setIVs() {
		IVs = new int[Stat.NUM_STATS];
		for (int i = 0; i < IVs.length; i++) {
			IVs[i] = Stat.getRandomIv();
		}
	}
	
	private void setCharacteristic() {
		int maxIndex = 0;
		for (int i = 1; i < IVs.length; i++) {
			if (IVs[i] > IVs[maxIndex]) {
				maxIndex = i;
			}
		}
		
		characteristic = characteristics[IVs[maxIndex]%5][maxIndex];
	}
	
	private void setStats() {
		int prevHP = stats[Stat.HP.index()];
		
		stats = new int[Stat.NUM_STATS];
		for (int i = 0; i < stats.length; i++) {
			stats[i] = Stat.getStat(i, level, pokemon.getStat(i), IVs[i], EVs[i], nature.getNatureVal(i));
		}
		
		hp += stats[Stat.HP.index()] - prevHP;
	}
	
	private Type computeHiddenPowerType() {
		return Type.getHiddenType(((
				IVs[Stat.HP.index()]%2 +
				2*(IVs[Stat.ATTACK.index()]%2) +
				4*(IVs[Stat.DEFENSE.index()]%2) +
				8*(IVs[Stat.SPEED.index()]%2) +
				16*(IVs[Stat.SP_ATTACK.index()]%2) +
				32*(IVs[Stat.SP_DEFENSE.index()]%2)
			)*15)/63);
	}
	
	public Type getHiddenPowerType() {
		return hiddenPowerType;
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
	
	public void assignAbility(Ability newAbility) {
		ability = newAbility;
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
	
	public int getStage(int index) {
		return attributes.getStage(index);
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
		
		return pokemon.getGrowthRate().getEXP(level + 1) - totalEXP;
	}
	
	// TODO: Test this to make sure it still works (espesh level 100)
	public float expRatio() {
		if (level == MAX_LEVEL) {
			return 0;
		}
		
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
		Messages.addMessage(getActualName() + " gained " + gain + " EXP points!");
		if (front) {
			Messages.addMessage("", b, this, Math.min(1, expRatio()), false);
		}
		
		// Add EVs
		Item i = getHeldItem(b);
		int[] vals = dead.getPokemonInfo().getGivenEVs();
		if (i instanceof EVItem) {
			vals = ((EVItem)i).getEVs(vals);
		}
		
		addEVs(vals);
		
		// Level up if applicable
		while (totalEXP >= pokemon.getGrowthRate().getEXP(level + 1)) {
			levelUp(b);
		}
	}

	public boolean levelUp(Battle b) {
		if (level == MAX_LEVEL) {
			return false;
		}
		
		boolean print = b != null;
		boolean front = print && b.getPlayer().front() == this;
		
		// Grow to the next level
		level++;
		if (print) {
			Messages.addMessage(getActualName() + " grew to level " + level + "!");
		}

		if (print && front) {
			Messages.addMessage("", b, this, Math.min(1, expRatio()), true);
		}
		
		// Change stats -- keep track of the gains
		int[] prevStats = stats.clone();
		int[] gain = new int[Stat.NUM_STATS];
		setStats();
		for (int i = 0; i < Stat.NUM_STATS; i++) {
			gain[i] = stats[i] - prevStats[i];
		}
		
		// TODO: Show gain update for other Pokemon in the party
		if (print && front) {
			Messages.addMessage("", b, this, gain, stats);
		}
		
		// Learn new moves
		for (AttackNamesies attack : pokemon.getMoves(level)) {
			learnMove(b, attack);
		}
		
		// Maybe you'll evolve?!
		BaseEvolution ev = (BaseEvolution)pokemon.getEvolution().getEvolution(EvolutionCheck.LEVEL, this, null);
		if (ev != null) {
			evolve(b, ev);
		}
		
		return true;
	}
	
	public void evolve(Battle b, BaseEvolution ev) {
		if (getActualHeldItem().namesies() == ItemNamesies.EVERSTONE) {
			return;
		}
		
		boolean print = b != null;
		boolean front = print && b.getPlayer().front() == this;
		boolean sameName = nickname.equals(pokemon.getName());
		
		ability = Ability.evolutionAssign(this, ev.getEvolution());
		
		String name = nickname;
		if (print) {
			Messages.addMessage(getActualName() + " is evolving!");
		}
		
		pokemon = ev.getEvolution();
		if (print) {
			b.getPlayer().getPokedex().setCaught(this.getPokemonInfo());
		}
		
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
		
		if (print && front) {
			Messages.addMessage("", pokemon, shiny, true, playerPokemon);
		}
		
		String message = name + " evolved into " + pokemon.getName() + "!";
		
		if (print) {
			Messages.addMessage(message);
		}

		if (print && front) {
			Messages.addMessage("", b, this, gain, stats);
		}
		
		// Learn new moves
		Set<AttackNamesies> levelMoves = pokemon.getMoves(level);
		for (AttackNamesies attack : levelMoves) {
			learnMove(b, attack);
		}
	}
	
	private void learnMove(Battle b, AttackNamesies attackName) {
		// Don't want to learn a move you already know!
		if (hasActualMove(attackName)) {
			return;
		}
		
		Move m = new Move(attackName.getAttack());
		if (moves.size() < Move.MAX_MOVES) {
			if (b != null) {
				Messages.addMessage(getActualName() + " learned " + m.getAttack().getName() + "!");
			}
			
			addMove(b, m, moves.size() - 1);
			return;
		}
		
		// Only add messagy things whilst in battle TODO: But really we need to be able to do messagy things outside of battle too...
		if (b == null) {
			return;
		}
		
		Messages.addMessage(" ", this, m);
		Messages.addMessage(getActualName() + " did not learn " + m.getAttack().getName() + ".");
		
		// Wait I think this is in a motherfucking for loop because this is really poorly and hackily implemented...
		for (Move move : moves) {
			Messages.addMessage(getActualName() + " forgot how to use " + move.getAttack().getName() + "...");
		}
		
		Messages.addMessage("...and " + getActualName() + " learned " + m.getAttack().getName() + "!");
	}
	
	public void addMove(Battle b, Move m, int index) {
		if (moves.size() < Move.MAX_MOVES) {
			moves.add(m);
		}
		else {
			moves.set(index, m);
		}
		
		BaseEvolution ev = (BaseEvolution)pokemon.getEvolution().getEvolution(EvolutionCheck.MOVE, this, null);
		if (ev != null) {
			evolve(b, ev);
		}
	}
	
	public int getLevel() {
		return level;
	}
	
	public void callNewMove(Battle b, ActivePokemon opp, Move m) {
		Move temp = getMove();
		m.setAttributes(b, this, opp);
		setMove(m);
		b.printAttacking(this);
		getAttack().apply(this, opp, b);
		setMove(temp);
	}
	
	// Pangoro breaks the mold!
	public boolean breaksTheMold() {
		switch (getAbility().namesies()) {
			case MOLD_BREAKER:
			case TURBOBLAZE:
			case TERAVOLT:
				return true;
			default:
				return false;
		}
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
	
	public int getAttackPower() {
		return getMove().getPower();
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
	
	public boolean isSemiInvulnerableDiving() {
		return isSemiInvulnerable() && getAttack().namesies() == AttackNamesies.DIVE;
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
	
	public Type[] getActualType() {
		return pokemon.getType();
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
	
	public boolean user() {
		return playerPokemon;
	}
	
	public void resetAttributes() {
		attributes = new BattleAttributes();
		for (Move m : moves) {
			m.resetReady();
		}
	}
	
	public void setCaught() {
		playerPokemon = true;
	}
	
	public boolean isFainted(Battle b)
	{
		// We have already checked that this Pokemon is fainted -- don't print/apply effects more than once
		if (hasStatus(StatusCondition.FAINTED)) {
			if (hp == 0) {
				return true;
			}

			Global.error("Pokemon should only have the Fainted Status Condition when HP is zero.");
		}
		
		// Deady
		if (hp == 0) {
			Messages.addMessage("", b, this);
			
			Status.die(this);
			Messages.addMessage(getName() + " fainted!", b, this);
			
			ActivePokemon murderer = b.getOtherPokemon(user());

			// Apply effects which occur when the user faints
			FaintEffect.grantDeathWish(b, this, murderer);
			
			// If the pokemon fainted via murder (by direct result of an attack) -- apply kill wishes
			if (murderer.getAttributes().isAttacking()) {
				MurderEffect.killKillKillMurderMurderMurder(b, this, murderer);
			}
			
			b.getEffects(playerPokemon).add((TeamEffect)EffectNamesies.DEAD_ALLY.getEffect());
			
			return true;	
		}
		
		// Still kickin' it
		return false;
	}
	
	// Returns the empty string if the Pokemon can switch, and the appropriate fail message if they cannot
	public boolean canEscape(Battle b) {
		// Shed Shell always allows escape
		if (isHoldingItem(b, ItemNamesies.SHED_SHELL)) {
			return true;
		}
		
		// Check if the user is under an effect that prevents escape
		TrappingEffect trapped = TrappingEffect.getTrapped(b, this);
		if (trapped != null) {
			Messages.addMessage(trapped.trappingMessage(this));
			return false;
		}
		
		// The opponent has an effect that prevents escape
		ActivePokemon other = b.getOtherPokemon(user());
		OpponentTrappingEffect trappedByOpponent = OpponentTrappingEffect.getTrapped(b, this, other);
		if (trappedByOpponent != null) {
			Messages.addMessage(trappedByOpponent.opponentTrappingMessage(this, other));
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
		list.addAll(this.getEffects());
		list.add(this.getStatus());
		list.add(this.getAbility());
		list.add(this.getHeldItem(b));

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
		return status.getType() != StatusCondition.NO_STATUS;
	}
	
	public boolean hasStatus(StatusCondition type) {
		return status.getType() == type;
	}
	
	// Sets the Pokemon's status condition to be None
	public void removeStatus() {
		Status.removeStatus(this);
		attributes.removeEffect(EffectNamesies.NIGHTMARE); // TODO: There should be a way for effects to be tied to status conditions so that they don't have to be hardcoded here
		attributes.removeEffect(EffectNamesies.BAD_POISON);
	}
	
	// Reduces hp by amount, returns the actual amount of hp that was reduced
	public int reduceHealth(Battle b, int amount) {

		// Not actually reducing health...
		if (amount == 0) {
			return 0;
		}
		
		// Substitute absorbs the damage instead of the Pokemon
		IntegerHolder e = (IntegerHolder)getEffect(EffectNamesies.SUBSTITUTE);
		if (e != null) {
			e.decrease(amount);
			if (e.getAmount() <= 0) {
				Messages.addMessage("The substitute broke!");
				attributes.removeEffect(EffectNamesies.SUBSTITUTE);
			}
			else {
				Messages.addMessage("The substitute absorbed the hit!");
			}
			
			return 0;
		}
		
		boolean fullHealth = fullHealth();
		
		// Reduce HP, record damage, and check if fainted
		int prev = hp, taken = prev - (hp = Math.max(0, hp - amount));
		attributes.takeDamage(taken);
		
		// Enduring the hit
		if (hp == 0) {
			BracingEffect brace = BracingEffect.getBracingEffect(b, this, fullHealth);
			if (brace != null) {
				taken -= heal(1);
				
				Messages.addMessage("", b, this);
				Messages.addMessage(brace.braceMessage(this));				
			}
		}
		
		if (isFainted(b)) {
			return taken;
		}
		
		Messages.addMessage("", b, this);
		
		// Check if the Pokemon fainted and also handle Focus Punch
		if (hasEffect(EffectNamesies.FOCUSING)) {
			Messages.addMessage(getName() + " lost its focus and couldn't move!");
			attributes.removeEffect(EffectNamesies.FOCUSING);
			addEffect((PokemonEffect)EffectNamesies.FLINCH.getEffect());
		}
		
		// Health Triggered Berries
		Item item = getHeldItem(b);
		if (item instanceof HealthTriggeredBerry) {
			HealthTriggeredBerry berry  = (HealthTriggeredBerry)item;
			double healthRatio = getHPRatio();
			if ((healthRatio <= berry.healthTriggerRatio() || (healthRatio <= .5 && hasAbility(AbilityNamesies.GLUTTONY)))) {
				if (berry.gainBerryEffect(b, this, CastSource.HELD_ITEM)) {
					consumeItem(b);
				}
			}
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
		hp = Math.min(getMaxHP(), hp + amount);
		return hp - prev;
	}
	
	// Restores the amount of health that corresponds to fraction of the pokemon's total health and returns this amount
	public int healHealthFraction(double fraction) {
		return heal((int)Math.max(getMaxHP()*fraction, 1));
	}
	
	// Removes status, restores PP for all moves, restores to full health
	public void fullyHeal() {
		removeStatus();
		this.getActualMoves().forEach(Move::resetPP);
		healHealthFraction(1);
	}
	
	// Heals the Pokemon by damage amount. It is assume damage has already been dealt to the victim
	public void sapHealth(ActivePokemon victim, int amount, Battle b, boolean print, boolean dreamEater) {
		if (victim.hasAbility(AbilityNamesies.LIQUID_OOZE)) {
			Messages.addMessage(victim.getName() + "'s " + AbilityNamesies.LIQUID_OOZE.getName() + " caused " + getName() + " to lose health instead!");
			reduceHealth(b, amount);
			return;
		}
		
		// Big Root heals an additional 30%
		if (isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
			amount *= 1.3;
		}
		
		// Sap message (different for Dream Eater)
		if (print) {
			String message = dreamEater ? victim.getName() + "'s dream was eaten!" : victim.getName() + "'s health was sapped!"; 
			Messages.addMessage(message);
		}
		
		// Healers gon' heal
		if (!hasEffect(EffectNamesies.HEAL_BLOCK)) {
			heal(amount);
		}
		
		Messages.addMessage("", b, victim);
		Messages.addMessage("", b, this);
	}
	
	public boolean isGrounded(Battle b) {
		return GroundedEffect.containsGroundedEffect(b, this);
	}
	
	// Returns true if the Pokemon is currently levitating for any reason
	public boolean isLevitating(Battle b) {

		// Grounded effect take precedence over levitation effects
		if (isGrounded(b)) {
			return false;
		}
		
		// Obvs levitating if you have a levitation effect
		// Stupid motherfucking Mold Breaker not allowing me to make Levitate a Levitation effect, fuck you Mold Breaker. -- NOT ANYMORE NOW WE HAVE Battle.hasInvoke FUCK YES YOU GO GLENN COCO
		if (LevitationEffect.containsLevitationEffect(b, this)) {
			return true;
		}
		
		// Flyahs gon' Fly
		return isType(b, Type.FLYING);
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
	
	public void consumeItem(Battle b) {
		Item consumed = getHeldItem(b);
		EffectNamesies.CONSUMED_ITEM.getEffect().cast(b, this, this, CastSource.HELD_ITEM, false);
		
		ActivePokemon other = b.getOtherPokemon(playerPokemon); 
		if (other.hasAbility(AbilityNamesies.PICKUP) && !other.isHoldingItem(b)) {
			other.giveItem((HoldItem)consumed);
			Messages.addMessage(other.getName() + " picked up " + getName() + "'s " + consumed.getName() + "!");
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
		
		if (item instanceof Berry && b.getOtherPokemon(user()).hasAbility(AbilityNamesies.UNNERVE)) {
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
		return pokemon;
	}
	
	public boolean isPokemon(PokemonNamesies name) {
		return pokemon.namesies() == name;
	}
	
	public double getWeight(Battle b) {
		int halfAmount = 0;
		halfAmount = HalfWeightEffect.updateHalfAmount(b, this, halfAmount);

		return this.pokemon.getWeight()/Math.pow(2, halfAmount);
	}
	
	public void startAttack(Battle b, ActivePokemon opp) {
		this.getAttributes().setAttacking(true);
		this.getMove().switchReady(b, this); // TODO: I don't think this works right because this is happening before you check if they're able to attack and honestly they shouldn't really switch until the end of the turn
		this.getMove().setAttributes(b, this, opp);
	}
	
	public void endAttack(ActivePokemon opp, boolean success, boolean reduce) {
		if (!success) {
			this.getAttributes().removeEffect(EffectNamesies.SELF_CONFUSION);
			this.getAttributes().resetCount();
		}
		
		this.getAttributes().setLastMoveUsed();
		
		if (reduce) {
			this.getMove().reducePP(opp.hasAbility(AbilityNamesies.PRESSURE) ? 2 : 1);
		}
		
		this.getAttributes().setAttacking(false);
	}
	
	boolean canBreed() {
		return !isEgg && pokemon.canBreed();
	}
}
