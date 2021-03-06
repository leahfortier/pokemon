package battle.stages;

import battle.ActivePokemon;
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
        stages[index] = Math.min(MAX_STAT_CHANGES, stages[index]);
        stages[index] = Math.max(-1*MAX_STAT_CHANGES, stages[index]);

        Messages.add(new MessageUpdate().withPokemon(stagesHolder));
    }

    public void incrementStage(Stat stat, int val) {
        setStage(stat, getStage(stat) + val);
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
