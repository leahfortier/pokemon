package battle;

import battle.attack.Attack;
import battle.attack.MoveType;
import battle.effect.DefiniteEscape;
import battle.effect.generic.BattleEffect;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectInterfaces.AccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.AlwaysCritEffect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.CrashDamageMove;
import battle.effect.generic.EffectInterfaces.CritBlockerEffect;
import battle.effect.generic.EffectInterfaces.CritStageEffect;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.EntryEffect;
import battle.effect.generic.EffectInterfaces.NameChanger;
import battle.effect.generic.EffectInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.OpponentBeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.OpponentPowerChangeEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.PriorityChangeEffect;
import battle.effect.generic.EffectInterfaces.SuperDuperEndTurnEffect;
import battle.effect.generic.EffectInterfaces.TerrainCastEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import battle.effect.generic.Weather;
import item.ItemNamesies;
import main.Game;
import main.Global;
import map.area.AreaData;
import map.overworld.TerrainType;
import map.weather.WeatherState;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import message.Messages.MessageState;
import pattern.action.UpdateMatcher;
import pokemon.ActivePokemon;
import pokemon.Stat;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Team;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.WildPokemon;
import trainer.player.Player;
import type.TypeAdvantage;
import util.PokeString;
import util.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Battle implements Serializable {
	private final Player player;
	private final Opponent opponent; // SO OBJECT-ORIENTED

	private List<BattleEffect> effects;

	private WeatherState baseWeather;
	private Weather weather;

	private TerrainType baseTerrain;
	private TerrainType currentTerrain;

	private int turn;
	private boolean firstAttacking;
	private int escapeAttempts;

	private transient UpdateMatcher npcUpdateInteraction;
	
	public Battle(Opponent opponent) {
		Messages.clearMessages(MessageState.FIGHTY_FIGHT);
		Messages.setMessageState(MessageState.FIGHTY_FIGHT);
		Messages.add(new MessageUpdate().withUpdate(Update.ENTER_BATTLE));

		this.player = Game.getPlayer();
		this.opponent = opponent;

		this.effects = new ArrayList<>();
		this.player.resetEffects();
		this.opponent.resetEffects();

		turn = 0;
		escapeAttempts = 0;
		firstAttacking = false;

		AreaData area = Game.getData().getMap(player.getMapName()).getArea(player.getLocation());
		this.setBaseWeather(area.getWeather());
		this.setTerrainType(area.getBattleTerrain(), true);

		int maxPokemonAllowed = opponent.maxPokemonAllowed();
		if (maxPokemonAllowed < Trainer.MAX_POKEMON) {
			boolean onlyOne = maxPokemonAllowed == 1;
			Messages.add(String.format("%s %d %s %s allowed for this battle!!",
					onlyOne ? "Only" : "At most",
					maxPokemonAllowed,
					PokeString.POKEMON,
					onlyOne ? "is" : "are"
			));
		}

		this.player.enterBattle();
		if (this.opponent instanceof Trainer) {
			Trainer opponentTrainer = (Trainer) this.opponent;
			opponentTrainer.enterBattle();
			Messages.add(opponentTrainer.getName() + " wants to fight!");
			opponentTrainer.setAction(TrainerAction.FIGHT);
			enterBattle(this.opponent.front());
		}
		else {
			ActivePokemon wildPokemon = this.opponent.front();
			enterBattle(wildPokemon, "Wild " + wildPokemon.getName() + " appeared!");
		}

		enterBattle(this.player.front());
	}

	public Battle(EnemyTrainer npcTrainer, UpdateMatcher npcUpdateInteraction) {
		this(npcTrainer);
		this.npcUpdateInteraction = npcUpdateInteraction;
	}

	public Player getPlayer() {
		return player;
	}

	public Opponent getOpponent() {
		return opponent;
	}

	public Weather getWeather() {
		return weather;
	}

	public int getTurn() {
		return turn;
	}

	public UpdateMatcher getNpcUpdateInteraction() {
		return this.npcUpdateInteraction;
	}

	public TerrainType getTerrainType() {
		return currentTerrain;
	}

	public void setBaseWeather(WeatherState weatherState) {
		this.baseWeather = weatherState;
		this.addEffect((Weather)weatherState.getWeatherEffect().getEffect());
	}

	public void setTerrainType(TerrainType terrainType, boolean base) {
		if (base) {
			this.baseTerrain = terrainType;
		}

		this.currentTerrain = terrainType;

		TerrainCastEffect.invokeTerrainCastEffect(this, player.front(), terrainType);
		TerrainCastEffect.invokeTerrainCastEffect(this, opponent.front(), terrainType);

		Messages.add(new MessageUpdate().withTerrain(currentTerrain));
	}

	public void resetTerrain() {
		this.currentTerrain = baseTerrain;
		Messages.add(new MessageUpdate().withTerrain(currentTerrain));
	}

	public boolean hasEffect(EffectNamesies effect) {
		return Effect.hasEffect(effects, effect);
	}

	public void fight() {
		startTurn();

		boolean playerFirst = speedPriority(player.front(), opponent.front());
		if (playerFirst) {
			fight(player.front(), opponent.front());
		} else {
			fight(opponent.front(), player.front());
		}
	}

	protected void fight(ActivePokemon attackFirst, ActivePokemon attackSecond) {
		// First turn
		firstAttacking = true;
		executionSolution(attackFirst, attackSecond);

		// Second turn
		firstAttacking = false;
		executionSolution(attackSecond, attackFirst);

		endTurn();

		deadOpponent();
		deadUser();

		printShit();
	}

	protected void printShit() {
		if (Messages.isMessageState(MessageState.SIMULATION_STATION)) {
			return;
		}

		for (PokemonEffect e : player.front().getEffects()) {
			System.out.println("P " + e);
		}

		for (PokemonEffect e : opponent.front().getEffects()) {
			System.out.println("O " + e);
		}

		for (TeamEffect e : player.getEffects()) {
			System.out.println("P " + e);
		}

		for (TeamEffect e : opponent.getEffects()) {
			System.out.println("O " + e);
		}

		for (BattleEffect e : getEffects()) {
			System.out.println("B " + e);
		}

		if (weather.namesies() != baseWeather.getWeatherEffect()) {
			System.out.println("W " + weather);
		}

		System.out.print(player.front().getActualName() + " ");
		for (Stat stat : Stat.BATTLE_STATS) {
			System.out.print(player.front().getStage(stat) + " ");
		}
		System.out.println();

		System.out.print(opponent.front().getActualName() + " ");
		for (Stat stat : Stat.BATTLE_STATS) {
			System.out.print(opponent.front().getStage(stat) + " ");
		}
		System.out.println();

		System.out.println(player.front().getActualName() + " " + player.front().getAbility().getName() + " " + player.front().getHeldItem(this).getName());
		System.out.println(opponent.front().getActualName() + " " + opponent.front().getAbility().getName() + " " + opponent.front().getHeldItem(this).getName());
	}

	// Handles events that occur at the beginning of each turn. Returns the two Pokemon currently in battle
	private void startTurn() {
		ActivePokemon plyr = player.front();
		ActivePokemon opp = opponent.front();

		turn++;
		plyr.getAttributes().resetTurn();
		opp.getAttributes().resetTurn();

		// Fucking focus punch
		if (isFighting(true)) {
			plyr.getAttack().startTurn(this, plyr);
		}

		if (isFighting(false)) {
			opp.getAttack().startTurn(this, opp);
		}
	}

	public boolean isFirstAttack() {
		return firstAttacking;
	}

	// If the trainer selected an attack, this will return true - Wild Pokemon will always return true
	// It will return false if the trainer tried to run, switched Pokemon, or used an item
	private boolean isFighting(boolean team) {
		Team trainer = getTrainer(team);
		return trainer instanceof WildPokemon || ((Trainer)trainer).getAction() == TrainerAction.FIGHT;
	}

	private void endTurn() {
		// Apply Effects
		endTurnPokemonEffects(player.front());
		endTurnPokemonEffects(opponent.front());

		// Decrement Pokemon effects
		decrementEffects(player.front().getEffects(), player.front());
		decrementEffects(opponent.front().getEffects(), opponent.front());

		// Decrement Team effects
		decrementEffects(player.getEffects(), player.front());
		decrementEffects(opponent.getEffects(), opponent.front());

		// Decrement Battle effects
		decrementEffects(effects, null);
		decrementWeather();

		// The very, very end
		while (SuperDuperEndTurnEffect.checkSuperDuperEndTurnEffect(this, player.front())
				|| SuperDuperEndTurnEffect.checkSuperDuperEndTurnEffect(this, opponent.front()));
	}

	public boolean deadUser() {
		// Front Pokemon is still functioning
		if (!player.front().isFainted(this)) {
			return false;
		}

		// Dead Front Pokemon, but you still have others to spare -- force a switch
		if (!player.blackout()) {
			Messages.add(new MessageUpdate("What Pokemon would you like to switch to?").withUpdate(Update.FORCE_SWITCH));
			return false;
		}

		// Blackout -- you're fucked
		Messages.add(player.getName() + " is out of usable " + PokeString.POKEMON + "! " + player.getName() + " blacked out!");

		// Sucks to suck
		if (opponent instanceof Trainer) {
			Trainer opp = (Trainer)opponent;
			int cashMoney = player.sucksToSuck(opp.getDatCashMoney());
			Messages.add(opp.getName() + " rummaged through the pockets of your passed out body and stole " + cashMoney + " pokedollars!!!");
		}

		player.healAll();
		player.teleportToPokeCenter();
		Messages.clearMessages(MessageState.MAPPITY_MAP);
		Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));

		return true;
	}

	public boolean deadOpponent() {
		ActivePokemon dead = opponent.front();

		// YOU'RE FINE
		if (!dead.isFainted(this)) {
			return false;
		}

		// Gain dat EXP
		player.gainEXP(dead, this);

		// You have achieved total victory
		if (opponent.blackout()) {
			player.winBattle(this, opponent);
			return true;
		}

		// We know this is not a wild battle anymore and I don't feel like casting so much
		Trainer opp = (Trainer)opponent;

		// They still have some Pokes left
		opp.switchToRandom(this);
		enterBattle(opp.front());

		return false;
	}

	public void enterBattle(ActivePokemon enterer) {
		NameChanger.setNameChanges(this, enterer);

		String enterMessage = "";
		if (enterer.isPlayer()) {
			enterMessage = "Go! " + enterer.getName() + "!";
		}
		else if (opponent instanceof Trainer) {
			enterMessage = ((Trainer)opponent).getName() + " sent out " + enterer.getName() + "!";
		}

		enterBattle(enterer, enterMessage);
	}

	public void enterBattle(ActivePokemon enterer, String enterMessage) {
		if (enterer.isEgg()) {
			Global.error("Eggs can't battle!!!");
		}

		// Document sighting in the Pokedex
		if (!enterer.isPlayer()) {
			player.getPokedex().setSeen(enterer, isWildBattle());
		}

		enterer.resetAttributes();
		NameChanger.setNameChanges(this, enterer);

		Messages.add(new MessageUpdate(enterMessage).withSwitch(this, enterer));

		enterer.getAttributes().setUsed(true);
		EntryEffect.invokeEntryEffect(this, enterer);

		getTrainer(!enterer.isPlayer()).resetUsed();
	}

	public boolean runAway() {
		escapeAttempts++;

		if (opponent instanceof Trainer) {
			Messages.add("There's no running from a trainer battle!");
			return false;
		}

		ActivePokemon plyr = player.front();
		ActivePokemon opp = opponent.front();

		if (!plyr.canEscape(this)) {
			return false;
		}

		int pSpeed = Stat.getStat(Stat.SPEED, plyr, opp, this);
		int oSpeed = Stat.getStat(Stat.SPEED, opp, plyr, this);

		int val = (int)((pSpeed*32.0)/(oSpeed/4.0) + 30.0*escapeAttempts);
		if (RandomUtils.chanceTest(val, 256) ||
				plyr.getAbility() instanceof DefiniteEscape || // TODO: This is wrong and should be able to escape even with mean look and such
				plyr.getHeldItem(this) instanceof DefiniteEscape) {
			Messages.add("Got away safely!");
			Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
			return true;
		}

		Messages.add("Can't escape!");
		player.performAction(this, TrainerAction.RUN);
		return false;
	}

	private void decrementEffects(List<? extends Effect> effects, ActivePokemon p) {
		for (int i = 0; i < effects.size(); i++) {
			Effect effect = effects.get(i);

			boolean inactive = !effect.isActive();
			if (!inactive) {
				effect.decrement(this, p);
				inactive = !effect.isActive() && !effect.nextTurnSubside();
			}

			if (inactive) {
				effects.remove(i--);
				effect.subside(this, p);

				// I think this is pretty much just for Future Sight...
				if (p != null && p.isFainted(this)) {
					return;
				}
			}
		}
	}

	private void decrementWeather() {
		if (!weather.isActive()) {
			Messages.add(weather.getSubsideMessage(player.front()));
			this.setBaseWeather(this.baseWeather);
			return;
		}

		weather.applyEndTurn(player.front(), this);
		weather.decrement(this, player.front());
	}
	
	private void endTurnPokemonEffects(ActivePokemon me) {
		EndTurnEffect.invokeEndTurnEffect(me, this);
		
		me.isFainted(this);
		
		// No longer the first turn anymore
		me.getAttributes().setFirstTurn(false);
	}

	private boolean isFront(ActivePokemon p) {
		return p == getTrainer(p).front();
	}

	private void executionSolution(ActivePokemon me, ActivePokemon o) {
		// Don't do anything if they're not actually attacking
		if (!isFighting(me.isPlayer()) || !isFront(me)) {
			return;
		}

		boolean success = false;
		
		me.startAttack(this);
		
		// HOLD IT RIGHT THERE! YOU MAY NOT BE ABLE TO ATTACK!
		if (ableToAttack(me, o)) {
			// Made it, suckah!
			printAttacking(me);
			
			// Check if the move actually hits!
			if (accuracyCheck(me, o)) {
				executeAttack(me, o);
				success = true;
			}
			else {
				Messages.add(me.getName() + "'s attack missed!");
				CrashDamageMove.invokeCrashDamageMove(this, me);
			}			
		}
		
		me.endAttack(o, success);

		// Can't use me and o in case there was a switch mid-turn
        Messages.add(new MessageUpdate().updatePokemon(this, player.front()));
        Messages.add(new MessageUpdate().updatePokemon(this, opponent.front()));
	}
	
	public void printAttacking(ActivePokemon p) {
		Messages.add((p.isPlayer() ? "" : "Enemy ") + p.getName() + " used " + p.getAttack().getName() + "!");
		p.getAttributes().setReducePP(true);
	}
	
	private void executeAttack(ActivePokemon me, ActivePokemon o) {
		me.getAttributes().count();

		boolean success = me.getAttack().apply(me, o, this);
		me.getAttributes().setLastMoveSucceeded(success);

		me.getMove().setUsed();
		me.getAttributes().decay();
	}
	
	public void addEffect(BattleEffect effect) {
		if (effect instanceof Weather) {
			weather = (Weather)effect;
			Messages.add(new MessageUpdate().withWeather(weather));
		}
		else {
			effects.add(effect);
		}
	}

	public List<BattleEffect> getEffects() {
		return effects;
	}

	public List<TeamEffect> getEffects(ActivePokemon teamMember) {
		return getEffects(teamMember.isPlayer());
	}
	
	public List<TeamEffect> getEffects(boolean isPlayer) {
		return isPlayer ? player.getEffects() : opponent.getEffects();
	}
	
	public List<Object> getEffectsList(ActivePokemon p, Object... additionalItems) {
		List<Object> list = new ArrayList<>();
		Collections.addAll(list, additionalItems);
		
		list.addAll(p.getAllEffects(this));
		list.addAll(getEffects(p));
		list.addAll(getEffects());
		list.add(weather);
		
		return list;
	}

	public Team getTrainer(ActivePokemon pokemon) {
		return getTrainer(pokemon.isPlayer());
	}

	public Team getTrainer(boolean isPlayer) {
		return isPlayer ? player : opponent;
	}

	public ActivePokemon getOtherPokemon(ActivePokemon pokemon) {
		return getOtherPokemon(pokemon.isPlayer());
	}
	
	// Returns the current Pokemon that is out on the team opposite to the one passed in
	public ActivePokemon getOtherPokemon(boolean isPlayer) {
		return isPlayer ? opponent.front() : player.front();
	}
	
	public boolean isWildBattle() {
		return opponent instanceof WildPokemon;
	}
	
	public int calculateDamage(ActivePokemon me, ActivePokemon o) {
		final Stat attacking;
		final Stat defending;
		switch (me.getAttack().getCategory()) {
			case PHYSICAL:
				attacking = Stat.ATTACK;
				defending = Stat.DEFENSE;
				break;
			case SPECIAL:
				attacking = Stat.SP_ATTACK;
				defending = Stat.SP_DEFENSE;
				break;
			default:
				Global.error("Invalid category " + me.getAttack().getCategory() + " for calculating damage");
				return -1;
		}

		int level = me.getLevel();
		int random = RandomUtils.getRandomInt(16) + 85;

		int power = me.getAttack().getPower(this, me, o);
		power *= getDamageModifier(me, o);
		
		int attackStat = Stat.getStat(attacking, me, o, this);
		int defenseStat = Stat.getStat(defending, o, me, this);
		
		double stab = TypeAdvantage.getSTAB(this, me);
		double adv = TypeAdvantage.getAdvantage(me, o, this);

		//		System.out.printf("%s %s %d %d %d %d %d %f %f %d%n",
//				me.getActualName(),
//				me.getAttack().getName(),
//				level,
//				power,
//				random,
//				attackStat,
//				defenseStat,
//				stab,
//				adv,
//				damage);
		
		return (int)Math.ceil(((((2*level/5.0 + 2)*attackStat*power/defenseStat)/50.0) + 2)*stab*adv*random/100.0);
	}

	public double getDamageModifier(ActivePokemon me, ActivePokemon o) {
		return PowerChangeEffect.getModifier(this, me, o)*OpponentPowerChangeEffect.getModifier(this, me, o);
	}

	// Crit yo pants
	private static final int[] CRITSICLES = { 16, 8, 4, 3, 2 };
	public boolean criticalHit(ActivePokemon me, ActivePokemon o) {
		if (CritBlockerEffect.checkBlocked(this, me, o)) {
			return false;
		}

		if (AlwaysCritEffect.defCritsies(this, me, o)) {
			return true;
		}

		// Increase crit stage and such
		int stage = 1;
		stage = CritStageEffect.updateCritStage(this, stage, me);
		stage = Math.min(stage, CRITSICLES.length); // Max it out, yo

		return RandomUtils.chanceTest(1, CRITSICLES[stage - 1]);
	}

	protected boolean accuracyCheck(ActivePokemon me, ActivePokemon o) {
		// Self-Target moves don't miss
		if (me.getAttack().isSelfTarget() && me.getAttack().isStatusMove()) {
			return true;
		}

		if (me.getAttack().isMoveType(MoveType.FIELD)) {
			return true;
		}
		
		// Effects that allow the user to bypass the accuracy check
		if (AccuracyBypassEffect.bypassAccuracyCheck(this, me, o)) {
			return true;
		}
		
		// Opponent effects that always allow the user to hit them
		if (OpponentAccuracyBypassEffect.bypassAccuracyCheck(this, me, o)) {
			return true;
		}
		
		// Semi-invulnerable target -- automatic miss (unless a previous condition was triggered)
		if (o.isSemiInvulnerable()) {
			return false;
		}
		
		int moveAccuracy = me.getAttack().getAccuracy(this, me, o);
		int accuracy = Stat.getStat(Stat.ACCURACY, me, o, this);
		int evasion = Stat.getStat(Stat.EVASION, o, me, this);
		
		return RandomUtils.chanceTest((int)(moveAccuracy*((double)accuracy/(double)evasion)));
	}
	
	// Returns true if the Pokemon is able to execute their turn by checking effects that have been casted upon them
	// This is where BeforeTurnEffects are handled
	protected boolean ableToAttack(ActivePokemon p, ActivePokemon opp) {
		// Dead Pokemon can't attack and it's not nice to attack a deady
		if (p.isFainted(this) || opp.isFainted(this)) {
			return false;
		}
		
		// Loop through all tha effects and do them checks
		if (BeforeTurnEffect.checkCannotAttack(p, opp, this)) {
			return false;
		}
		
		// Opponents effects that prevent you from attacking
		if (OpponentBeforeTurnEffect.checkCannotAttack(p, opp, this)) {
			return false;
		}
		
		// WOOOOOOOOOO
		return true;
	}

	public int getPriority(ActivePokemon p, Attack attack) {
		int priority = attack.getPriority(this, p);
		priority += PriorityChangeEffect.getModifier(this, p, attack);

//		System.out.println(attack.getName() + " Priority: " + priority);

		return priority;
	}
	
	// Returns the priority of the current action the player is performing
	private int getPriority(ActivePokemon p) {
		// They are attacking -- return the priority of the attack
		if (isFighting(p.isPlayer())) {
			return getPriority(p, p.getAttack());
		}
		
		return ((Trainer)getTrainer(p.isPlayer())).getAction().getPriority();
	}
	
	// Returns true if the player will be attacking first, and false if the opponent will be 
	private boolean speedPriority(ActivePokemon plyr, ActivePokemon opp) {

		// Higher priority always goes first -- Prankster increases the priority of status moves by one
		int pPriority = getPriority(plyr);
		int oPriority = getPriority(opp);

		if (pPriority != oPriority) {
			return pPriority > oPriority;
		}

		// TODO: Rewrite this shit it looks like ass
		// Quick Claw gives holder a 20% chance of striking first within its priority bracket
		boolean pQuick = plyr.isHoldingItem(this, ItemNamesies.QUICK_CLAW);
		boolean oQuick = opp.isHoldingItem(this, ItemNamesies.QUICK_CLAW);
		if (pQuick && !oQuick && RandomUtils.chanceTest(20)) {
			Messages.add(plyr.getName() + "'s " + ItemNamesies.QUICK_CLAW.getName() + " allowed it to strike first!");
			return true;
		}
		if (oQuick && !pQuick && RandomUtils.chanceTest(20)) {
			Messages.add(opp.getName() + "'s " + ItemNamesies.QUICK_CLAW.getName() + " allowed it to strike first!");
			return false;
		}		
		
		// Trick Room makes the slower Pokemon go first
		boolean reverse = hasEffect(EffectNamesies.TRICK_ROOM);
		
		// Pokemon that are stalling go last, if both are stalling, the slower one goes first
		boolean pStall = plyr.isStalling(this);
		boolean oStall = opp.isStalling(this);

		if (pStall && oStall) {
			reverse = true;
		}
		else if (pStall) {
			return false;
		}
		else if (oStall) {
			return true;
		}
		
		// Get the speeds of the Pokemon
		int pSpeed = Stat.getStat(Stat.SPEED, plyr, opp, this);
		int oSpeed = Stat.getStat(Stat.SPEED, opp, plyr, this);
		
		// Speeds are equal -- alternate
		if (pSpeed == oSpeed) {
			return turn%2 == 0;
		}
		
		// Return the faster Pokemon (or slower if reversed)
		return reverse ? oSpeed > pSpeed : oSpeed < pSpeed;
	}
}
