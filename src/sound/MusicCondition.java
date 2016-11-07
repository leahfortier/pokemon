package sound;

import map.Condition;

public class MusicCondition {
    private SoundTitle music;
    private Condition condition;

    public MusicCondition(SoundTitle music, String conditionString) {
        this.music = music;
        this.condition = new Condition(conditionString);
    }

    public SoundTitle getMusic() {
        return this.music;
    }

    public boolean isTrue() {
        return condition.isTrue();
    }
}
