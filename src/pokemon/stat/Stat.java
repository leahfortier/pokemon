package pokemon.stat;

import battle.ActivePokemon;
import battle.Battle;
import battle.stages.Stages;
import battle.effect.InvokeInterfaces.IgnoreStageEffect;
import battle.effect.InvokeInterfaces.OpponentIgnoreStageEffect;
import battle.effect.InvokeInterfaces.OpponentStatSwitchingEffect;
import battle.effect.InvokeInterfaces.StageChangingEffect;
import battle.effect.InvokeInterfaces.StatChangingEffect;
import battle.effect.InvokeInterfaces.StatModifyingEffect;
import battle.effect.InvokeInterfaces.StatSwitchingEffect;
import main.Global;

import java.util.List;

public enum Stat {
    HP(0, "HP", "HP", "HP", -1, InBattle.NEVER, User.BOTH),
    ATTACK(1, "Attack", "Attack", "Atk", 2, InBattle.BOTH, User.ATTACKING),
    DEFENSE(2, "Defense", "Defense", "Def", 2, InBattle.BOTH, User.DEFENDING),
    SP_ATTACK(3, "Special Attack", "Sp. Attack", "SpA", 2, InBattle.BOTH, User.ATTACKING),
    SP_DEFENSE(4, "Special Defense", "Sp. Defense", "SpD", 2, InBattle.BOTH, User.DEFENDING),
    SPEED(5, "Speed", "Speed", "Spd", 2, InBattle.BOTH, User.BOTH),
    ACCURACY(0, "Accuracy", "Accuracy", "Acc", 3, InBattle.ONLY, User.ATTACKING),
    EVASION(6, "Evasion", "Evasion", "Eva", 3, InBattle.ONLY, User.DEFENDING);

    public static final int NUM_STATS = 6;
    public static final int NUM_BATTLE_STATS = 7;

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
    private final User user;

    Stat(int index, String name, String shortName, String shortestName, int modifier, InBattle onlyBattle, User user) {
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

    public User user() {
        return user;
    }

    // Includes stats which are not exclusively attacking stats like HP or Speedz
    public boolean isAttacking() {
        return user.isAttacking();
    }

    // Includes stats which are not exclusively defending stats like HP or Speed
    public boolean isDefending() {
        return user.isDefending();
    }

    public boolean isAttackingStat() {
        return this == ATTACK || this == SP_ATTACK;
    }

    // Gets the stat value only taking the raw stage into account
    public int getBasicStat(Battle b, ActivePokemon p) {
        return this.getStat(b, p, b.getOtherPokemon(p), false);
    }

    // Returns the modifier based ONLY on the stage
    public double getStageStatModifier(int stage) {
        if (stage > 0) {
            return (this.modifier + stage)/this.modifier;
        } else if (stage < 0) {
            return this.modifier/(this.modifier - stage);
        } else {
            return 1;
        }
    }

    // Gets the stat value only taking the stage into account
    private int getStat(Battle b, ActivePokemon p, ActivePokemon opp, boolean applyEffects) {
        int stat;
        if (this == EVASION || this == ACCURACY) {
            stat = 100;
        } else {
            stat = p.getStat(b, this);
            if (applyEffects) {
                stat = StatChangingEffect.modifyStat(b, p, opp, this, stat);
            }
        }

        // Only use basic stage if not applying effects
        int stage = applyEffects ? this.getStage(p, opp, b) : p.getStage(this);

        // Modify stat based off stage
        stat *= this.getStageStatModifier(stage);

        // Applies stat changes to each for each item in list
        if (applyEffects) {
            stat *= StatModifyingEffect.getModifier(b, p, opp, this);
        }

        return stat;
    }

    // Gets the stat of a Pokemon during battle with all modifiers taken into account
    public static int getStat(Stat s, ActivePokemon p, Battle b) {
        ActivePokemon opp = b.getOtherPokemon(p);

        // Effects that manipulate stats
        s = StatSwitchingEffect.switchStat(b, p, s);
        s = OpponentStatSwitchingEffect.switchStat(b, opp, s);

        // Apply stage and stat changes
        int stat = s.getStat(b, p, opp, true);

        // Just to be safe
        stat = Math.max(1, stat);

        return stat;
    }

    public int getStage(ActivePokemon stagePokemon, ActivePokemon otherPokemon, Battle b) {
        // Opponent effects that completely ignore stage changes
        if (OpponentIgnoreStageEffect.checkIgnoreStage(b, stagePokemon, otherPokemon, this)) {
            return 0;
        }

        int stage = stagePokemon.getStage(this);
        stage = StageChangingEffect.getStageWithCritCheck(stage, stagePokemon, otherPokemon, this);

        // Update the stage due to effects
        stage += StageChangingEffect.getModifier(b, stagePokemon, otherPokemon, this);

        // Let's keep everything in bounds, okay!
        stage = Math.max(-1*Stages.MAX_STAT_CHANGES, Math.min(stage, Stages.MAX_STAT_CHANGES));

        // Stage Pokemon effects that completely ignore stage changes
        if (IgnoreStageEffect.checkIgnoreStage(b, stagePokemon, this, stage)) {
            return 0;
        }

        return stage;
    }

    // Returns the corresponding Stat based on the index passed in
    public static Stat getStat(int index, boolean battle) {
        for (Stat s : values()) {
            if ((s.onlyBattle == InBattle.ONLY && !battle) || (s.onlyBattle == InBattle.NEVER && battle)) {
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
