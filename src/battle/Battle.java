package battle;

import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.DefiniteEscape;
import battle.effect.attack.MultiTurnMove;
import battle.effect.generic.BattleEffect;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectInterfaces.AccuracyBypassEffect;
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
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import battle.effect.generic.Weather;
import item.ItemNamesies;
import main.Game;
import main.Global;
import main.Type;
import map.TerrainType;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import message.Messages.MessageState;
import pattern.action.UpdateMatcher;
import pokemon.ActivePokemon;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import trainer.CharacterData;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.Team;
import trainer.Trainer;
import trainer.Trainer.Action;
import trainer.WildPokemon;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Battle {
	private CharacterData player;
	private Opponent opponent; // SO OBJECT-ORIENTED

	private Weather weather;
	private List<BattleEffect> effects;

	private int turn;
	private boolean firstAttacking;
	private boolean reduce;
	private int escapeAttempts;

	private UpdateMatcher npcUpdateInteraction;

	private TerrainType baseTerrain;
	private TerrainType currentTerrain;
	
	public Battle(Opponent o) {
        Messages.clearMessages(MessageState.FIGHTY_FIGHT);
        Messages.setMessageState(MessageState.FIGHTY_FIGHT);
		Messages.addMessage(new MessageUpdate("", Update.ENTER_BATTLE));

		player = Game.getPlayer();
		opponent = o;
		effects = new ArrayList<>();
		player.resetEffects();
		opponent.resetEffects();
		turn = 0;
		escapeAttempts = 0;
		firstAttacking = false;
		weather = (Weather)EffectNamesies.CLEAR_SKIES.getEffect();
		player.enterBattle();

		if (opponent instanceof Trainer) {
			((Trainer) opponent).enterBattle();
			Messages.addMessage(((Trainer)opponent).getName() + " wants to fight!");
			((Trainer)opponent).setAction(Action.FIGHT);
			enterBattle(opponent.front());
		}
		else {
			enterBattle(opponent.front(), "Wild " + opponent.front().getName() + " appeared!");
		}

		enterBattle(player.front());
	}

	public Battle(EnemyTrainer npcTrainer, UpdateMatcher npcUpdateInteraction) {
		this(npcTrainer);
		this.npcUpdateInteraction = npcUpdateInteraction;
	}

	public CharacterData getPlayer() {
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

	public void setTerrainType(TerrainType terrainType, boolean base) {
		if (base) {
			this.baseTerrain = terrainType;
		}

		this.currentTerrain = terrainType;
	}

	public void resetTerrain() {
		this.currentTerrain = baseTerrain;
	}

	public boolean hasEffect(EffectNamesies effect) {
		return Effect.hasEffect(effects, effect);
	}

	public void fight() {
		startTurn();

		boolean playerFirst = speedPriority(player.front(), opponent.front());

		final ActivePokemon attackFirst;
		final ActivePokemon attackSecond;

		if (playerFirst) {
			attackFirst = player.front();
			attackSecond = opponent.front();
		} else {
			attackFirst = opponent.front();
			attackSecond = player.front();
		}

		// First turn
		firstAttacking = true;
		executionSolution(attackFirst, attackSecond);

		// Second turn
		firstAttacking = false;
		executionSolution(attackSecond, attackFirst);

		endTurn();

		deadUser();
		deadOpponent();

		printShit();
	}

	private void printShit() {
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

		if (weather.namesies() != EffectNamesies.CLEAR_SKIES) {
			System.out.println("W " + weather);
		}

		for (int i = 0; i < 7; i++) {
			System.out.print((i == 0 ? player.front().getActualName() + " " : "") + player.front().getStage(i) + (i == 6 ? "\n" : " "));
		}

		for (int i = 0; i < 7; i++) {
			System.out.print((i == 0 ? opponent.front().getActualName() + " " : "") + opponent.front().getStage(i) + (i == 6 ? "\n" : " "));
		}

		System.out.println(player.front().getActualName() + " " + player.front().getAbility().getName() + " " + player.front().getHeldItem(this).getName());
		System.out.println(opponent.front().getActualName() + " " + opponent.front().getAbility().getName() + " " + opponent.front().getHeldItem(this).getName());
	}

	// Handles events that occur at the beginning of each turn. Returns the two Pokemon currently in battle
	private void startTurn() {
		ActivePokemon plyr = player.front();
		ActivePokemon opp = opponent.front();

		opp.setMove(Move.selectOpponentMove(this, opp));

		turn++;
		plyr.getAttributes().resetDamageTaken();
		opp.getAttributes().resetDamageTaken();

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
		return trainer instanceof WildPokemon || ((Trainer)trainer).getAction() == Action.FIGHT;
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
	}

	private void deadUser() {
		// Front Pokemon is still functioning
		if (!player.front().isFainted(this)) {
			return;
		}

		// Dead Front Pokemon, but you still have others to spare -- force a switch
		if (!player.blackout()) {
			Messages.addMessage("What Pokemon would you like to switch to?", Update.FORCE_SWITCH);
			return;
		}

		// Blackout -- you're fucked
		Messages.addMessage(player.getName() + " is out of usable Pok\u00e9mon! " + player.getName() + " blacked out!");

		// Sucks to suck
		if (opponent instanceof Trainer) {
			Trainer opp = (Trainer)opponent;
			int cashMoney = player.sucksToSuck(opp.getDatCashMoney());
			Messages.addMessage(opp.getName() + " rummaged through the pockets of your passed out body and stole " + cashMoney + " pokedollars!!!");
		}

		player.healAll();
		player.teleportToPokeCenter();
		Messages.clearMessages(MessageState.MAPPITY_MAP);
		Messages.addMessage(" ", Update.EXIT_BATTLE);
	}

	private void deadOpponent() {
		ActivePokemon dead = opponent.front();

		// YOU'RE FINE
		if (!dead.isFainted(this)) {
			return;
		}

		// Gain dat EXP
		player.gainEXP(dead, this);

		// You have achieved total victory
		if (opponent.blackout()) {
			player.winBattle(this, opponent);

			// WE'RE DONE HERE
			Messages.addMessage(" ", Update.EXIT_BATTLE);
			return;
		}

		// We know this is not a wild battle anymore and I don't feel like casting so much
		Trainer opp = (Trainer)opponent;

		// They still have some Pokes left
		opp.switchToRandom();
		enterBattle(opp.front());
	}

	public void enterBattle(ActivePokemon enterer) {
		NameChanger.setNameChanges(this, enterer);

		String enterMessage = "";
		if (enterer.user()) {
			enterMessage = "Go! " + enterer.getName() + "!";
		}
		else if (opponent instanceof Trainer) {
			enterMessage = ((Trainer)opponent).getName() + " sent out " + enterer.getName() + "!";
		}

		enterBattle(enterer, enterMessage, true);
	}

	public void enterBattle(ActivePokemon enterer, String enterMessage)
	{
		enterBattle(enterer, enterMessage, true);
	}

	public void enterBattle(ActivePokemon enterer, String enterMessage, boolean reset) {
		if (enterer.isEgg()) {
			Global.error("Eggs can't battle!!!");
		}

		// Document sighting in the Pokedex
		if (!enterer.user()) {
			player.getPokedex().setSeen(enterer, isWildBattle());
		}

		if (reset) {
			enterer.resetAttributes();
		}

		Messages.addMessage(enterMessage, this, enterer, true);

		enterer.getAttributes().setUsed(true);
		EntryEffect.invokeEntryEffect(this, enterer);

		getTrainer(!enterer.user()).resetUsed();
	}

	public boolean runAway() {
		escapeAttempts++;

		if (opponent instanceof Trainer) {
			Messages.addMessage("There's no running from a trainer battle!");
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
			Messages.addMessage("Got away safely!");
			Messages.addMessage(" ", Update.EXIT_BATTLE);
			return true;
		}

		Messages.addMessage("Can't escape!");
		player.performAction(this, Action.RUN);
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
			Messages.addMessage(weather.getSubsideMessage(player.front()));
			weather = (Weather)EffectNamesies.CLEAR_SKIES.getEffect();
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
	
	private void executionSolution(ActivePokemon me, ActivePokemon o) {
		// Don't do anything if they're not actually attacking
		if (!isFighting(me.user())) {
			return;
		}

		boolean success = false;
		reduce = false;
		
		me.startAttack(this, o);
		
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
				Messages.addMessage(me.getName() + "'s attack missed!");
				CrashDamageMove.invokeCrashDamageMove(this, me);
			}			
		}
		
		me.endAttack(o, success, reduce);
	}
	
	public void printAttacking(ActivePokemon p) {
		Messages.addMessage((p.user() ? "" : "Enemy ") + p.getName() + " used " + p.getAttack().getName() + "!");
		reduce = true;
	}
	
	private void executeAttack(ActivePokemon me, ActivePokemon o) {
		me.getAttributes().count();
		me.getAttack().apply(me, o, this);
		me.getMove().use();
		me.getAttributes().decay();
	}
	
	public void addEffect(BattleEffect effect) {
		if (effect instanceof Weather) {
			weather = (Weather)effect;
		}
		else {
			effects.add(effect);
		}
	}
	
	public List<BattleEffect> getEffects() {
		return effects;
	}	
	
	public List<TeamEffect> getEffects(boolean team) {
		return team ? player.getEffects() : opponent.getEffects();
	}
	
	public List<Object> getEffectsList(ActivePokemon p, Object... additionalItems) {
		List<Object> list = new ArrayList<>();
		Collections.addAll(list, additionalItems);
		
		list.addAll(p.getAllEffects(this));
		list.addAll(getEffects(p.user()));
		list.addAll(getEffects());
		list.add(weather);
		
		return list;
	}
	
	public Team getTrainer(boolean team) {
		return team ? player : opponent;
	}
	
	// Returns the current Pokemon that is out on the team opposite to the one passed in
	public ActivePokemon getOtherPokemon(boolean team) {
		return team ? opponent.front() : player.front();
	}
	
	public boolean isWildBattle() {
		return opponent instanceof WildPokemon;
	}
	
	public int calculateDamage(ActivePokemon me, ActivePokemon o) {
		int level = me.getLevel();
		int power = me.getAttackPower();
		int random = RandomUtils.getRandomInt(16) + 85;
		
		final Stat attacking;
		final Stat defending;
		if (me.getAttack().getCategory() == MoveCategory.PHYSICAL) {
			attacking = Stat.ATTACK;
			defending = Stat.DEFENSE;
		}
		else {
			attacking = Stat.SP_ATTACK;
			defending = Stat.SP_DEFENSE;
		}
		
		int attackStat = Stat.getStat(attacking, me, o, this);
		int defenseStat = Stat.getStat(defending, o, me, this);
		
		double stab = Type.getSTAB(this, me);
		double adv = Type.getAdvantage(me, o, this);
		
		int damage = (int)Math.ceil(((((2*level/5.0 + 2)*attackStat*power/defenseStat)/50.0) + 2)*stab*adv*random/100.0);
		
		System.out.printf("%s %s %d %d %d %d %d %f %f %d%n",
				me.getActualName(),
				me.getAttack().getName(),
				level,
				power,
				random,
				attackStat,
				defenseStat,
				stab,
				adv,
				damage);
		
		damage *= getDamageModifier(me, o); 
		damage *= criticalHit(me, o);
		
		return damage;
	}

	private double getDamageModifier(ActivePokemon me, ActivePokemon o) {
		double modifier = 1;
		modifier = PowerChangeEffect.updateModifier(modifier, this, me, o);
		modifier = OpponentPowerChangeEffect.updateModifier(modifier, this, me, o);
		
//		System.out.println(me.getName() + " Modifier: " + modifier);
		return modifier;
	}
	
	private static final int[] CRITSICLES = { 16, 8, 4, 3, 2 };
	private int criticalHit(ActivePokemon me, ActivePokemon o) {
		if (CritBlockerEffect.checkBlocked(this, me, o)) {
			return 1;
		}
		
		// Increase crit stage and such
		int stage = 1;
		stage = CritStageEffect.updateCritStage(this, stage, me);
		stage = Math.min(stage, CRITSICLES.length); // Max it out, yo
		
		boolean crit = me.getAttack().isMoveType(MoveType.ALWAYS_CRIT) || RandomUtils.chanceTest(1, CRITSICLES[stage - 1]);
		
		// Crit yo pants
		if (crit) {
			Messages.addMessage("It's a critical hit!!");
			if (o.hasAbility(AbilityNamesies.ANGER_POINT)) {
				Messages.addMessage(o.getName() + "'s " + AbilityNamesies.ANGER_POINT.getName() + " raised its attack to the max!");
				o.getAttributes().setStage(Stat.ATTACK.index(), Stat.MAX_STAT_CHANGES);
			}
			
			return me.hasAbility(AbilityNamesies.SNIPER) ? 3 : 2;
		}
		
		return 1;
	}
	
	private boolean accuracyCheck(ActivePokemon me, ActivePokemon o) {
		// Self-Target moves don't miss
		if (me.getAttack().isSelfTarget() && me.getAttack().getCategory() == MoveCategory.STATUS) {
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
	private boolean ableToAttack(ActivePokemon p, ActivePokemon opp) {
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

		// TODO: Make static method inside MultiTurnMove
		// Multi-turn Moves
		if (!p.getMove().isReady()) {
			((MultiTurnMove)p.getAttack()).charge(p, this);
			return false;
		}
		
		// WOOOOOOOOOO
		return true;
	}
	
	// Returns the priority of the current action the player is performing
	private int getPriority(ActivePokemon p) {
		// They are attacking -- return the priority of the attack
		if (isFighting(p.user())) {
			int priority = p.getAttack().getPriority(this, p);
			priority = PriorityChangeEffect.updatePriority(this, p, priority);
			
//			System.out.println(p.getAttack().getName() + " Priority: " + priority);
			
			return priority;
		}
		
		return ((Trainer)getTrainer(p.user())).getAction().getPriority();
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
			Messages.addMessage(plyr.getName() + "'s " + ItemNamesies.QUICK_CLAW.getName() + " allowed it to strike first!");
			return true;
		}
		if (oQuick && !pQuick && RandomUtils.chanceTest(20)) {
			Messages.addMessage(opp.getName() + "'s " + ItemNamesies.QUICK_CLAW.getName() + " allowed it to strike first!");
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
