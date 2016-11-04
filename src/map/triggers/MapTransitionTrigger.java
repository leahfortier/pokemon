package map.triggers;

import main.Game;
import map.Direction;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.MapTransitionTriggerMatcher;
import trainer.CharacterData;

public class MapTransitionTrigger extends Trigger {
	private String nextMap;
	private String mapEntranceName;
	private Direction direction;
	private int newX;
	private int newY;

	static String getTriggerSuffix(String contents) {
		MapTransitionTriggerMatcher matcher = AreaDataMatcher.deserialize(contents, MapTransitionTriggerMatcher.class);
		return matcher.previousMap + "_" + matcher.nextMap + "_" + matcher.mapEntrance;
	}

	MapTransitionTrigger(String contents) {
		super(TriggerType.MAP_TRANSITION, contents);

		MapTransitionTriggerMatcher matcher = AreaDataMatcher.deserialize(contents, MapTransitionTriggerMatcher.class);
		this.nextMap = matcher.nextMap;
		this.mapEntranceName = matcher.mapEntrance;
		this.direction = matcher.direction;
		this.newX = matcher.newX;
		this.newY = matcher.newY;
	}
	
	protected void executeTrigger() {
		System.out.println("execute map trigger");
		CharacterData player = Game.getPlayer();
		player.setMap(nextMap, mapEntranceName);
		
		if (mapEntranceName == null || !Game.getData().getMap(nextMap).setCharacterToEntrance(player, mapEntranceName)) {
			player.setLocation(newX, newY);
		}
		
		if (direction != null) {
			player.setDirection(direction);
		}
		
		player.mapReset = true;
	}

	public String getTransitionTriggerName() {
		return this.nextMap + "_" + this.mapEntranceName;
	}

	public String getNextMap() {
		return this.nextMap;
	}

	public String getMapEntranceName() {
		return this.mapEntranceName;
	}

	public Direction getDirection() {
		return this.direction;
	}
}
