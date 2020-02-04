package battle;

import battle.effect.InvokeInterfaces.ModifyStageValueEffect;
import battle.effect.InvokeInterfaces.StatLoweredEffect;
import battle.effect.InvokeInterfaces.StatProtectingEffect;
import battle.effect.source.CastSource;
import main.Global;
import message.Messages;
import pokemon.stat.Stat;

public class StageModifier {
    private int[] modifiers;
    private ModifyStageMessenger messenger;

    public StageModifier(int[] modifiers) {
        this.modifiers = modifiers;
        this.messenger = null;
    }

    public StageModifier(int modifier, Stat... stats) {
        this(new int[Stat.NUM_BATTLE_STATS]);
        this.set(modifier, stats);
    }

    public StageModifier set(int modifier, Stat... stats) {
        for (Stat stat : stats) {
            this.modifiers[stat.index()] = modifier;
        }
        return this;
    }

    public StageModifier withMessage(ModifyStageMessenger messenger) {
        this.messenger = messenger;
        return this;
    }

    public boolean modify(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        boolean modified = false;
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i] == 0) {
                continue;
            }
            modified |= this.modify(caster, victim, modifiers[i], Stat.getStat(i, true), b, source);
        }
        return modified;
    }

    // Modifies a stat for a Pokemon and prints appropriate messages and stuff
    private boolean modify(ActivePokemon caster, ActivePokemon victim, int val, Stat stat, Battle b, CastSource source) {
        if (this.messenger == null) {
            this.messenger = createGetter(b, caster, victim, source);
        }

        // Don't modify the stages of a dead Pokemon
        if (victim.isFainted(b)) {
            return false;
        }

        String statName = stat.getName();
        boolean printFail = source == CastSource.ATTACK && caster.getAttack().canPrintFail();

        // Effects that change the value of the modifier
        ActivePokemon moldBreaker = source == CastSource.ATTACK && caster != victim ? caster : null;
        val *= ModifyStageValueEffect.getModifier(b, moldBreaker, victim);

        // Effects that prevent stat reductions caused by the opponent
        if (val < 0 && caster != victim) {
            StatProtectingEffect prevent = StatProtectingEffect.getPreventEffect(moldBreaker, b, caster, victim, stat);
            if (prevent != null) {
                if (printFail) {
                    // TODO: This prints multiple times for attacks that lower multiple stats
                    Messages.add(prevent.preventionMessage(b, victim, stat));
                }
                return false;
            }
        }

        // Too High
        int currentStage = victim.getStages().getStage(stat);
        if (currentStage == Stages.MAX_STAT_CHANGES && val > 0) {
            if (printFail) {
                Messages.add(victim.getName() + "'s " + statName + " cannot be raised any higher!");
            }
            return false;
        }

        // HOW LOW CAN YOU GO?!
        if (currentStage == -Stages.MAX_STAT_CHANGES && val < 0) {
            // THIS LOW
            if (printFail) {
                Messages.add(victim.getName() + "'s " + statName + " cannot be lowered any further!");
            }
            return false;
        }

        String change = this.getChangedStatString(val);
        String victimName = caster == victim ? "its" : victim.getName() + "'s";

        String message = messenger.getMessage(victimName, statName, change);
        Messages.add(message);

        victim.getStages().incrementStage(stat, val);

        if (val < 0) {
            StatLoweredEffect.invokeStatLoweredEffect(b, caster, victim);
        }

        return true;
    }

    // -3 or lower: drastically lowered
    // -2: sharply lowered
    // -1: lowered
    // 0: <throws error>
    // 1: raised
    // 2: sharply raised
    // 3 or higher: drastically raised
    private String getChangedStatString(int val) {
        if (val == 0) {
            Global.error("Cannot modify a stage by zero.");
        }

        int positive = Math.abs(val);
        String modifier;
        if (positive == 1) {
            modifier = "";
        } else if (positive == 2) {
            modifier = "sharply ";
        } else {
            modifier = "drastically ";
        }

        String direction;
        if (val > 0) {
            direction = "raised";
        } else {
            direction = "lowered";
        }

        return modifier + direction;
    }

    private ModifyStageMessenger createGetter(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        switch (source) {
            case ATTACK:
            case USE_ITEM:
                // Bulbasaur's Attack was sharply raised!
                return (victimName, statName, changed) -> String.format(
                        "%s's %s was %s!", victim.getName(), statName, changed
                );
            case ABILITY:
            case HELD_ITEM:
                // Gyarados's Intimidate lowered Charmander's Attack!
                // Bulbasaur's Absorb Bulb raised its Special Attack!
                return (victimName, statName, changed) -> String.format(
                        "%s's %s %s %s %s!",
                        caster.getName(), source.getSourceName(b, caster), changed, victimName, statName
                );
            case EFFECT:
                Global.error("Effect message should be handled manually using the other modifyStage method.");
                break;
            default:
                Global.error("Unknown source for stage modifier.");
                break;
        }

        return (victimName, statName, changed) -> "";
    }

    @FunctionalInterface
    public interface ModifyStageMessenger {
        String getMessage(String victimName, String statName, String changed);
    }
}
