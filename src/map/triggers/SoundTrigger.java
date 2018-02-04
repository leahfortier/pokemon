package map.triggers;

import map.condition.Condition;
import sound.SoundPlayer;
import sound.SoundTitle;

class SoundTrigger extends Trigger {
    private final SoundTitle soundTitle;

    SoundTrigger(String contents, Condition condition) {
        super(TriggerType.SOUND, contents, condition);

        this.soundTitle = SoundTitle.valueOf(contents);
    }

    @Override
    protected void executeTrigger() {
        SoundPlayer.instance().playSound(soundTitle);
    }
}
