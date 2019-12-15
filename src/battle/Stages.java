package battle;

import battle.effect.InvokeInterfaces.ModifyStageValueEffect;
import battle.effect.InvokeInterfaces.StatLoweredEffect;
import battle.effect.InvokeInterfaces.StatProtectingEffect;
import battle.effect.source.CastSource;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.stat.Stat;
import util.serialization.Serializable;

import java.util.List;
import java.util.stream.Collectors;

public class Stages implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient ActivePokemon stagesHolder;

    private int[] stages;

    public Stages(ActivePokemon stagesHolder) {
        this.stagesHolder = stagesHolder;
        this.reset();
    }

    public void setStagesHolder(ActivePokemon stagesHolder) {
        this.stagesHolder = stagesHolder;
    }

    public void reset() {
        stages = new int[Stat.NUM_BATTLE_STATS];
    }

    public int getStage(Stat stat) {
        return this.stages[stat.index()];
    }

    public void setStage(Stat stat, int val) {
        int index = stat.index();
        stages[index] = val;

        // Don't let it go out of bounds, yo!
        stages[index] = Math.min(Stat.MAX_STAT_CHANGES, stages[index]);
        stages[index] = Math.max(-1*Stat.MAX_STAT_CHANGES, stages[index]);

        Messages.add(new MessageUpdate().withPokemon(stagesHolder));
    }

    private void incrementStage(Stat stat, int val) {
        setStage(stat, getStage(stat) + val);
    }

    public void resetStage(Stat stat) {
        setStage(stat, 0);
    }

    public List<Stat> getNonMaxStats() {
        return getNonValueStats(Stat.MAX_STAT_CHANGES);
    }

    public List<Stat> getNonMinStats() {
        return getNonValueStats(-Stat.MAX_STAT_CHANGES);
    }

    private List<Stat> getNonValueStats(int value) {
        return Stat.BATTLE_STATS.stream()
                                .filter(stat -> this.stages[stat.index()] != value)
                                .collect(Collectors.toList());
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
                    Messages.add(prevent.preventionMessage(b, victim, stat));
                }
                return false;
            }
        }

        // Too High
        if (getStage(stat) == Stat.MAX_STAT_CHANGES && val > 0) {
            if (printFail) {
                Messages.add(victim.getName() + "'s " + statName + " cannot be raised any higher!");
            }
            return false;
        }

        // HOW LOW CAN YOU GO?!
        if (getStage(stat) == -Stat.MAX_STAT_CHANGES && val < 0) {
            // THIS LOW
            if (printFail) {
                Messages.add(victim.getName() + "'s " + statName + " cannot be lowered any further!");
            }
            return false;
        }

        String change;
        if (val >= 2) {
            change = "sharply raised";
        } else if (val == 1) {
            change = "raised";
        } else if (val == -1) {
            change = "lowered";
        } else if (val <= -2) {
            change = "sharply lowered";
        } else {
            Global.error("Cannot modify a stage by zero.");
            return false;
        }

        String victimName = caster == victim ? "its" : victim.getName() + "'s";

        String message = messageGetter.getMessage(victimName, statName, change);
        Messages.add(message);

        this.incrementStage(stat, val);

        if (val < 0) {
            StatLoweredEffect.invokeStatLoweredEffect(b, caster, victim);
        }

        return true;
    }

    public void modifyStages(Battle b, ActivePokemon modifier, int[] mod, CastSource source) {
        for (int i = 0; i < mod.length; i++) {
            if (mod[i] == 0) {
                continue;
            }

            this.modifyStage(modifier, mod[i], Stat.getStat(i, true), b, source);
        }
    }

    // Returns the sum of all stages
    public int totalStatChanges() {
        return Stat.BATTLE_STATS.stream().mapToInt(this::getStage).sum();
    }

    // Returns the sum of all positive stages
    public int totalStatIncreases() {
        return Stat.BATTLE_STATS.stream()
                                .filter(stat -> this.getStage(stat) > 0)
                                .mapToInt(this::getStage)
                                .sum();
    }

    public void swapStages(Stat stat, ActivePokemon other) {
        int userStat = this.getStage(stat);
        int victimStat = other.getStages().getStage(stat);

        this.setStage(stat, victimStat);
        other.getStages().setStage(stat, userStat);
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
