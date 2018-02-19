package map.triggers;

import map.condition.Condition;
import sound.SoundPlayer;
import sound.SoundTitle;

public class SoundTrigger extends Trigger {
    private final SoundTitle soundTitle;

    SoundTrigger(String contents, Condition condition) {
        this(SoundTitle.valueOf(contents), condition);
    }

    public SoundTrigger(SoundTitle soundTitle, Condition condition) {
        super(soundTitle.name(), condition);
        this.soundTitle = soundTitle;
    }

    @Override
    protected void executeTrigger() {
        SoundPlayer.instance().playSound(soundTitle);
    }
}
