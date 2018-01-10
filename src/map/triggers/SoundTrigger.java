package map.triggers;

import sound.SoundPlayer;
import sound.SoundTitle;

class SoundTrigger extends Trigger {
    private final SoundTitle soundTitle;

    SoundTrigger(String contents, String condition) {
        super(TriggerType.SOUND, contents, condition);

        this.soundTitle = SoundTitle.valueOf(contents);
    }

    @Override
    protected void executeTrigger() {
        SoundPlayer.instance().playSound(soundTitle);
    }
}
