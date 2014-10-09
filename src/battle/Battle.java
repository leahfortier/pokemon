package battle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import main.Global;
import main.Namesies;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonInfo;
import pokemon.Stat;
import trainer.CharacterData;
import trainer.Opponent;
import trainer.Pokedex.PokedexStatus;
import trainer.Team;
import trainer.Trainer;
import trainer.Trainer.Action;
import trainer.WildPokemon;
import battle.Attack.Category;
import battle.Attack.MoveType;
import battle.MessageUpdate.Update;
import battle.effect.AccuracyBypassEffect;
import battle.effect.BattleEffect;
import battle.effect.BeforeTurnEffect;
import battle.effect.CrashDamageMove;
import battle.effect.CritBlockerEffect;
import battle.effect.CritStageEffect;
import battle.effect.DefiniteEscape;
import battle.effect.Effect;
import battle.effect.EndTurnEffect;
import battle.effect.EntryEffect;
import battle.effect.MultiTurnMove;
import battle.effect.OpponentAccuracyBypassEffect;
import battle.effect.OpponentBeforeTurnEffect;
import battle.effect.OpponentPowerChangeEffect;
import battle.effect.PokemonEffect;
import battle.effect.PowerChangeEffect;
import battle.effect.PriorityChangeEffect;
import battle.effect.Status.StatusCondition;
import battle.effect.TeamEffect;
import battle.effect.Weather;

public class Battle 
{
	private CharacterData player;
	private Opponent opponent; // SO OBJECT-ORIENTED
	private Weather weather;
	private List<BattleEffect> effects;
	private int turn;
	private boolean firstAttacking;
	private boolean reduce;
	private int escapeAttempts;
	private ArrayDeque<MessageUpdate> messages;
	private String winGlobal;
	
	public Battle(CharacterData p, Opponent o)
	{
		player = p;
		opponent = o;
		effects = new ArrayList<BattleEffect>();
		player.resetEffects();
		opponent.resetEffects();
		turn = 0;
		escapeAttempts = 0;
		firstAttacking = false;
		messages = new ArrayDeque<>();
		weather = Weather.getEffect(Namesies.CLEAR_SKIES_EFFECT);
		player.enterBattle();
		
		if (opponent instanceof Trainer)
		{
			((Trainer) opponent).enterBattle();
			addMessage(((Trainer)opponent).getName() + " wants to fight!");
			((Trainer)opponent).setAction(Action.FIGHT);
			enterBattle(opponent.front());
		}
		else
		{
			enterBattle(opponent.front(), "Wild " + opponent.front().getName() + " appeared!");
		}
		
		enterBattle(player.front());
	}
	
	public Battle(CharacterData p, Opponent o, String win)
	{
		this(p, o);
		winGlobal = win;
	}
	
	public CharacterData getPlayer()
	{
		return player;
	}
	
	public Opponent getOpponent()
	{
		return opponent;
	}
	
	public String getWinGlobal()
	{
		return winGlobal;
	}
	
	public ArrayDeque<MessageUpdate> getMessages()
	{
		return messages;
	}
	
	public Weather getWeather()
	{
		return weather;
	}
	
	public int getTurn()
	{
		return turn;
	}
	
	public void addMessage(String message)
	{
		messages.add(new MessageUpdate(message));
	}
	
	public void addMessage(String message, int hp, boolean target)
	{
		messages.add(new MessageUpdate(message, hp, target));
	}
	
	public void addMessage(String message, int hp, int maxHP, boolean target)
	{
		messages.add(new MessageUpdate(message, hp, maxHP, target));
	}
	
	public void addMessage(String message, int hp, int[] statGains, int[] stats, boolean target)
	{
		messages.add(new MessageUpdate(message, hp, statGains, stats, target));
	}
	
	public void addMessage(String message, StatusCondition status, boolean target)
	{
		messages.add(new MessageUpdate(message, status, target));
	}
	
	public void addMessage(String message, PokemonInfo pokemon, boolean shiny, boolean animation, boolean target)
	{
		messages.add(new MessageUpdate(message, pokemon, shiny, animation, target));
	}
	
	public void addMessage(String message, Type[] type, boolean target)
	{
		messages.add(new MessageUpdate(message, type, target));
	}
	
