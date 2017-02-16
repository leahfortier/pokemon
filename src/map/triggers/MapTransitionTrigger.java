package map.triggers;

import main.Game;
import map.MapName;
import map.PathDirection;
import pattern.map.MapTransitionMatcher;
import trainer.CharacterData;
import util.JsonUtils;

class MapTransitionTrigger extends Trigger {
	private final MapName nextMap;
	private final String mapEntranceName;
	private final PathDirection direction;
	private final boolean deathPortal;

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
		
		if (direction != null && direction != PathDirection.WAIT) {
			player.setDirection(direction.getDirection());
		}

		if (deathPortal) {
			Game.getPlayer().setPokeCenter(nextMap, mapEntranceName);
		}

		player.setMapReset(true);
	}
}
