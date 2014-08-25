package map.triggers;

import java.util.regex.Matcher;

import main.Game;

public class MapTransitionTrigger extends Trigger
{
	public String mapName;
	public int newX, newY;
	public int direction = -1;
	public String mapEntranceName;
	
	public MapTransitionTrigger(String name, String contents) 
	{
		super(name, contents);
		Matcher m = variablePattern.matcher(contents);
		
		while (m.find())
		{
			switch (m.group(1))
			{
				case "nextMap":
					mapName = m.group(2);
					break;
				case "newX":
					newX = Integer.parseInt(m.group(2));
					break;
				case "newY":
					newY = Integer.parseInt(m.group(2));
					break;
				case "direction":
					direction = Integer.parseInt(m.group(2));
					break;
				case "mapEntrance":
					mapEntranceName = m.group(2);
					break;
			}
		}
	}
	
	public MapTransitionTrigger(String name, String conditionString, String mapName, String mapEntranceName, int direction) 
	{
		super(name, conditionString);
		
		this.mapName = mapName;
		this.mapEntranceName = mapEntranceName;
		this.direction = direction;
	}
	
	public void execute(Game game) 
	{
		super.execute(game);
		
		game.charData.setMap(mapName, mapEntranceName);
		
		if (mapEntranceName == null || !game.data.getMap(mapName).setCharacterToEntrance(game.charData, mapEntranceName)) 
		{
			game.charData.setLocation(newX, newY);
		}
		
		if (direction != -1)
			game.charData.setDirection(direction);
		
		game.charData.mapReset = true;
	}

	public String toString() 
	{
		return "MapTransitionTrigger: " + name + " map:" + mapName + " " + newX + " " + newY;
	}
	
	public String triggerDataAsString() 
	{
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		ret.append("\tnextMap: " + mapName + "\n"+
				"\tmapEntrance: " + mapEntranceName + "\n" +
				(direction == -1 ? "" : "\tdirection: " + direction + "\n"));
		
		return ret.toString();
	}
}
