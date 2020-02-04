package battle;

import battle.effect.InvokeInterfaces.ModifyStageValueEffect;
import battle.effect.InvokeInterfaces.StatLoweredEffect;
import battle.effect.InvokeInterfaces.StatProtectingEffect;
import battle.effect.source.CastSource;
import main.Global;
import message.Messages;
import pokemon.stat.Stat;

public class StageModifier {
    private transient ActivePokemon stagesHolder;

    public StageModifier(ActivePokemon stagesHolder) {
        this.stagesHolder = stagesHolder;
    }

    public void setStagesHolder(ActivePokemon stagesHolder) {
        this.stagesHolder = stagesHolder;
    }

    public int getStage(Stat stat) {
        return this.stagesHolder.getStages().getStage(stat);
    }

    private void incrementStage(Stat stat, int val) {
        this.stagesHolder.getStages().setStage(stat, getStage(stat) + val);
    }

    public void modifyStages(Battle b, ActivePokemon modifier, int[] mod, CastSource source) {
        for (int i = 0; i < mod.length; i++) {
            if (mod[i] == 0) {
                continue;
            }

            this.modifyStage(modifier, mod[i], Stat.getStat(i, true), b, source);
        }
    }

    public boolean modifyStage(ActivePokemon caster, int val, Stat stat, Battle b, CastSource source) {
        ModifyStageMessageGetter messageGetter = createGetter(b, caster, source);
        return modifyStage(caster, val, stat, b, source, messageGetter);
    }

    // Modifies a stat for a Pokemon and prints appropriate messages and stuff
    public boolean modifyStage(ActivePokemon caster, int val, Stat stat, Battle b, CastSource source, ModifyStageMessageGetter messageGetter) {
        ActivePokemon victim = this.stagesHolder;

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
        if (getStage(stat) == Stages.MAX_STAT_CHANGES && val > 0) {
            if (printFail) {
                Messages.add(victim.getName() + "'s " + statName + " cannot be raised any higher!");
            }
            return false;
        }

        // HOW LOW CAN YOU GO?!
        if (getStage(stat) == -Stages.MAX_STAT_CHANGES && val < 0) {
            // THIS LOW
            if (printFail) {
                Messages.add(victim.getName() + "'s " + statName + " cannot be lowered any further!");
            }
            return false;
        }

        String change = this.getChangedStatString(val);
        String victimName = caster == victim ? "its" : victim.getName() + "'s";

        String message = messageGetter.getMessage(victimName, statName, change);
        Messages.add(message);

        this.incrementStage(stat, val);

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

    private ModifyStageMessageGetter createGetter(Battle b, ActivePokemon caster, CastSource source) {
        switch (source) {
            case ATTACK:
            case USE_ITEM:
                // Bulbasaur's Attack was sharply raised!
                return (victimName, statName, changed) -> String.format(
                        "%s's %s was %s!", this.stagesHolder.getName(), statName, changed
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
    public interface ModifyStageMessageGetter {
        String getMessage(String victimName, String statName, String changed);
    }
}
