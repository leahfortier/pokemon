package map.triggers;

import sound.SoundPlayer;
import sound.SoundTitle;

public class SoundTrigger extends Trigger {
    private final SoundTitle soundTitle;

    public SoundTrigger(SoundTitle soundTitle) {
        super(soundTitle.name());
        this.soundTitle = soundTitle;
    }

    @Override
    public void execute() {
        SoundPlayer.instance().playSound(soundTitle);
    }
}
