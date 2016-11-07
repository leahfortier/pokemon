package map.triggers;

import main.Global;
import sound.SoundTitle;
import util.PokeString;

public class SoundTrigger extends Trigger {
	private SoundTitle music;
	public SoundTitle effect; // TODO: Make private

	SoundTrigger(String contents, String condition) {
		super(TriggerType.SOUND, contents, condition);

		SoundTitle soundTitle = SoundTitle.valueOf(PokeString.getNamesiesString(contents));
		if (soundTitle.isMusic()) {
			this.music = soundTitle;
		} else {
			this.effect = soundTitle;
		}
	}
	
	protected void executeTrigger() {
		if (music != null) {
			Global.soundPlayer.playMusic(music);
		}
		
		if (effect != null) {
			Global.soundPlayer.playSoundEffect(effect);
		}
	}
}