	public void addMessage(String message, ActivePokemon p)
	{
		messages.add(new MessageUpdate(message, p, this));
	}
	
	public void addMessage(String message, Update update)
	{
		messages.add(new MessageUpdate(message, update));
	}
	
	public void addMessage(String message, int duration)
	{
		messages.add(new MessageUpdate(message, duration));
	}
	
	public void addMessage(String message, float expRatio)
	{
		messages.add(new MessageUpdate(message, expRatio));
	}
	
	public void addMessage(String message, int level, float expRatio)
	{
		messages.add(new MessageUpdate(message, level, expRatio));
	}
	
	public void addMessage(String message, String name, boolean target)
	{
		messages.add(new MessageUpdate(message, name, target));
	}
	
	public void addMessage(String message, Gender gender, boolean target)
	{
		messages.add(new MessageUpdate(message, gender, target));
	}
	
	public void addMessage(String message, ActivePokemon p, Move move)
	{
		messages.add(new MessageUpdate(message, p, move));
	}
	
	public boolean hasEffect(Namesies effect)
	{
		return Effect.hasEffect(effects, effect);
	}
	
	public void fight()
	{
		startTurn();

		boolean playerFirst = speedPriority(player.front(), opponent.front());
		
		firstAttacking = true;
		if (playerFirst) executionSolution(player.front(), opponent.front());
		else executionSolution(opponent.front(), player.front());
		
		firstAttacking = false;
		if (playerFirst) executionSolution(opponent.front(), player.front());
		else executionSolution(player.front(), opponent.front());
		
		endTurn();
		
		deadUser();
		deadOpponent();
		
		for (PokemonEffect e : player.front().getEffects()) System.out.println("P " + e);
		for (PokemonEffect e : opponent.front().getEffects()) System.out.println("O " + e);
		
		for (TeamEffect e : player.getEffects()) System.out.println("P " + e);
		for (TeamEffect e : opponent.getEffects()) System.out.println("O " + e);
		
		for (BattleEffect e : getEffects()) System.out.println("B " + e);
		if (weather.namesies() != Namesies.CLEAR_SKIES_EFFECT) System.out.println("W " + weather);
		
		for (int i = 0; i < 7; i++) System.out.print((i == 0 ? player.front().getName() + " " : "") + player.front().getStage(i) + (i == 6 ? "\n" : " "));
		for (int i = 0; i < 7; i++) System.out.print((i == 0 ? opponent.front().getName() + " " : "") + opponent.front().getStage(i) + (i == 6 ? "\n" : " "));
		
		System.out.println(player.front().getName() + " " + player.front().getAbility().getName() + " " + player.front().getHeldItem(this).getName());
		System.out.println(opponent.front().getName() + " " + opponent.front().getAbility().getName() + " " + opponent.front().getHeldItem(this).getName());
	}
	
	// Handles events that occur at the beginning of each turn. Returns the two Pokemon currently in battle
	private void startTurn()
	{
		ActivePokemon plyr = player.front(), opp = opponent.front(); 
		opp.setMove(Move.selectOpponentMove(this, opp));

		turn++;
		plyr.getAttributes().resetDamageTaken();
		opp.getAttributes().resetDamageTaken();
		
		// Fucking focus punch
		if (isFighting(true)) plyr.getAttack().startTurn(this, plyr);
		if (isFighting(false)) opp.getAttack().startTurn(this, opp);
	}
	
	public boolean isFirstAttack()
	{
		return firstAttacking;
	}
	
	// If the trainer selected an attack, this will return true - Wild Pokemon will always return true
	// It will return false if the trainer tried to run, switched Pokemon, or used an item
	private boolean isFighting(boolean team)
	{
		Team trainer = getTrainer(team);
		return trainer instanceof WildPokemon || ((Trainer)trainer).getAction() == Action.FIGHT;
	}
	
