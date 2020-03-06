package battle.stages;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.InvokeInterfaces.ModifyStageValueEffect;
import battle.effect.InvokeInterfaces.StatLoweredEffect;
import battle.effect.InvokeInterfaces.StatProtectingEffect;
import battle.effect.InvokeInterfaces.StatTargetSwapperEffect;
import battle.effect.source.CastSource;
import battle.stages.ModifyStageMessenger.DefaultModifyStageMessenger;
import message.Messages;
import pokemon.stat.Stat;
import util.serialization.Serializable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StageModifier implements Serializable {
    private static final long serialVersionUID = 1L;

    private int[] modifiers;
    private ModifyStageMessenger messenger;

    private Set<String> failMessages;

    public StageModifier() {
        this.modifiers = new int[Stat.NUM_BATTLE_STATS];
        this.messenger = new DefaultModifyStageMessenger();
        this.failMessages = new HashSet<>();
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

    // Modifies all set stats for the victim and prints appropriate messages and stuff
    public boolean modify(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        this.failMessages.clear();

        boolean modified = false;
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i] == 0) {
                continue;
            }
            modified |= this.modify(caster, victim, modifiers[i], Stat.getStat(i, true), b, source);
        }

        return modified;
    }

    // Modifies a single stat for a Pokemon and prints appropriate messages and stuff
    private boolean modify(ActivePokemon caster, ActivePokemon victim, int val, Stat stat, Battle b, CastSource source) {
        // Don't modify the stages of a dead Pokemon
        if (victim.isFainted(b) || !b.isFront(victim)) {
            return false;
        }

        String statName = stat.getName();
        boolean printFail = source == CastSource.ATTACK && caster.getAttack().canPrintFail();
        boolean selfCaster = caster == victim;

        // Effects that change the value of the modifier
        ActivePokemon moldBreaker = source == CastSource.ATTACK && !selfCaster ? caster : null;
        val *= ModifyStageValueEffect.getModifier(b, moldBreaker, victim);

        // Effects that prevent stat reductions caused by the opponent
        if (val < 0 && !selfCaster) {
            StatTargetSwapperEffect swapper = StatTargetSwapperEffect.checkTargetSwap(moldBreaker, b, caster, victim);
            if (swapper != null) {
                this.addFailMessage(true, swapper.getSwapStatTargetMessage(victim));
                victim = caster;
            }

            StatProtectingEffect prevent = StatProtectingEffect.getPreventEffect(moldBreaker, b, caster, victim, stat);
            if (prevent != null) {
                this.addFailMessage(printFail, prevent.preventionMessage(victim, stat));
                return false;
            }
        }

        // Too High
        int currentStage = victim.getStages().getStage(stat);
        if (currentStage == Stages.MAX_STAT_CHANGES && val > 0) {
            this.addFailMessage(printFail, victim.getName() + "'s " + statName + " cannot be raised any higher!");
            return false;
        }

        // HOW LOW CAN YOU GO?!
        if (currentStage == -Stages.MAX_STAT_CHANGES && val < 0) {
            // THIS LOW
            this.addFailMessage(printFail, victim.getName() + "'s " + statName + " cannot be lowered any further!");
            return false;
        }

        this.messenger.addMessage(caster, victim, source, val, statName);

        victim.getStages().incrementStage(stat, val);

        if (val < 0) {
            StatLoweredEffect.invokeStatLoweredEffect(b, victim, selfCaster);
        }

        return true;
    }

    // If applicable, adds the message to the queue
    // Keeps track of all the messages in the current modify to prevent duplicates
    private void addFailMessage(boolean printFail, String failMessage) {
        if (printFail && !this.failMessages.contains(failMessage)) {
            Messages.add(failMessage);
            this.failMessages.add(failMessage);
        }
    }
}
