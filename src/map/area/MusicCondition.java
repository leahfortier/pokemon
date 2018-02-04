package map.area;

import map.condition.Condition;
import map.condition.ConditionSet;
import sound.SoundTitle;

public class MusicCondition {
    private final SoundTitle music;
    private final ConditionSet condition;

    public MusicCondition(SoundTitle music, Condition condition) {
        this.music = music;
        this.condition = new ConditionSet(condition);
    }

    public SoundTitle getMusic() {
        return this.music;
    }

    public boolean isTrue() {
        return condition.evaluate();
    }
}