	private void endTurn()
	{
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
	
	private void deadUser()
	{
		// Front Pokemon is still functioning
		if (!player.front().isFainted(this)) return;
		
		// Dead Front Pokemon, but you still have others to spare -- force a switch
		if (!player.blackout())
		{
			addMessage("What Pokemon would you like to switch to?", Update.FORCE_SWITCH);
			return;
		}
		
		// Blackout -- you're fucked
		addMessage(player.getName() + " is out of usable Pok\u00e9mon! " + player.getName() + " blacked out!");
		
		// Sucks to suck
		if (opponent instanceof Trainer)
		{
			Trainer opp = (Trainer)opponent;
			int cashMoney = player.sucksToSuck(opp.getDatCashMoney());
			addMessage(opp.getName() + " rummaged through the pockets of your passed out body and stole " + cashMoney + " pokedollars!!!");
		}
		
		player.healAll();
		player.teleportToPokeCenter();
		addMessage(" ", Update.EXIT_BATTLE);
	}
	
	private void deadOpponent()
	{
		ActivePokemon dead = opponent.front();
		
		// YOU'RE FINE
		if (!dead.isFainted(this)) 
			return; 
		
		// Gain dat EXP
		player.gainEXP(dead, this); 
		
		// You have achieved total victory
		if (opponent.blackout())
		{
			player.winBattle(this, opponent);
			
			// WE'RE DONE HERE
			addMessage(" ", Update.EXIT_BATTLE);
			return;
		}
		
		// We know this is not a wild battle anymore and I don't feel like casting so much
		Trainer opp = (Trainer)opponent;
		
		// They still have some Pokes left
		opp.switchToRandom();
		enterBattle(opp.front());
	}
	
	public void enterBattle(ActivePokemon enterer)
	{
		String enterMessage = "";
		if (enterer.user()) enterMessage = "Go! " + enterer.getName() + "!";
		else if (opponent instanceof Trainer) enterMessage = ((Trainer)opponent).getName() + " sent out " + enterer.getName() + "!";
		
		enterBattle(enterer, enterMessage, true);
	}
	
	public void enterBattle(ActivePokemon enterer, String enterMessage)
	{
		enterBattle(enterer, enterMessage, true);
	}
	
	public void enterBattle(ActivePokemon enterer, String enterMessage, boolean reset)
	{
		if (enterer.isEgg()) 
		{
			Global.error("Eggs can't battle!!!");
		}
		
		// Document sighting in the Pokedex
		if (!enterer.user()) 
		{
			player.getPokedex().setStatus(enterer, PokedexStatus.SEEN, isWildBattle() ? player.getRouteName() : "");
		}
		
		// TODO: I don't think this is sending the message properly, switched to Squirtle and had Bulbasaur's type colors at first
		addMessage(enterMessage, enterer);
		
		if (reset) 
		{
			enterer.resetAttributes();
		}
		
		// TODO: Test the invoke
		enterer.getAttributes().setUsed(true);
		Object[] list = getEffectsList(enterer);
		Global.invoke(list, EntryEffect.class, "enter", this, enterer);
		
		getTrainer(!enterer.user()).resetUsed();
	}
	
	public boolean runAway()
	{
		escapeAttempts++;
		
		if (opponent instanceof Trainer)
		{
			addMessage("There's no running from a trainer battle!");
			return false;
		}
		
		ActivePokemon plyr = player.front(), opp = opponent.front();
		
		String trapMessage = plyr.canEscape(this);
		if (trapMessage.length() > 0)
		{
			addMessage(trapMessage);
			return false;
		}
		
		int pSpeed = Stat.getStat(Stat.SPEED, plyr, opp, this);
		int oSpeed = Stat.getStat(Stat.SPEED, opp, plyr, this);
		
		int val = (pSpeed*32)/(oSpeed/4) + 30*escapeAttempts;
		if (Math.random()*256 < val 
				|| plyr.getAbility() instanceof DefiniteEscape 
				|| plyr.getHeldItem(this) instanceof DefiniteEscape)
		{
			addMessage("Got away safely!");
			addMessage(" ", Update.EXIT_BATTLE);
			return true;
		}
		
		addMessage("Can't escape!");
		player.performAction(this, Action.RUN);
		return false;
	}

	private void decrementEffects(List<? extends Effect> effects, ActivePokemon p)
	{
		for (int i = 0; i < effects.size(); i++)
		{
			Effect e = effects.get(i);
			
			boolean inactive = !e.isActive();
			if (!inactive)
			{
				e.decrement(this, p);
				inactive = !e.isActive() && !e.nextTurnSubside();  
			}
			
			if (inactive)
			{
				effects.remove(i--);
				e.subside(this, p);
				
				// I think this is pretty much just for Future Sight...
				if (p != null && p.isFainted(this))
				{
					return;
				}
			}
		}
	}
	
	private void decrementWeather()
	{
		if (!weather.isActive())
		{
			addMessage(weather.getSubsideMessage(player.front()));
			weather = Weather.getEffect(Namesies.CLEAR_SKIES_EFFECT);
			return;
		}
		
		weather.applyEndTurn(player.front(), this);
		weather.decrement(this, player.front());
	}
	
	private void endTurnPokemonEffects(ActivePokemon me)
	{
		// Effects that need to be checked
		List<Object> list = new ArrayList<>();
		list.addAll(me.getEffects());
		list.addAll(getEffects(me.user()));
		list.addAll(getEffects());
		list.add(me.getStatus());
		list.add(me.getAbility());
		list.add(me.getHeldItem(this));
		
		Global.invoke(this, me, null, list.toArray(), EndTurnEffect.class, "applyEndTurn", me, this);
		
		me.isFainted(this);
		
		// No longer the first turn anymore
		me.getAttributes().setFirstTurn(false);
	}
	
	private void executionSolution(ActivePokemon me, ActivePokemon o)
	{
		// Don't do anything if they're not actually attacking
		if (!isFighting(me.user()))
		{
			return;
		}

		boolean success = false;
		reduce = false;
		
		me.startAttack(this, o);
		
		// HOLD IT RIGHT THERE! YOU MAY NOT BE ABLE TO ATTACK!
		if (ableToAttack(me, o))
		{
			// Made it, suckah!
			printAttacking(me);
			
			// Check if the move actually hits!
			if (accuracyCheck(me, o))
			{
				executeAttack(me, o);
				success = true;
			}
			else
			{
				addMessage(me.getName() + "'s attack missed!");
				Global.invoke(new Object[] {me.getAttack()}, CrashDamageMove.class, "crash", this, me);
			}			
		}
		
		me.endAttack(this, o, success, reduce);
		
		// Hopefully this doesn't mess anything up but update type at the end of each attack TODO: I'm pretty sure it will -- try with Illusion or something
		addMessage("", me.getType(this), me.user());
		addMessage("", o.getType(this), o.user());
	}
	
	public void printAttacking(ActivePokemon p)
	{
		addMessage((p.user() ? "" : "Enemy ") + p.getName() + " used " + p.getAttack().getName() + "!");
		reduce = true;
	}
	
	private void executeAttack(ActivePokemon me, ActivePokemon o)
	{
		me.getAttributes().count();
		me.getAttack().apply(me, o, this);
		me.getMove().use();
		me.getAttributes().decay();
	}
	
	public void addEffect(BattleEffect e)
	{
		if (e instanceof Weather) weather = (Weather)e; 
		else effects.add(e);
	}
	
	public List<BattleEffect> getEffects()
	{
		return effects;
	}	
	
	public List<TeamEffect> getEffects(boolean team)
	{
		return team ? player.getEffects() : opponent.getEffects();
	}
	
	public Object[] getEffectsList(ActivePokemon p, Object... additionalItems)
	{
		List<Object> list = new ArrayList<>();
		list.addAll(p.getEffects());
		list.addAll(getEffects(p.user()));
		list.addAll(getEffects());
		list.add(p.getStatus());
		list.add(p.getAbility());
		list.add(p.getHeldItem(this));
		list.add(weather);
		
		for (Object additionalItem : additionalItems)
		{
			list.add(additionalItem);
		}
		
		return list.toArray();
	}
	
	public Team getTrainer(boolean team)
	{
		return team ? player : opponent;
	}
	
	// Returns the current Pokemon that is out on the team opposite to the one passed in
	public ActivePokemon getOtherPokemon(boolean team)
	{
		return team ? opponent.front() : player.front();
	}
	
	public boolean isWildBattle()
	{
		return opponent instanceof WildPokemon;
	}
	
	public int damageCalc(ActivePokemon me, ActivePokemon o)
	{
		int level = me.getLevel();
		int power = me.getAttackPower();
		int random = (int)(Math.random()*16) + 85;
		
		Stat attacking, defending;
		if (me.getAttack().getCategory() == Category.PHYSICAL)
		{
			attacking = Stat.ATTACK;
			defending = Stat.DEFENSE;
		}
		else
		{
			attacking = Stat.SP_ATTACK;
			defending = Stat.SP_DEFENSE;
		}
		
		int attackStat = Stat.getStat(attacking, me, o, this);
		int defenseStat = Stat.getStat(defending, o, me, this);
		
		double stab = Type.getSTAB(this, me);
		double adv = Type.getAdvantage(me, o, this);
		
		int damage = (int)Math.ceil(((((2*level/5.0 + 2)*attackStat*power/defenseStat)/50.0) + 2)*stab*adv*random/100.0);
		
		System.out.printf("%s %s %d %d %d %d %d %f %f %d%n", me.getName(), me.getAttack().getName(), level, power, random, attackStat, defenseStat, stab, adv, damage);
		
		damage *= getDamageModifier(me, o); 
		damage *= criticalHit(me, o);
		
		return damage;
	}
	
	private double getDamageModifier(ActivePokemon me, ActivePokemon o)
	{
		// User effects that effect user power
		Object[] list = getEffectsList(me);
		double modifier = Global.multiplyInvoke(1, list, PowerChangeEffect.class, "getMultiplier", this, me, o);
		
		// Opponent effects that effects user power
		list = getEffectsList(o);
		modifier = Global.multiplyInvoke(modifier, me, list, OpponentPowerChangeEffect.class, "getOpponentMultiplier", this, me, o);
		
//		System.out.println(me.getName() + " Modifier: " + modifier);
		return modifier;
	}
	
	private static int[] critsicles = { 16, 8, 4, 3, 2 };
	private int criticalHit(ActivePokemon me, ActivePokemon o)
	{
		Object[] listsies = this.getEffectsList(o, me.getAttack());
		Object blockCrits = Global.checkInvoke(true, me, listsies, CritBlockerEffect.class, "blockCrits");
		if (blockCrits != null)
		{
			return 1;
		}
		
		// Increase crit stage and such
		int stage = 1;
		listsies = this.getEffectsList(me);
		stage = (int)Global.updateInvoke(0, listsies, CritStageEffect.class, "increaseCritStage", stage, me);
		stage = Math.min(stage, critsicles.length); // Max it out, yo
		
		boolean crit = me.getAttack().isMoveType(MoveType.ALWAYS_CRIT) || Math.random()*critsicles[stage - 1] < 1;
		
		// Crit yo pants
		if (crit)
		{
			addMessage("It's a critical hit!!");
			if (o.hasAbility(Namesies.ANGER_POINT_ABILITY))
			{
				addMessage(o.getName() + "'s " + Namesies.ANGER_POINT_ABILITY.getName() + " raised its attack to the max!");
				o.getAttributes().setStage(Stat.ATTACK.index(), Stat.MAX_STAT_CHANGES);
			}
			
			return me.hasAbility(Namesies.SNIPER_ABILITY) ? 3 : 2;
		}
		
		return 1;
	}
	
	public boolean accuracyCheck(ActivePokemon me, ActivePokemon o)
	{
		// Self-Target moves don't miss
		if (me.getAttack().isSelfTarget() && me.getAttack().getCategory() == Category.STATUS) 
		{
			return true;
		}
		
		// Effects that allow the user to bypass the accuracy check
		Object[] invokees = this.getEffectsList(me, me.getAttack());
		Object bypass = Global.checkInvoke(true, invokees, AccuracyBypassEffect.class, "bypassAccuracy", this, me, o);
		if (bypass != null)
		{
			return true;
		}
		
		// Opponent effects that always allow the user to hit them
		invokees = this.getEffectsList(o);
		bypass = Global.checkInvoke(true, invokees, OpponentAccuracyBypassEffect.class, "opponentBypassAccuracy", this, me, o);
		if (bypass != null)
		{
			return true;
		}
		
		// Semi-invulnerable target -- automatic miss (unless a previous condition was triggered)
		if (o.isSemiInvulnerable()) 
		{
			return false;
		}
		
		int moveAccuracy = me.getAttack().getAccuracy(this, me, o);
		int accuracy = Stat.getStat(Stat.ACCURACY, me, o, this);
		int evasion = Stat.getStat(Stat.EVASION, o, me, this);
		
		return Math.random()*100 < moveAccuracy*((double)accuracy/(double)evasion);
	}
	
	// Returns true if the Pokemon is able to execute their turn by checking effects that have been casted upon them
	// This is where BeforeTurnEffects are handled
	private boolean ableToAttack(ActivePokemon p, ActivePokemon opp)
	{
		// Dead Pokemon can't attack and it's not nice to attack a deady
		if (p.isFainted(this) || opp.isFainted(this))
		{
			return false;
		}
		
		// Loop through all tha effects and do them checks
		Object[] invokees = getEffectsList(p);
		
		// False because we're checking if they 'cannot attack' from the 'canAttack' method
		Object cannotAttack = Global.checkInvoke(false, this, p, opp, invokees, BeforeTurnEffect.class, "canAttack", p, opp, this);
		if (cannotAttack != null)
		{
			return false;
		}
		
		// Opponents effects that prevent you from attacking
		invokees = getEffectsList(opp);
		cannotAttack = Global.checkInvoke(false, this, p, opp, p, invokees, OpponentBeforeTurnEffect.class, "opposingCanAttack", p, opp, this);
		if (cannotAttack != null)
		{
			return false;
		}
		
		// Multi-turn Moves
		if (!p.getMove().isReady())
		{
			((MultiTurnMove)p.getAttack()).charge(p, this);
			return false;
		}
		
		// WOOOOOOOOOO
		return true;
	}
	
	// Returns the priority of the current action the player is performing
	private int getPriority(ActivePokemon p)
	{
		// They are attacking -- return the priority of the attack
		if (isFighting(p.user()))
		{
			int priority = p.getAttack().getPriority(this, p);
			
			Object[] invokees = this.getEffectsList(p);
			priority = (int)Global.updateInvoke(2, invokees, PriorityChangeEffect.class, "changePriority", this, p, priority);
			
//			System.out.println(p.getAttack().getName() + " Priority: " + priority);
			
			return priority;
		}
		
		return ((Trainer)getTrainer(p.user())).getAction().getPriority();
	}
	
	// Returns true if the player will be attacking first, and false if the opponent will be 
	private boolean speedPriority(ActivePokemon plyr, ActivePokemon opp)
	{
		// Higher priority always goes first -- Prankster increases the priority of status moves by one
		int pPriority = getPriority(plyr), oPriority = getPriority(opp);
		if (pPriority != oPriority) return pPriority > oPriority;
		
		// Quick Claw gives holder a 20% chance of striking first within its priority bracket
		boolean pQuick = plyr.isHoldingItem(this, Namesies.QUICK_CLAW_ITEM), oQuick = opp.isHoldingItem(this, Namesies.QUICK_CLAW_ITEM);
		if (pQuick && !oQuick && Math.random() < .2)
		{
			addMessage(plyr.getName() + "'s " + Namesies.QUICK_CLAW_ITEM.getName() + " allowed it to strike first!");
			return true;
		}
		if (oQuick && !pQuick && Math.random() < .2)
		{
			addMessage(opp.getName() + "'s " + Namesies.QUICK_CLAW_ITEM.getName() + " allowed it to strike first!");
			return false;
		}		
		
		// Trick Room makes the slower Pokemon go first
		boolean reverse = hasEffect(Namesies.TRICK_ROOM_EFFECT);
		
		// Pokemon that are stalling go last, if both are stalling, the slower one goes first
		boolean pStall = plyr.isStalling(this), oStall = opp.isStalling(this);
		if (pStall && oStall) reverse = true;
		else if (pStall) return false;
		else if (oStall) return true;
		
		// Get the speeds of the Pokemon
		int pSpeed = Stat.getStat(Stat.SPEED, plyr, opp, this);
		int oSpeed = Stat.getStat(Stat.SPEED, opp, plyr, this);
		
		// Speeds are equal -- alternate
		if (pSpeed == oSpeed) return turn%2 == 0;
		
		// Return the faster Pokemon (or slower if reversed)
		return reverse ? oSpeed > pSpeed : oSpeed < pSpeed;
	}
}
