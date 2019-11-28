package test.battle;

import org.junit.Assert;
import pokemon.stat.Stat;
import test.general.TestUtils;

public class TestStages {
    private int[] stages;

    public TestStages() {
        stages = new int[Stat.NUM_BATTLE_STATS];
    }

    // Increases the stat stage by the amount (okay to be non-positive)
    // Valid to pass an amount that would increase past the max (will cap there automatically)
    // Typically used when comparing to previous stages
    public void increment(int amount, Stat stat) {
        int index = stat.index();
        stages[index] += amount;
        stages[index] = Math.min(Stat.MAX_STAT_CHANGES, stages[index]);
        stages[index] = Math.max(-Stat.MAX_STAT_CHANGES, stages[index]);
    }

    public TestStages set(int stage, Stat... stats) {
        for (Stat s : stats) {
            TestUtils.assertInclusiveRange(s.getName(), -6, 6, stage);
            Assert.assertEquals(0, stages[s.index()]);
            stages[s.index()] = stage;
        }
        return this;
    }

    public int get(Stat stat) {
        return this.stages[stat.index()];
    }

    public int[] get() {
        return this.stages.clone();
    }
}
