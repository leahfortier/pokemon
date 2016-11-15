package map.triggers;

import main.Game;
import map.Direction;
import pattern.map.MapTransitionMatcher;
import trainer.CharacterData;
import util.JsonUtils;

public class MapTransitionTrigger extends Trigger {
	private String nextMap;
	private String mapEntranceName;
	private Direction direction;
	private boolean deathPortal;

	private int newX;
	private int newY;

	static String getTriggerSuffix(String contents) {
		MapTransitionMatcher matcher = JsonUtils.deserialize(contents, MapTransitionMatcher.class);
		return matcher.getPreviousMap() + "_" + matcher.getNextMap() + "_" + matcher.getNextEntranceName();
	}

	MapTransitionTrigger(String contents, String condition) {
		super(TriggerType.MAP_TRANSITION, contents, condition);

		MapTransitionMatcher matcher = JsonUtils.deserialize(contents, MapTransitionMatcher.class);
		this.nextMap = matcher.getNextMap();
		this.mapEntranceName = matcher.getNextEntranceName();
		this.direction = matcher.getDirection();
		this.deathPortal = matcher.isDeathPortal();
	}
	
	protected void executeTrigger() {
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
