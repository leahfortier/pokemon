package test.battle;

import org.junit.Assert;
import pokemon.stat.Stat;
import test.TestUtils;

public class TestStages {
    private int[] stages;

    public TestStages() {
        stages = new int[Stat.NUM_BATTLE_STATS];
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
