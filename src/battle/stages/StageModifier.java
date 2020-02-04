package battle.stages;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.InvokeInterfaces.ModifyStageValueEffect;
import battle.effect.InvokeInterfaces.StatLoweredEffect;
import battle.effect.InvokeInterfaces.StatProtectingEffect;
import battle.effect.source.CastSource;
import main.Global;
import message.Messages;
import pokemon.stat.Stat;
import util.serialization.Serializable;

import java.util.Arrays;

public class StageModifier implements Serializable {
    private static final long serialVersionUID = 1L;

    private int[] modifiers;
    private ModifyStageMessenger messenger;

    public StageModifier() {
        this(new int[Stat.NUM_BATTLE_STATS]);
    }

    public StageModifier(int[] modifiers) {
        this.modifiers = modifiers;
        this.messenger = null;
    }

    public StageModifier(int modifier, Stat... stats) {
        this();
        this.set(modifier, stats);
    }

    public StageModifier set(int modifier, Stat... stats) {
        for (Stat stat : stats) {
            this.modifiers[stat.index()] = modifier;
        }
        return this;
    }

    // Resets all the modifier values to zero (does not effect other things like messenger)
    public void reset() {
        Arrays.fill(this.modifiers, 0);
    }

    public StageModifier withMessage(ModifyStageMessenger messenger) {
        this.messenger = messenger;
        return this;
    }

    public int[] getCopy() {
        return this.modifiers.clone();
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

        this.addMessage(b, caster, victim, source, val, statName);

        victim.getStages().incrementStage(stat, val);

        if (val < 0) {
            StatLoweredEffect.invokeStatLoweredEffect(b, caster, victim);
        }

        return true;
    }

    private void addMessage(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, int val, String statName) {
        if (this.messenger == null) {
            this.messenger = createMessenger(b, caster, source);
        }

        String change = this.getChangedStatString(val);
        String possessiveVictim = caster == victim ? "its" : victim.getName() + "'s";
        String message = messenger.getMessage(victim.getName(), possessiveVictim, statName, change);
        Messages.add(message);
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

    private ModifyStageMessenger createMessenger(Battle b, ActivePokemon caster, CastSource source) {
        switch (source) {
            case ATTACK:
            case USE_ITEM:
                // Bulbasaur's Attack was sharply raised!
                return (victimName, possessiveVictim, statName, changed) -> String.format(
                        "%s's %s was %s!", victimName, statName, changed
                );
            case ABILITY:
            case HELD_ITEM:
                // Gyarados's Intimidate lowered Charmander's Attack!
                // Bulbasaur's Absorb Bulb raised its Special Attack!
                return (victimName, possessiveVictim, statName, changed) -> String.format(
                        "%s's %s %s %s %s!",
                        caster.getName(), source.getSourceName(b, caster), changed, possessiveVictim, statName
                );
            case EFFECT:
                Global.error("Effect message should be handled manually using the other modifyStage method.");
                break;
            default:
                Global.error("Unknown source for stage modifier.");
                break;
        }

        return (victimName, possessiveVictim, statName, changed) -> "";
    }

    @FunctionalInterface
    public interface ModifyStageMessenger extends Serializable {
        String getMessage(String victimName, String possessiveVictim, String statName, String changed);
    }
}
