package map.triggers;

import sound.SoundPlayer;
import sound.SoundTitle;
import util.PokeString;

class SoundTrigger extends Trigger {
	private final SoundTitle soundTitle;

	SoundTrigger(String contents, String condition) {
		super(TriggerType.SOUND, contents, condition);

		this.soundTitle = SoundTitle.valueOf(PokeString.getNamesiesString(contents));
	}
	
	protected void executeTrigger() {
		SoundPlayer.soundPlayer.playSound(soundTitle);
	}
}
