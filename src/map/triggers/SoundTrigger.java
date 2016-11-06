package map.triggers;

import main.Global;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.SoundTriggerMatcher;
import sound.SoundTitle;
import util.PokeString;
import util.StringUtils;

public class SoundTrigger extends Trigger {
	private SoundTitle music;
	public SoundTitle effect; // TODO: Make private

	SoundTrigger(String contents) {
		super(TriggerType.SOUND, contents);

		SoundTriggerMatcher matcher = AreaDataMatcher.deserialize(contents, SoundTriggerMatcher.class);
		if (!StringUtils.isNullOrEmpty(matcher.effectName)) {
			this.effect = SoundTitle.valueOf(PokeString.getNamesiesString(matcher.effectName));
		}

		if (!StringUtils.isNullOrEmpty(matcher.musicName)) {
			this.music = SoundTitle.valueOf(PokeString.getNamesiesString(matcher.musicName));
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
