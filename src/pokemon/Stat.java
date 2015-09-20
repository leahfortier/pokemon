package pokemon;

import main.Global;
import battle.Battle;
import battle.effect.IgnoreStageEffect;
import battle.effect.StageChangingEffect;
import battle.effect.StatChangingEffect;
import battle.effect.StatSwitchingEffect;

public enum Stat 
{
	HP(0, "HP", "HP", -1, InBattle.NEVER, true), 
	ATTACK(1, "Attack", "Attack", 2, InBattle.BOTH, true),
	DEFENSE(2, "Defense", "Defense", 2, InBattle.BOTH, false), 
	SP_ATTACK(3, "Special Attack", "Sp. Attack", 2, InBattle.BOTH, true), 
	SP_DEFENSE(4, "Special Defense", "Sp. Defense", 2, InBattle.BOTH, false), 
	SPEED(5, "Speed", "Speed", 2, InBattle.BOTH, true), 
	ACCURACY(0, "Accuracy", "Accuracy", 3, InBattle.ONLY, true), 
	EVASION(6, "Evasion", "Evasion", 3, InBattle.ONLY, false);
	
	private int index;
	private String name;
	private String shortName;
	private double modifier;
	private InBattle onlyBattle;
	private boolean user;
	
	// Never -- The stat is not used in battle (HP)
	// Both -- used in and out of battle
	// Only -- only used in battle (Accuracy/Evasion)
	private static enum InBattle
	{
		NEVER, BOTH, ONLY
	}
	
	private Stat(int i, String n, String s, int m, InBattle b, boolean u)
	{	
		index = i;
		name = n;
		shortName = s;
		modifier = m;
		onlyBattle = b;
		user = u;
	}
	
	public int index()
	{
		return index;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getShortName()
	{
		return shortName;
	}
	
	public boolean user()
	{
		return user;
	}
	
	public static final int NUM_STATS = 6;
	public static final int NUM_BATTLE_STATS = 7;
	public static final int MAX_STAT_CHANGES = 6;
	public static final int MAX_EVS = 510;
	public static final int MAX_STAT_EVS = 255;
	
	public static final Stat[] STATS;
	static
	{
		STATS = new Stat[NUM_STATS];
		int i = 0;
		
		for (Stat s : Stat.values()) 
		{
			if (s.onlyBattle == InBattle.ONLY)
			{
				continue;
			}
			
			STATS[i++] = s;
		}
	}
	
	// Generates a new stat
	public static int getStat(int statIndex, int level, int baseStat, int IV, int EV, double natureVal)
	{
		if (statIndex == HP.index)
		{
			return (int)(((IV + 2*baseStat + (EV/4.0))*level/100.0) + 10 + level);
		}
		
		return (int)((((IV + 2*baseStat + (EV/4.0))*level/100.0) + 5)*natureVal);
	}
	
	// Gets the stat of a Pokemon during battle
	public static int getStat(Stat s, ActivePokemon p, ActivePokemon opp, Battle b)
	{
		// Effects that manipulate stats
		Object[] list;
		
		ActivePokemon attacking = s.user ? p : opp;
		list = b.getEffectsList(p, attacking.getAttack());
		
		s = (Stat)Battle.updateInvoke(0, list, StatSwitchingEffect.class, "switchStat", s);
		
		// Apply stage changes
		int stage = getStage(list, s, p, opp, b);
		int stat = s == EVASION || s == ACCURACY ? 100 : p.getStat(b, s);
		
//		int temp = stat;
		
		// Modify stat based off stage
		if (stage > 0) stat *= ((s.modifier + stage)/s.modifier);
	    else if (stage < 0) stat *= (s.modifier/(s.modifier - stage));
		
		ActivePokemon moldBreaker  = s.user ? null : opp;
		
		// Applies stat changes to each for each item in list
		stat = (int)Battle.updateInvoke(0, moldBreaker, list, StatChangingEffect.class, "modify", stat, p, opp, s, b);
		
//		System.out.println(p.getName() + " " + s.name + " Stat Change: " + temp + " -> " + stat);
		
		// Just to be safe
		stat = Math.max(1, stat);
		
		return stat;
	}
	
	private static int getStage(Object[] list, Stat s, ActivePokemon p, ActivePokemon opp, Battle b)
	{
		int stage = p.getStage(s.index);
		
//		int temp = stage;
		
		// Update the stage due to effects
		ActivePokemon moldBreaker = s.user ? null : opp;
		stage = (int)Battle.updateInvoke(0, moldBreaker, list, StageChangingEffect.class, "adjustStage", stage, s, p, opp, b);
		
//		int temp2 = stage;
		
		ActivePokemon attacking = s.user ? p : opp;
		
		// Effects that completely ignore stage changes
		list = new Object[] { opp.getAbility(), attacking.getAttack() };
		Object ignoreStage = Battle.checkInvoke(true, p, list, IgnoreStageEffect.class, "ignoreStage", s);
		if (ignoreStage != null)
		{
			stage = 0;
		}
		
		// Let's keep everything in bounds, okay!
		stage = Math.max(-1*MAX_STAT_CHANGES, Math.min(stage, MAX_STAT_CHANGES));
		
//		System.out.println(p.getName() + " " + s.getName() + " Stage: " + temp + " -> " + temp2 + " -> " + stage);
		
		return stage;
	}
	
	// Returns the corresponding Stat based on the index passed in
	public static Stat getStat(int index, boolean battle)
	{
		for (Stat s : values())
		{
			if (s.onlyBattle == InBattle.ONLY && !battle) continue;
			if (s.onlyBattle == InBattle.NEVER && battle) continue;
			
			if (s.index == index) return s;
		}
		
		Global.error("Incorrect stat index " + index);
		return null;
	}
}
