package test.battle;

import pokemon.Stat;
import test.TestUtils;

public class TestStages {
    private int[] stages;

    public TestStages() {
        stages = new int[Stat.NUM_BATTLE_STATS];
    }

    public TestStages set(Stat s, int stage) {
        TestUtils.assertInclusiveRange(s.getName(), -6, 6, stage);
        stages[s.index()] = stage;
        return this;
    }

    public int get(Stat stat) {
        return this.stages[stat.index()];
    }

    public int[] get() {
        return this.stages.clone();
    }
}
