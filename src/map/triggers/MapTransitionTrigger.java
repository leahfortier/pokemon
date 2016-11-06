package map.triggers;

import main.Game;
import map.Direction;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.MapExitMatcher;
import trainer.CharacterData;

public class MapTransitionTrigger extends Trigger {
	private String nextMap;
	private String mapEntranceName;
	private Direction direction;
	private boolean deathPortal;

	private int newX;
	private int newY;

	static String getTriggerSuffix(String contents) {
		MapExitMatcher matcher = AreaDataMatcher.deserialize(contents, MapExitMatcher.class);
		return matcher.previousMap + "_" + matcher.nextMap + "_" + matcher.nextEntrance;
	}

	MapTransitionTrigger(String contents, String condition) {
		super(TriggerType.MAP_TRANSITION, contents, condition);

		MapExitMatcher matcher = AreaDataMatcher.deserialize(contents, MapExitMatcher.class);
		this.nextMap = matcher.nextMap;
		this.mapEntranceName = matcher.nextEntrance;
		this.direction = matcher.direction;
		this.deathPortal = matcher.isDeathPortal();
	}
	
	protected void executeTrigger() {
		System.out.println("execute map trigger");
		CharacterData player = Game.getPlayer();
		player.setMap(nextMap, mapEntranceName);

		// TODO: When is newx/newy specified? why would they not just specify an actual entrance?
		if (mapEntranceName == null || !Game.getData().getMap(nextMap).setCharacterToEntrance(mapEntranceName)) {
			player.setLocation(newX, newY);
		}
		
		if (direction != null) {
			player.setDirection(direction);
		}

		if (deathPortal) {
			Game.getPlayer().setPokeCenter();
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
