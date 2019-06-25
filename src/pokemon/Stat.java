package pokemon;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.InvokeInterfaces.OpponentIgnoreStageEffect;
import battle.effect.InvokeInterfaces.OpponentStatSwitchingEffect;
import battle.effect.InvokeInterfaces.StageChangingEffect;
import battle.effect.InvokeInterfaces.StatChangingEffect;
import battle.effect.InvokeInterfaces.StatModifyingEffect;
import battle.effect.InvokeInterfaces.StatSwitchingEffect;
import main.Global;

import java.util.List;

public enum Stat {
    HP(0, "HP", "HP", "HP", -1, InBattle.NEVER, true),
    ATTACK(1, "Attack", "Attack", "Atk", 2, InBattle.BOTH, true),
    DEFENSE(2, "Defense", "Defense", "Def", 2, InBattle.BOTH, false),
    SP_ATTACK(3, "Special Attack", "Sp. Attack", "SpA", 2, InBattle.BOTH, true),
    SP_DEFENSE(4, "Special Defense", "Sp. Defense", "SpD", 2, InBattle.BOTH, false),
    SPEED(5, "Speed", "Speed", "Spd", 2, InBattle.BOTH, true),
    ACCURACY(0, "Accuracy", "Accuracy", "Acc", 3, InBattle.ONLY, true),
    EVASION(6, "Evasion", "Evasion", "Eva", 3, InBattle.ONLY, false);

    public static final int NUM_STATS = 6;
    public static final int NUM_BATTLE_STATS = 7;
    public static final int MAX_STAT_CHANGES = 6;

    public static final List<Stat> STATS;
    public static final List<Stat> BATTLE_STATS;
    static {
        Stat[] stats = new Stat[NUM_STATS];
        Stat[] battleStats = new Stat[NUM_BATTLE_STATS];
        int statIndex = 0;
        int battleStatIndex = 0;

        for (Stat stat : Stat.values()) {
            switch (stat.onlyBattle) {
                case BOTH:
                    stats[statIndex++] = stat;
                    battleStats[battleStatIndex++] = stat;
                    break;
                case ONLY:
                    battleStats[battleStatIndex++] = stat;
                    break;
                case NEVER:
                    stats[statIndex++] = stat;
                    break;
            }
        }

        STATS = List.of(stats);
        BATTLE_STATS = List.of(battleStats);
    }

    private final int index;
    private final String name;
    private final String shortName;
    private final String shortestName;
    private final double modifier;
    private final InBattle onlyBattle;
    private final boolean user;

    Stat(int index, String name, String shortName, String shortestName, int modifier, InBattle onlyBattle, boolean user) {
        this.index = index;
        this.name = name;
        this.shortName = shortName;
        this.shortestName = shortestName;
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

    public String getShortestName() {
        return this.shortestName;
    }

    public boolean user() {
        return user;
    }

    // Gets the stat of a Pokemon during battle
    public static int getStat(Stat s, ActivePokemon p, Battle b) {
        ActivePokemon opp = b.getOtherPokemon(p);

        // Effects that manipulate stats
        s = StatSwitchingEffect.switchStat(b, p, s);
        s = OpponentStatSwitchingEffect.switchStat(b, opp, s);

        // Apply stage changes
        int stage = getStage(s, p, opp, b);
        int stat = s == EVASION || s == ACCURACY ? 100 : p.getStat(b, s);

        // Modify stat based off stage
        if (stage > 0) {
            stat *= (s.modifier + stage)/s.modifier;
        } else if (stage < 0) {
            stat *= s.modifier/(s.modifier - stage);
        }

        // Applies stat changes to each for each item in list
        stat *= StatModifyingEffect.getModifier(b, p, opp, s);
        stat = StatChangingEffect.modifyStat(b, p, opp, s, stat);

        // Just to be safe
        stat = Math.max(1, stat);

        return stat;
    }

    public static int getStage(Stat s, ActivePokemon stagePokemon, ActivePokemon otherPokemon, Battle b) {
        // Effects that completely ignore stage changes
        if (OpponentIgnoreStageEffect.checkIgnoreStage(b, stagePokemon, otherPokemon, s)) {
            return 0;
        }

        int stage = stagePokemon.getStage(s);

        // Update the stage due to effects
        stage += StageChangingEffect.getModifier(b, stagePokemon, otherPokemon, s);

        // Let's keep everything in bounds, okay!
        return Math.max(-1*MAX_STAT_CHANGES, Math.min(stage, MAX_STAT_CHANGES));
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

    // Never -- The stat is not used in battle (HP)
    // Both -- used in and out of battle
    // Only -- only used in battle (Accuracy/Evasion)
    private enum InBattle {
        NEVER,
        BOTH,
        ONLY,
    }
}
