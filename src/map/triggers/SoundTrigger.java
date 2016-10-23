package map.triggers;

import java.util.regex.Matcher;

import sound.SoundTitle;
import main.Game;
import main.Global;
import util.StringUtils;

public class SoundTrigger extends Trigger {
	private SoundTitle music;
	public SoundTitle effect; // TODO: Make private
	
	public SoundTrigger(String name, String contents) 
	{
		super(name, contents);

		Matcher m = variablePattern.matcher(contents);
		
		while (m.find()) {
			switch (m.group(1)) {
				case "effectName":
					effect = SoundTitle.valueOf(m.group(2));
					break;
				case "musicName":
					music = SoundTitle.valueOf(m.group(2));
					break;
			}
		}
	}
	
	public SoundTrigger(String name, String conditionString, SoundTitle music, SoundTitle effect) {
		super(name, conditionString);
		
		this.music = music;
		this.effect = effect;
	}
	
	public void execute(Game game) {
		super.execute(game);
			
		if (music != null) {
			Global.soundPlayer.playMusic(music);
		}
		
		if(effect != null) {
			Global.soundPlayer.playSoundEffect(effect);
		}
	}

	public String toString() {
		return "SoundTrigger: " + name + " music: " + music + " effect: " + effect;
	}
	
	public String triggerDataAsString() {
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		
		if (music != null) {
			StringUtils.appendLine(ret, "\tmusicName: " + music);
		}

		if (effect != null) {
			StringUtils.appendLine(ret, "\teffectName: " + effect);
		}
		
		return ret.toString();
	}
}
