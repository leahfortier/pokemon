package pokemon;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.OpponentIgnoreStageEffect;
import battle.effect.generic.EffectInterfaces.OpponentStatSwitchingEffect;
import battle.effect.generic.EffectInterfaces.StageChangingEffect;
import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import battle.effect.generic.EffectInterfaces.StatModifyingEffect;
import battle.effect.generic.EffectInterfaces.StatSwitchingEffect;
import main.Global;
import util.RandomUtils;

public enum Stat {
	HP(0, "HP", "HP", -1, InBattle.NEVER, true), 
	ATTACK(1, "Attack", "Attack", 2, InBattle.BOTH, true),
	DEFENSE(2, "Defense", "Defense", 2, InBattle.BOTH, false), 
	SP_ATTACK(3, "Special Attack", "Sp. Attack", 2, InBattle.BOTH, true), 
	SP_DEFENSE(4, "Special Defense", "Sp. Defense", 2, InBattle.BOTH, false), 
	SPEED(5, "Speed", "Speed", 2, InBattle.BOTH, true), 
	ACCURACY(0, "Accuracy", "Accuracy", 3, InBattle.ONLY, true), 
	EVASION(6, "Evasion", "Evasion", 3, InBattle.ONLY, false);
	
	private final int index;
	private final String name;
	private final String shortName;
	private final double modifier;
	private final InBattle onlyBattle;
	private final boolean user;
	
	// Never -- The stat is not used in battle (HP)
	// Both -- used in and out of battle
	// Only -- only used in battle (Accuracy/Evasion)
	private enum InBattle {
		NEVER,
		BOTH,
		ONLY,
	}

	Stat(int index, String name, String shortName, int modifier, InBattle onlyBattle, boolean user) {
		this.index = index;
		this.name = name;
		this.shortName = shortName;
		this.modifier = modifier;
		this.onlyBattle = onlyBattle;
		this.user = user;
	}
	
	public int index() {
		return index;
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public boolean user() {
		return user;
	}
	
	public static final int NUM_STATS = 6;
	public static final int NUM_BATTLE_STATS = 7;
	public static final int MAX_STAT_CHANGES = 6;
	public static final int MAX_EVS = 510;
	public static final int MAX_STAT_EVS = 255;
	public static final int MAX_IV = 31;
	
	public static final Stat[] STATS;
	static {
		STATS = new Stat[NUM_STATS];
		int i = 0;
		
		for (Stat stat : Stat.values()) {
			if (stat.onlyBattle == InBattle.ONLY) {
				continue;
			}
			
			STATS[i++] = stat;
		}
	}
	
	// Generates a new stat
	public static int getStat(int statIndex, int level, int baseStat, int IV, int EV, double natureVal) {
		if (statIndex == HP.index) {
			return (int)(((IV + 2*baseStat + (EV/4.0))*level/100.0) + 10 + level);
		}
		
		return (int)((((IV + 2*baseStat + (EV/4.0))*level/100.0) + 5)*natureVal);
	}
	
	// Gets the stat of a Pokemon during battle
	public static int getStat(Stat s, ActivePokemon p, ActivePokemon opp, Battle b) {

		// Effects that manipulate stats
		s = StatSwitchingEffect.switchStat(b, p, opp, s);
		s = OpponentStatSwitchingEffect.switchStat(b, opp, s);

		// Apply stage changes
		int stage = getStage(s, p, opp, b);
		int stat = s == EVASION || s == ACCURACY ? 100 : p.getStat(b, s);
		
//		int temp = stat;
		
		// Modify stat based off stage
		if (stage > 0) {
			stat *= ((s.modifier + stage)/s.modifier);
		}
	    else if (stage < 0) {
			stat *= (s.modifier/(s.modifier - stage));
		}

		// Applies stat changes to each for each item in list
		stat *= StatModifyingEffect.getModifier(b, p, opp, s);
		stat = StatChangingEffect.modifyStat(b, p, opp, s, stat);

//		System.out.println(p.getName() + " " + s.name + " Stat Change: " + temp + " -> " + stat);
		
		// Just to be safe
		stat = Math.max(1, stat);
		
		return stat;
	}

	// TODO: Rename these pokemon names because they are srsly confusing the shit out of me
	public static int getStage(Stat s, ActivePokemon p, ActivePokemon opp, Battle b) {

		// Effects that completely ignore stage changes
		if (OpponentIgnoreStageEffect.checkIgnoreStage(b, p, opp, s)) {
			return 0;
		}

		int stage = p.getStage(s.index);

//		int temp = stage;

		// Update the stage due to effects
		stage += StageChangingEffect.getModifier(b, p, opp, s);

//		int temp2 = stage;
		
		// Let's keep everything in bounds, okay!
		stage = Math.max(-1*MAX_STAT_CHANGES, Math.min(stage, MAX_STAT_CHANGES));
		
//		System.out.println(p.getName() + " " + s.getName() + " Stage: " + temp + " -> " + temp2 + " -> " + stage);
		
		return stage;
	}
	
	// Returns the corresponding Stat based on the index passed in
	public static Stat getStat(int index, boolean battle) {
		for (Stat s : values()) {
			if ((s.onlyBattle == InBattle.ONLY && !battle) ||
					(s.onlyBattle == InBattle.NEVER && battle)) {
				continue;
			}
			
			if (s.index == index) {
				return s;
			}
		}
		
		Global.error("Incorrect stat index " + index);
		return HP; // Because I'm sick of NPE warnings and the above line does a system exit
	}

	public static int getRandomIv() {
		return RandomUtils.getRandomInt(MAX_IV + 1);
	}
}
