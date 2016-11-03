package map.triggers;

import main.Game;
import map.Direction;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.MapTransitionTriggerMatcher;
import trainer.CharacterData;

public class MapTransitionTrigger extends Trigger {
	private String mapName;
	private String mapEntranceName;
	private Direction direction;
	private int newX;
	private int newY;

	public MapTransitionTrigger(String name, String contents) {
		super(name, contents);

		MapTransitionTriggerMatcher matcher = AreaDataMatcher.deserialize(contents, MapTransitionTriggerMatcher.class);
		this.mapName = matcher.nextMap;
		this.mapEntranceName = matcher.mapEntrance;
		this.direction = matcher.direction;
		this.newX = matcher.newX;
		this.newY = matcher.newY;
	}
	
	public MapTransitionTrigger(String name, String conditionString, String mapName, String mapEntranceName, int directionIndex) {
		super(name, conditionString);
		
		this.mapName = mapName;
		this.mapEntranceName = mapEntranceName;
		this.direction = directionIndex == -1 ? null : Direction.values()[directionIndex];
	}
	
	public void execute() {
		super.execute();

		CharacterData player = Game.getPlayer();
		player.setMap(mapName, mapEntranceName);
		
		if (mapEntranceName == null || !Game.getData().getMap(mapName).setCharacterToEntrance(player, mapEntranceName)) {
			player.setLocation(newX, newY);
		}
		
		if (direction != null) {
			player.setDirection(direction);
		}
		
		player.mapReset = true;
	}

	public String getTransitionTriggerName() {
		return this.mapName + "_" + this.mapEntranceName;
	}

	// TODO: Lalala rename these
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
