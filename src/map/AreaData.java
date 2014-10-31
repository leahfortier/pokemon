package map;

import gui.GameData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AreaData {

	public static final Pattern areaSoundConditionPattern = Pattern.compile("(?:([()&|!\\w]+)\\s*:\\s*)?([\\w-]+)");
	
	public enum WeatherState{
		NORMAL, SUN, RAIN, FOG, SNOW
	};
	
	public enum TerrainType{
		GRASS, BUILDING, CAVE, SAND, WATER, SNOW, ICE 
	};
	
	private String name;
	private int color;
	private TerrainType terrainType;
	private WeatherState weather;
	
	private String musicCondition;
	private String musicTriggerName;
	
	public AreaData(String name, int color, String weather, String terrainType, String musicCondition)
	{
		this.name = name;
		this.color = color;
		this.terrainType = TerrainType.valueOf(terrainType);
		
		this.musicCondition = musicCondition;
		
		this.weather = WeatherState.valueOf(weather);
	}
	
	public void addMusicTriggers(GameData data)
	{
		if(musicCondition != null)
		{
			StringBuilder groupTriggers = new StringBuilder();
			String areaNameDisplay = name.replace(' ', '_').replaceAll("\\W", "");
			
			Matcher areaSoundMatcher = areaSoundConditionPattern.matcher(musicCondition);
			while(areaSoundMatcher.find())
			{
				String condition = areaSoundMatcher.group(1);
				String musicName = areaSoundMatcher.group(2);
				String soundTriggerName = "SoundTrigger_AreaSound_for_"+areaNameDisplay+"_MusicName_"+musicName;
				
//				System.out.println(condition + " : " + musicName);
				
				data.addTrigger("Sound", soundTriggerName, (condition != null? "condition: "+condition: "") + "\nmusicName: "+musicName);
				groupTriggers.append("trigger: "+soundTriggerName +"\n");
			}
			
			data.addTrigger("Group", "GroupTrigger_AreaSound_for_"+areaNameDisplay, groupTriggers.toString());
			musicTriggerName = "GroupTrigger_AreaSound_for_"+areaNameDisplay;
		}
	}
	
	public WeatherState getWeather()
	{
		return weather;
	}
	
	public TerrainType getTerrain()
	{
		return terrainType;
	}
	
	public String getAreaName()
	{
		return name;
	}
	
	public String getMusicTriggerName()
	{
		return musicTriggerName;
	}
}
