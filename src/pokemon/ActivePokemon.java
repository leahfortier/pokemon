package pokemon;

import item.Item;
import item.Item.PowerItem;
import item.berry.Berry;
import item.berry.HealthTriggeredBerry;
import item.hold.HoldItem;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import main.Global;
import main.Type;
import pokemon.Evolution.EvolutionCheck;
import pokemon.PokemonInfo.WildHoldItem;
import trainer.CharacterData;
import trainer.Pokedex;
import trainer.Pokedex.PokedexStatus;
import battle.Attack;
import battle.Battle;
import battle.BattleAttributes;
import battle.Move;
import battle.effect.AbilityCondition;
import battle.effect.BracingEffect;
import battle.effect.Effect;
import battle.effect.Effect.CastSource;
import battle.effect.FaintEffect;
import battle.effect.GroundedEffect;
import battle.effect.IntegerCondition;
import battle.effect.ItemCondition;
import battle.effect.LevitationEffect;
import battle.effect.MoveListCondition;
import battle.effect.MultiTurnMove;
import battle.effect.OpponentTrappingEffect;
import battle.effect.PokemonEffect;
import battle.effect.StallingEffect;
import battle.effect.StatsCondition;
import battle.effect.Status;
import battle.effect.Status.StatusCondition;
import battle.effect.TeamEffect;
import battle.effect.TrappingEffect;
import battle.effect.TypeCondition;

