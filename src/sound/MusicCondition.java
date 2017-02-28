package sound;

import map.condition.Condition;

public class MusicCondition {
    private final SoundTitle music;
    private final Condition condition;

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
