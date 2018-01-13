package battle;

import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.ModifyStageValueEffect;
import battle.effect.generic.EffectInterfaces.StatLoweredEffect;
import battle.effect.generic.EffectInterfaces.StatProtectingEffect;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;
import util.StringUtils;

import java.io.Serializable;

public class Stages implements Serializable {
    private transient ActivePokemon attributesHolder;

    private int[] stages;

    public Stages(ActivePokemon attributesHolder) {
        this.attributesHolder = attributesHolder;
        this.reset();
    }

    public void setAttributesHolder(ActivePokemon attributesHolder) {
        this.attributesHolder = attributesHolder;
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

        Messages.add(new MessageUpdate().withPokemon(attributesHolder));
    }

    public void incrementStage(Stat stat, int val) {
        setStage(stat, getStage(stat) + val);
    }

    public void resetStage(Stat stat) {
        setStage(stat, 0);
    }

    public boolean modifyStage(ActivePokemon caster, ActivePokemon victim, int val, Stat stat, Battle b, CastSource source) {
        String message = StringUtils.empty();

        switch (source) {
            case ATTACK:
            case USE_ITEM:
                message = victim.getName() + "'s {statName} was {change}!";
                break;
            case ABILITY:
                message = caster.getName() + "'s " + caster.getAbility().getName() + " {change} {victimName} {statName}!";
                break;
            case HELD_ITEM:
                message = caster.getName() + "'s " + caster.getHeldItem(b).getName() + " {change} {victimName} {statName}!";
                break;
            case EFFECT:
                Global.error("Effect message should be handled manually using the other modifyStage method.");
                break;
            default:
                Global.error("Unknown source for stage modifier.");
                break;
        }

        return modifyStage(caster, victim, val, stat, b, source, message);
    }

    // Modifies a stat for a Pokemon and prints appropriate messages and stuff
    public boolean modifyStage(ActivePokemon caster, ActivePokemon victim, int val, Stat stat, Battle b, CastSource source, String message) {

        // Don't modify the stages of a dead Pokemon
        if (victim.isFainted(b)) {
            return false;
        }

        String statName = stat.getName();
        boolean print = source == CastSource.ATTACK && caster.getAttack().canPrintFail();

        // Effects that change the value of the modifier
        val = ModifyStageValueEffect.updateModifyStageValueEffect(b, caster, victim, val);

        // Effects that prevent stat reductions caused by the opponent
        if (val < 0 && caster != victim) {
            StatProtectingEffect prevent = StatProtectingEffect.getPreventEffect(b, caster, victim, stat);
            if (prevent != null) {
                if (print) {
                    Messages.add(prevent.preventionMessage(victim, stat));
                }

                return false;
            }
        }

        // Too High
        if (getStage(stat) == Stat.MAX_STAT_CHANGES && val > 0) {
            if (print) {
                Messages.add(victim.getName() + "'s " + statName + " cannot be raised any higher!");
            }

            return false;
        }

        // HOW LOW CAN YOU GO?!
        if (getStage(stat) == -1*Stat.MAX_STAT_CHANGES && val < 0) {
            // THIS LOW
            if (print) {
                Messages.add(victim.getName() + "'s " + statName + " cannot be lowered any further!");
            }

            return false;
        }

        String change;
        String victimName = caster == victim ? "its" : victim.getName() + "'s";

        if (val >= 2) {
            change = "sharply raised";
        } else if (val == 1) {
            change = "raised";
        } else if (val == -1) {
            change = "lowered";
        } else if (val <= -2) {
            change = "sharply lowered";
        } else {
            // TODO: Make sure this is an appropriate error -- not sure why it wasn't here before
            Global.error("Cannot modify a stage by zero.");
            return false;
        }

        message = message.replace("{statName}", statName)
                         .replace("{change}", change)
                         .replace("{victimName}", victimName);
        Messages.add(message);

        this.incrementStage(stat, val);

        // Defiant raises Attack stat by two when a stat is lowered by the opponent
        if (val < 0 && caster != victim) {
            StatLoweredEffect.invokeStatLoweredEffect(b, caster, victim);
        }

        return true;
    }

    public int totalStatIncreases() {
        int sum = 0;
        for (Stat stat : Stat.BATTLE_STATS) {
            int stage = getStage(stat);
            if (stage > 0) {
                sum += stage;
            }
        }

        return sum;
    }

    public void swapStages(Stat stat, ActivePokemon other) {
        int userStat = this.getStage(stat);
        int victimStat = other.getStages().getStage(stat);

        this.setStage(stat, victimStat);
        other.getStages().setStage(stat, userStat);
    }

    public void modifyStages(Battle b, ActivePokemon modifier, int[] mod, CastSource source) {
        for (int i = 0; i < mod.length; i++) {
            if (mod[i] == 0) {
                continue;
            }

            this.modifyStage(modifier, this.attributesHolder, mod[i], Stat.getStat(i, true), b, source);
        }
    }
}