public class ActivePokemon implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int MAX_LEVEL = 100;
	
	private static final String[][] characteristics = 
		{{"Loves to eat", "Proud of its power", "Sturdy body", "Highly curious", "Strong willed", "Likes to run"},
		 {"Takes plenty of siestas", "Likes to thrash about", "Capable of taking hits", "Mischievous", "Somewhat vain", "Alert to sounds"},
		 {"Nods off a lot", "A little quick tempered", "Highly persistent", "Thoroughly cunning", "Strongly defiant", "Impetuous and silly"},
		 {"Scatters things often", "Likes to fight", "Good endurance", "Often lost in thought", "Hates to lose", "Somewhat of a clown"},
		 {"Likes to relax", "Quick tempered", "Good perseverance", "Very finicky", "Somewhat stubborn", "Quick to flee"}};
	
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
	private Type hiddenType;
	private int hiddenPower;
	private boolean isEgg;
	private int eggSteps;

	public ActivePokemon(PokemonInfo p, int lev, boolean wild, boolean user)
	{
		pokemon = p;
		nickname = p.getName();
		level = lev;
		setIVs();
		nature = new Nature();
		setCharacteristic();
		EVs = new int[Stat.NUM_STATS];
		stats = new int[Stat.NUM_STATS];
		setStats();
		hp = stats[Stat.HP.index()];
		playerPokemon = user;
		attributes = new BattleAttributes();
		removeStatus();
		totalEXP = GrowthRate.getEXP(pokemon.getGrowthRate(), level);
		totalEXP += (int)(Math.random()*expToNextLevel());
		gender = Gender.getGender(pokemon.getMaleRatio());
		shiny = wild ? (int)(Math.random()*8192) == 13 : false;
		setMoves();
		ability = Ability.assign(pokemon);
		hiddenType = computeHiddenType();
		hiddenPower = computeHiddenPower();
		heldItem = wild ? WildHoldItem.getWildHoldItem(pokemon.getWildItems()) : (HoldItem)Item.noneItem();
		isEgg = false;
		eggSteps = 0;
	}
	
	// Constructor for Eggs
	public ActivePokemon(PokemonInfo p)
	{
		this(p, 1, false, true);
		isEgg = true;
		eggSteps = p.getEggSteps();
		nickname = "Egg";
	}
	
	public boolean isEgg()
	{
		return isEgg;
	}
	
	private boolean hatch()
	{	
		eggSteps--;
		
//		System.out.println(pokemon.getName() + " Egg Steps: " + eggSteps);
		return eggSteps <= 0;
	}
	
	public String getEggMessage()
	{
		if (!isEgg()) Global.error("Only Eggs can have egg messages.");
		
		if (eggSteps > 10*255) return "Wonder what's inside? It needs more time though.";
		else if (eggSteps > 5*255) return "It moves around inside sometimes. It must be close to hatching.";
		return "It's making sounds inside! It's going to hatch soon!";
	}
	
	public static void hatch(CharacterData player, List<ActivePokemon> team)
	{
		for (ActivePokemon p : team)
		{
			if (p.isEgg && p.hatch())
			{
				// TODO: Show hatch animation
				p.isEgg = false;
				p.nickname = p.pokemon.getName();
				player.getPokedex().setStatus(p, Pokedex.PokedexStatus.CAUGHT);
				break; // Only one hatch per step
			}
		}
	}
	
	private void setMoves()
	{
		moves = new ArrayList<Move>();
		TreeMap<Integer, List<String>> map = pokemon.getLevelUpMoves();
		for (Integer i : map.keySet())
		{
			if (i > level) continue;
			for (String s : map.get(i))
			{
				if (hasMove(s)) continue;
				moves.add(new Move(Attack.getAttack(s)));
			}
		}
		while (moves.size() > Move.MAX_MOVES) moves.remove(0);
	}
	
	public void setMoves(List<Move> list)
	{
		moves = list;
	}
	
	public void setShiny()
	{
		shiny = true;
	}
	
	// Random value between 0 and 31
	private void setIVs()
	{
		IVs = new int[Stat.NUM_STATS];
		for (int i = 0; i < IVs.length; i++) IVs[i] = (int)(Math.random()*32);
	}
	
	private void setCharacteristic()
	{
		int maxIndex = 0;
		for (int i = 1; i < IVs.length; i++)
		{
			if (IVs[i] > IVs[maxIndex])
			{
				maxIndex = i;
			}
		}
		
		characteristic = characteristics[IVs[maxIndex]%5][maxIndex];
	}
	
	private void setStats()
	{
		int prevHP = stats[Stat.HP.index()];
		
		stats = new int[Stat.NUM_STATS];
		for (int i = 0; i < stats.length; i++)
		{
			stats[i] = Stat.getStat(i, level, pokemon.getStat(i), IVs[i], EVs[i], nature.getNatureVal(i));
		}
		
		hp += stats[Stat.HP.index()]-prevHP;
	}
	
	private Type computeHiddenType()
	{
		return Type.getHiddenType(((IVs[Stat.HP.index()]%2 + 2*(IVs[Stat.ATTACK.index()]%2) 
				+ 4*(IVs[Stat.DEFENSE.index()]%2) + 8*(IVs[Stat.SPEED.index()]%2) 
				+ 16*(IVs[Stat.SP_ATTACK.index()]%2) + 32*(IVs[Stat.SP_DEFENSE.index()]%2))*15)/63);
	}
	
	private int computeHiddenPower()
	{
		return (IVs[Stat.HP.index()]%2 + 2*(IVs[Stat.ATTACK.index()]%2) 
				+ 4*(IVs[Stat.DEFENSE.index()]%2) + 8*(IVs[Stat.SPEED.index()]%2) 
				+ 16*(IVs[Stat.SP_ATTACK.index()]%2) + 32*(IVs[Stat.SP_DEFENSE.index()]%2)*40)/63 + 30;
	}
	
	public Type getHiddenType()
	{
		return hiddenType;
	}
	
	public int getHiddenPower()
	{
		return hiddenPower;
	}
	
	public String getCharacteristic()
	{
		return characteristic;
	}
	
	public int[] getStats()
	{
		return stats;
	}
	
	public int[] getIVs()
	{
		return IVs;
	}
	
	public int[] getEVs()
	{
		return EVs;
	}
	
	public int getStat(Stat s)
	{
		PokemonEffect e = getEffect("Transformed");
		if (e != null) return ((StatsCondition)e).getStat(s);
		return stats[s.index()];
	}
	
	public int getIV(int index)
	{
		return IVs[index];
	}
	
	public int getEV(int index)
	{
		return EVs[index];
	}
	
	public Nature getNature()
	{
		return nature;
	}
	
	public Ability getActualAbility()
	{
		return ability;
	}
	
	public Ability getAbility()
	{
		// Check if the Pokemon has had its ability changed during the battle
		PokemonEffect e = getEffect("ChangeAbility");
		if (e != null) return ((AbilityCondition)e).getAbility();
		return ability;
	}
	
	public int getStage(int index)
	{
		return attributes.getStage(index);
	}
	
	public Move getMove(int index)
	{
		return getMoves().get(index);
	}
	
	public List<Move> getMoves()
	{
		PokemonEffect mimic = getEffect("Mimic"), transformed = getEffect("Transformed");
		if (transformed != null) return ((MoveListCondition)transformed).getMoveList(this, moves);
		if (mimic != null) return ((MoveListCondition)mimic).getMoveList(this, moves);
		
		return moves;
	}
	
	public List<Move> getActualMoves()
	{
		return moves;
	}
	
	public int getTotalEXP()
	{
		return totalEXP;
	}
	
	public int expToNextLevel()
	{
		if (level == MAX_LEVEL) return 0;
		
		return GrowthRate.getEXP(pokemon.getGrowthRate(), level+1)-totalEXP;
	}
	
	public float expRatio()
	{
		return 1.0f-(float)expToNextLevel()/(GrowthRate.getEXP(pokemon.getGrowthRate(), level+1)-GrowthRate.getEXP(pokemon.getGrowthRate(), level));
	}
	
	public void gainEXP(Battle b, int gain, ActivePokemon dead)
	{
		boolean front = b.getPlayer().front() == this;
		
		// Add EXP
		totalEXP += gain;
		b.addMessage(nickname+" gained "+gain+" EXP points!");
		if (front) b.addMessage("", Math.min(1, expRatio()));
		
		// Add EVs
		Item i = getHeldItem(b);
		int[] vals = dead.getPokemonInfo().getGivenEVs();
		if (i instanceof PowerItem) vals = ((PowerItem)i).getEVs(vals);
		addEVs(vals);
		
		// Level up if applicable
		while (totalEXP >= GrowthRate.getEXP(pokemon.getGrowthRate(), level+1))
		{
			levelUp(b);
		}
	}

	private boolean levelUp(Battle b)
	{
		if (level == MAX_LEVEL) return false;
		
		boolean print = b != null, front = print && b.getPlayer().front() == this;
		
		// Grow to the next level
		level++;
		if (print) b.addMessage(nickname+" grew to level "+level+"!");
		if (print && front) b.addMessage("", level, Math.min(1, expRatio()));
		
		// Change stats 
		int[] prevStats = stats.clone(), gain = new int[Stat.NUM_STATS];
		setStats();
		for (int i = 0; i < Stat.NUM_STATS; i++) gain[i] = stats[i] - prevStats[i];
		if (print && front) b.addMessage("", hp, gain, stats, playerPokemon);
		
		// Learn new moves
		for (String s : pokemon.getMoves(level)) learnMove(b, s);
		
		// Maybe you'll evolve?!
		BaseEvolution ev = (BaseEvolution)pokemon.getEvolution().getEvolution(EvolutionCheck.LEVEL, this, null);
		if (ev != null) evolve(b, ev);
		
		return true;
	}
	
	public String evolve(Battle b, BaseEvolution ev)
	{
		if (getActualHeldItem() == Item.getItem("Everstone")) return "";
		
		boolean print = b != null, front = print && b.getPlayer().front() == this;
		boolean sameName = nickname.equals(pokemon.getName());
		
		ability = Ability.evolutionAssign(this, ev.getEvolution());
		
		String name = nickname;
		if (print) b.addMessage(nickname+" is evolving!");			
		pokemon = ev.getEvolution();
		if (print) b.getPlayer().getPokedex().setStatus(this, PokedexStatus.CAUGHT);
		
		// Set name if it was not given a nickname
		if (sameName) nickname = pokemon.getName();
		if (print && front) b.addMessage("", nickname, playerPokemon);
		if (print && front) b.addMessage("", getType(), playerPokemon);
		
		// Change stats
		int[] prevStats = stats.clone(), gain = new int[Stat.NUM_STATS];
		setStats();
		for (int i = 0; i < Stat.NUM_STATS; i++) gain[i] = stats[i] - prevStats[i];
		
		if (print && front) b.addMessage("", hp, stats[Stat.HP.index()], playerPokemon);
		if (print && front) b.addMessage("", pokemon, shiny, true, playerPokemon);
		
		String message = name+" evolved into "+pokemon.getName()+"!";
		if (print) b.addMessage(message);
		if (print && front) b.addMessage("", hp, gain, stats, playerPokemon);
		
		// Learn new moves
		List<String> levelMoves = pokemon.getMoves(level);
		for (String s : levelMoves) learnMove(b, s);
		
		return message;
	}
	
	private void learnMove(Battle b, String attackName)
	{
		if (hasMove(attackName)) return;
		
		Move m = new Move(Attack.getAttack(attackName));
		if (moves.size() < Move.MAX_MOVES)
		{
			if (b != null) b.addMessage(nickname + " learned " + m.getAttack().getName() + "!");
			addMove(b, m, moves.size() - 1);
			return;
		}
		
		if (b == null) return;
		b.addMessage(" ", this, m);
		b.addMessage(nickname + " did not learn " + m.getAttack().getName() + ".");
		
		for (int i = 0; i < moves.size(); i++)
		{
			b.addMessage(nickname + " forgot how to use " + moves.get(i).getAttack().getName() + "...");	
		}
		b.addMessage("...and " + nickname + " learned " + m.getAttack().getName() + "!");
	}
	
	public void addMove(Battle b, Move m, int index)
	{
		if (moves.size() < Move.MAX_MOVES) moves.add(m);
		else moves.set(index, m);
		
		BaseEvolution ev = (BaseEvolution)pokemon.getEvolution().getEvolution(EvolutionCheck.MOVE, this, null);
		if (ev != null) evolve(b, ev);
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public void callNewMove(Battle b, ActivePokemon opp, Move m)
	{
		Move temp = getMove();
		setMove(m);
		b.printAttacking(this);
		getAttack().apply(this, opp, b);
		setMove(temp);
	}
	
	public boolean breaksTheMold()
	{
		switch (getAbility().getName())
		{
			case "Mold Breaker":
			case "Turboblaze":
			case "Teravolt":
				return true;
		}
		return false;
	}
	
	public boolean canFight()
	{
		return !hasStatus(StatusCondition.FAINTED) && !isEgg();
	}
	
	// Returns if the Pokemon is stalling -- that is that it will move last within its priority bracket
	public boolean isStalling(Battle b)
	{
		return getAbility() instanceof StallingEffect || getHeldItem(b) instanceof StallingEffect;
	}
	
	public boolean hasAbility(String a)
	{
		Ability.getAbility(a);
		return getAbility().getName().equals(a);
	}
	
	public void setStatus(Status s)
	{
		status = s;
	}
	
	public void addEffect(PokemonEffect e)
	{
		attributes.addEffect(e);
	}
	
	public void setMove(Move m)
	{
		attributes.setMove(m);
	}
	
	public Move getMove()
	{
		return attributes.getMove();
	}
	
	public Attack getAttack()
	{
		return attributes.getMove().getAttack();
	}
	
	// Returns whether or not this Pokemon knows this move already
	public boolean hasMove(String name)
	{
		for (Move m : getMoves())
		{
			if (m.getAttack().getName().equals(name)) return true;
		}
		return false;
	}
	
	public Gender getGender()
	{
		return gender;
	}
	
	public boolean isSemiInvulnerable()
	{
		if (getMove() == null) return false;
		return !getMove().isReady() && ((MultiTurnMove)getAttack()).semiInvulnerability();
	}
	
	private int totalEVs()
	{
		int sum = 0;
		for (int i = 0; i < EVs.length; i++) sum += EVs[i];
		return sum;
	}
	
	// Adds Effort Values to a Pokemon, returns true if they were successfully added
	public boolean addEVs(int[] vals)
	{
		if (totalEVs() == Stat.MAX_EVS) return false;
		
		boolean added = false;
		for (int i = 0; i < EVs.length; i++)
		{
			if (vals[i] > 0 && EVs[i] < Stat.MAX_STAT_EVS)
			{
				added = true;
				EVs[i] = Math.min(Stat.MAX_STAT_EVS, EVs[i]+vals[i]); // Don't exceed stat EV amount
				
				// Don't exceed total EV amount
				if (totalEVs() > Stat.MAX_EVS) 
				{
					EVs[i] -= (Stat.MAX_EVS-totalEVs());
					break;
				}
			}
			else if (vals[i] < 0 && EVs[i] > 0)
			{
				added = true;
				EVs[i] = Math.max(0, EVs[i]+vals[i]); // Don't drop below zero
			}
		}
		
		setStats();
		return added;
	}
	
	public Type[] getActualType()
	{
		return pokemon.getType();
	}
	
	public Type[] getType() 
	{
		// Check if the Pokemon has had its type changed during the battle
		PokemonEffect e = getEffect("ChangeType");
		if (e != null) return ((TypeCondition)e).getType();
		e = getEffect("Transformed");
		if (e != null) return ((TypeCondition)e).getType();
		return pokemon.getType();
	}
	
	public boolean isType(Type type)
	{
		Type[] types = getType();
		return types[0].equals(type) || types[1].equals(type); 
	}
	
	public int getHP()
	{
		return hp;
	}
	
	public void setHP(int amount)
	{
		hp = Math.min(getStat(Stat.HP), Math.max(0, amount));
	}
	
	public boolean fullHealth()
	{
		return hp == getStat(Stat.HP);
	}
	
	public double getHPRatio()
	{
		return (double)hp/getStat(Stat.HP);
	}
	
	public Color getHPColor()
	{
		return Global.getHPColor(getHPRatio());
	}
	
	public String getName()
	{
		return nickname;
	}
	
	public void setNickname(String nickity)
	{
		nickname = nickity;
	}
	
	public BattleAttributes getAttributes()
	{
		return attributes;
	}
	
	public boolean user()
	{
		return playerPokemon;
	}
	
	public void resetAttributes()
	{
		attributes = new BattleAttributes();
	}
	
	public void setCaught()
	{
		playerPokemon = true;
	}
	
	public boolean isFainted(Battle b)
	{
		// We have already checked that this Pokemon is fainted -- don't print/apply effects more than once
		if (hasStatus(StatusCondition.FAINTED))
		{
			if (hp == 0) return true;
			Global.error("Pokemon should only have the Fainted Status Condition when HP is zero.");
		}
		
		// Deady
		if (hp == 0)
		{
			Status.die(this);
			b.addMessage("", hp, playerPokemon);
			b.addMessage(nickname+" fainted!", StatusCondition.FAINTED, playerPokemon);
			
			ActivePokemon murderer = b.getOtherPokemon(playerPokemon);
			
			// Apply effects which occur when the user faints 
			for (PokemonEffect e : attributes.getEffects())
			{
				if (!e.isActive() || !(e instanceof FaintEffect)) continue;
				((FaintEffect)e).deathwish(b, this, murderer);
			}
			if (murderer.getAbility() instanceof FaintEffect) ((FaintEffect)murderer.getAbility()).deathwish(b, this, murderer);
			
			b.getEffects(playerPokemon).add(TeamEffect.getEffect("DeadAlly").newInstance());
			
			return true;	
		}
		
		// Still kickin' it
		return false;
	}
	
	// Returns the empty string if the Pokemon can switch, and the appropriate fail message if they cannot
	public String canEscape(Battle b)
	{
		// Shed Shell always allows escape
		if (isHoldingItem(b, "Shed Shell")) return "";
		
		// Check if the user is under an effect that prevents escape
		for (PokemonEffect e : getEffects())
		{
			if (!e.isActive()) continue;
			if (e instanceof TrappingEffect) return nickname+" cannot be recalled at this time!";
		}
		
		// The opponent has an ability that prevents escape
		Ability oppAbility = b.getOtherPokemon(playerPokemon).getAbility(); 
		if (oppAbility instanceof OpponentTrappingEffect)
		{
			OpponentTrappingEffect trapping = (OpponentTrappingEffect)oppAbility;
			if (trapping.isTrapped(b, this)) return trapping.trappingMessage(this, b.getOtherPokemon(user()));
		}
		
		return "";
	}
	
	public boolean hasEffect(String effect)
	{
		return attributes.hasEffect(effect);
	}
	
	// Returns null if the Pokemon is not under the effects of the input effect, otherwise returns the Condition
	public PokemonEffect getEffect(String effect)
	{
		return attributes.getEffect(effect);
	}
	
	public List<PokemonEffect> getEffects()
	{
		return attributes.getEffects();
	}
	
	public void modifyStages(Battle b, ActivePokemon modifier, int[] mod, CastSource source)
	{
		for (int i = 0; i < mod.length; i++)
		{
			if (mod[i] == 0) continue;
			attributes.modifyStage(modifier, this, mod[i], Stat.getStat(i, true), b, source);
		}
	}
	
	public Status getStatus()
	{
		return status;
	}
	
	// Returns whether or not the Pokemon is afflicted with a status condition
	public boolean hasStatus()
	{
		return status.getType() != StatusCondition.NONE;
	}
	
	public boolean hasStatus(StatusCondition type)
	{
		return status.getType() == type;
	}
	
	// Sets the Pokemon's status condition to be None
	public void removeStatus()
	{
		Status.removeStatus(this);
		attributes.removeEffect("Nightmare");
		attributes.removeEffect("BadPoison");
	}
	
	// Returns null if the Pokemon is not bracing, and the associated effect if it is
	private BracingEffect bracing(Battle b, boolean fullHealth)
	{
		BracingEffect e = (BracingEffect)getEffect("Bracing");
		if (e != null) return e;
		
		Ability a = getAbility();
		if (a instanceof BracingEffect && ((BracingEffect)a).isBracing(b, this, fullHealth)
				&& !b.getOtherPokemon(playerPokemon).breaksTheMold()) return (BracingEffect)a;
		
		Item i = getHeldItem(b);
		if (i instanceof BracingEffect && ((BracingEffect)i).isBracing(b, this, fullHealth)) return (BracingEffect)i;
		
		return null;
	}
	
	// Reduces hp by amount, returns the actual amount of hp that was reduced
	public int reduceHealth(Battle b, int amount)
	{
		if (amount == 0) return 0;
		
		// Substitute absorbs the damage instead of the Pokemon
		IntegerCondition e = (IntegerCondition)getEffect("Substitute");
		if (e != null)
		{
			e.decrease(amount);
			if (e.getAmount() <= 0)
			{
				b.addMessage("The substitute broke!");
				attributes.removeEffect("Substitute");
			}
			return 0;
		}
		
		boolean fullHealth = fullHealth();
		
		// Reduce HP, record damage, and check if fainted
		int prev = hp, taken = prev - (hp = Math.max(0, hp-amount));
		attributes.takeDamage(taken);
		
		// Enduring the hit
		if (hp == 0)
		{
			BracingEffect brace = bracing(b, fullHealth);
			if (brace != null)
			{
				taken -= heal(1);
				b.addMessage("", hp, playerPokemon);
				b.addMessage(brace.braceMessage(this));				
			}
		}
		
		if (isFainted(b)) return taken;
		b.addMessage("", hp, playerPokemon);
		
		// Check if the Pokemon fainted and also handle Focus Punch
		if (hasEffect("Focusing"))
		{
			b.addMessage(nickname+" lost its focus and couldn't move!");
			attributes.removeEffect("Focusing");
			addEffect(PokemonEffect.getEffect("Flinch"));		
		}
		
		// Health Triggered Berries
		Item i = getHeldItem(b);
		if (i instanceof HealthTriggeredBerry)
		{
			HealthTriggeredBerry h  = (HealthTriggeredBerry)i;
			double healthRatio = getHPRatio();
			if ((healthRatio <= h.healthTriggerRatio() || (healthRatio <= .5 && hasAbility("Gluttony"))))
			{
				if (h.useHealthTriggerBerry(b, this))
				{
					consumeItem(b);
				}
			}
		}
		
		return taken;
	}
	
	// Reduces the amount of health that corresponds to fraction of the pokemon's total health and returns this amount
	public int reduceHealthFraction(Battle b, double fraction)
	{
		return reduceHealth(b, (int)Math.max(stats[Stat.HP.index()]*fraction, 1));
	}
	
	// Restores hp by amount, returns the actual amount of hp that was restored
	public int heal(int amount)
	{
		// Dead Pokemon can't heal
		if (hasStatus(StatusCondition.FAINTED)) return 0;
		
		int prev = hp;
		hp = Math.min(getStat(Stat.HP), hp+amount);
		return hp - prev;
	}
	
	// Restores the amount of health that corresponds to fraction of the pokemon's total health and returns this amount
	public int healHealthFraction(double fraction)
	{
		return heal((int)Math.max(getStat(Stat.HP)*fraction, 1));
	}
	
	public int fullyHeal()
	{
		removeStatus();
		return healHealthFraction(1);
	}
	
	// Heals the Pokemon by damage amount. It is assume damage has already been dealt to the victim
	public void sapHealth(ActivePokemon victim, int amount, Battle b, boolean print)
	{
		if (victim.hasAbility("Liquid Ooze"))
		{
			b.addMessage(victim.getName()+"'s Liquid Ooze caused "+nickname+" to lose health instead!");
			reduceHealth(b, amount);
			return;
		}
		
		if (isHoldingItem(b, "Big Root")) amount *= 1.3;
		if (print) b.addMessage(victim.getName()+"'s health was sapped!");
		if (!hasEffect("HealBlock")) heal(amount);
		b.addMessage("", victim.hp, victim.user());
		b.addMessage("", hp, user());
	}
	
	// Returns true if the Pokemon is currently levitating for any reason
	public boolean isLevitating(Battle b)
	{
		boolean levitation = false;
		
		// Check effects that cause user to be grounded/levitating -- Grounded effect overrules Levitation effect
		List<Object> effects = b.getEffectsList(this);
		for (Object e : effects)
		{
			if (e instanceof Effect && !((Effect)e).isActive()) continue;
			if (e instanceof GroundedEffect) return false;
			if (e instanceof LevitationEffect) levitation = true;
		}
		
		if (levitation) return true;
		
		// Stupid motherfucking Mold Breaker not allowing me to make Levitate a Levitation effect, fuck you Mold Breaker.
		if (hasAbility("Levitate") && !b.getOtherPokemon(playerPokemon).breaksTheMold()) return true;
		
		// Flyahs gon' Fly
		return isType(Type.FLYING);
	}

	public void giveItem(HoldItem i)
	{
		heldItem = i;
	}
	
	public void removeItem()
	{
		heldItem = (HoldItem)Item.noneItem();
	}
	
	public void consumeItem(Battle b)
	{
		Item consumed = getHeldItem(b);
		PokemonEffect.getEffect("ConsumedItem").cast(b, this, this, CastSource.HELD_ITEM, false);
		
		ActivePokemon other = b.getOtherPokemon(playerPokemon); 
		if (other.hasAbility("Pickup") && !other.isHoldingItem(b))
		{
			other.giveItem((HoldItem)consumed);
			b.addMessage(other.getName()+" picked up "+getName()+"'s "+consumed.getName()+"!");
		}
	}
	
	public Item getActualHeldItem()
	{
		return (Item)heldItem;
	}
	
	public Item getHeldItem(Battle b)
	{
		if (hasAbility("Klutz")) return Item.noneItem();
		if (b.hasEffect("MagicRoom")) return Item.noneItem();
		if (hasEffect("Embargo")) return Item.noneItem();
		
		// Check if the Pokemon has had its item changed during the battle
		PokemonEffect e = getEffect("ChangeItem");
		Item item = e == null ? (Item)heldItem : ((ItemCondition)e).getItem();
		
		if (item instanceof Berry && b.getOtherPokemon(user()).hasAbility("Unnerve")) return Item.noneItem();
		
		return item;
	}
	
	public boolean isHoldingItem(Battle b, String itemName)
	{
		if (b == null) return getActualHeldItem() == Item.getItem(itemName);
		return getHeldItem(b) == Item.getItem(itemName);
	}
	
	public boolean isHoldingItem(Battle b)
	{
		return getHeldItem(b) != Item.noneItem();
	}
	
	public boolean isShiny()
	{
		return shiny;
	}
	
	public PokemonInfo getPokemonInfo()
	{
		return pokemon;
	}
	
	public boolean isPokemon(String name)
	{
		return pokemon.getName().equals(name);
	}
	
	public double getWeight(Battle b)
	{
		int halfAmount = hasAbility("Light Metal") && !b.getOtherPokemon(playerPokemon).breaksTheMold() ? 1 : 0;
		if (isHoldingItem(b, "Float Stone")) halfAmount++;
		PokemonEffect e = getEffect("HalfWeight");
		if (e != null) halfAmount += ((IntegerCondition)e).getAmount();
		return pokemon.getWeight()/Math.pow(2, halfAmount);
	}
}