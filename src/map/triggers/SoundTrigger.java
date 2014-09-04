package map.triggers;

import java.util.regex.Matcher;

import main.Game;
import main.Global;

public class SoundTrigger extends Trigger {

	public String musicName;
	public String effectName;
	
	public SoundTrigger(String name, String contents) {
		super(name, contents);

		Matcher m = variablePattern.matcher(contents);
		
		while (m.find())
		{
			switch (m.group(1))
			{
				case "effectName":
					effectName = m.group(2);
					break;
				case "musicName":
					musicName = m.group(2);
					break;
			}
		}
	}
	
	public SoundTrigger(String name, String conditionString, String musicName, String effectName) 
	{
		super(name, conditionString);
		
		this.musicName = musicName;
		this.effectName = effectName;
	}
	
	public void execute(Game game) 
	{
		super.execute(game);
			
		if(musicName != null)
		{
			Global.soundPlayer.playMusic(musicName);
		}
		
		if(effectName != null)
		{
			Global.soundPlayer.playSoundEffect(effectName);
		}
	}

	public String toString() 
	{
		return "SoundTrigger: " + name + " music: " + musicName + " effect: " + effectName;
	}
	
	public String triggerDataAsString() 
	{
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		
		if(musicName != null)
		{
			ret.append("\tmusicName: " + musicName + "\n");
		}
		
		if(effectName != null)
		{
			ret.append("\teffectName: " + effectName + "\n");
		}
		
		return ret.toString();
	}
}
