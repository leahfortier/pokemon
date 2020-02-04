package battle;

import battle.StageModifier.ModifyStageMessageGetter;
import battle.effect.source.CastSource;
import message.MessageUpdate;
import message.Messages;
import pokemon.stat.Stat;
import util.serialization.Serializable;

import java.util.List;
import java.util.stream.Collectors;

public class Stages implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_STAT_CHANGES = 6;

    private transient ActivePokemon stagesHolder;

    private int[] stages;
    private StageModifier stageModifier;

    public Stages(ActivePokemon stagesHolder) {
        this.stagesHolder = stagesHolder;
        this.stageModifier = new StageModifier(stagesHolder);
        this.reset();
    }

    public void setStagesHolder(ActivePokemon stagesHolder) {
        this.stagesHolder = stagesHolder;
        this.stageModifier.setStagesHolder(stagesHolder);
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
        stages[index] = Math.min(MAX_STAT_CHANGES, stages[index]);
        stages[index] = Math.max(-1*MAX_STAT_CHANGES, stages[index]);

        Messages.add(new MessageUpdate().withPokemon(stagesHolder));
    }

    public void resetStage(Stat stat) {
        setStage(stat, 0);
    }

    public List<Stat> getNonMaxStats() {
        return getNonValueStats(MAX_STAT_CHANGES);
    }

    public List<Stat> getNonMinStats() {
        return getNonValueStats(-MAX_STAT_CHANGES);
    }

    private List<Stat> getNonValueStats(int value) {
        return Stat.BATTLE_STATS.stream()
                                .filter(stat -> this.stages[stat.index()] != value)
                                .collect(Collectors.toList());
    }

    public boolean modifyStage(ActivePokemon caster, int val, Stat stat, Battle b, CastSource source) {
        return this.stageModifier.modifyStage(caster, val, stat, b, source);
    }

    // Modifies a stat for a Pokemon and prints appropriate messages and stuff
    public boolean modifyStage(ActivePokemon caster, int val, Stat stat, Battle b, CastSource source, ModifyStageMessageGetter messageGetter) {
        return this.stageModifier.modifyStage(caster, val, stat, b, source, messageGetter);
    }

    public void modifyStages(Battle b, ActivePokemon modifier, int[] mod, CastSource source) {
        this.stageModifier.modifyStages(b, modifier, mod, source);
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
}
