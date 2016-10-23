package map.triggers;

import java.util.regex.Matcher;

import main.Game;
import map.entity.MovableEntity.Direction;

public class MapTransitionTrigger extends Trigger {
	private String mapName;
	private String mapEntranceName;
	private Direction direction;
	private int newX;
	private int newY;

	public MapTransitionTrigger(String name, String contents) {
		super(name, contents);
		Matcher m = variablePattern.matcher(contents);
		
		while (m.find()) {
			switch (m.group(1)) {
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
					int directionIndex = Integer.parseInt(m.group(2));
					direction = directionIndex == -1 ? null : Direction.values()[directionIndex];
					break;
				case "mapEntrance":
					mapEntranceName = m.group(2);
					break;
			}
		}
	}
	
	public MapTransitionTrigger(String name, String conditionString, String mapName, String mapEntranceName, int directionIndex) {
		super(name, conditionString);
		
		this.mapName = mapName;
		this.mapEntranceName = mapEntranceName;
		this.direction = directionIndex == -1 ? null : Direction.values()[directionIndex];;
	}
	
	public void execute(Game game) {
		super.execute(game);
		
		game.characterData.setMap(mapName, mapEntranceName);
		
		if (mapEntranceName == null || !game.data.getMap(mapName).setCharacterToEntrance(game.characterData, mapEntranceName)) {
			game.characterData.setLocation(newX, newY);
		}
		
		if (direction != null) {
			game.characterData.setDirection(direction);
		}
		
		game.characterData.mapReset = true;
	}

	public String getTransitionTriggerName() {
		return this.mapName + "_" + this.mapEntranceName;
	}

	public String getMapNamee() {
		return this.mapName;
	}

	public String getMapEntranceNamee() {
		return this.mapEntranceName;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public String toString() {
		return "MapTransitionTrigger: " + name + " map:" + mapName + " " + newX + " " + newY;
	}
	
	public String triggerDataAsString() {
		return super.triggerDataAsString()
				+ "\tnextMap: " + mapName + "\n"
				+ "\tmapEntrance: " + mapEntranceName + "\n"
				+ (direction == null ? "" : "\tdirection: " + direction + "\n");
	}
}
