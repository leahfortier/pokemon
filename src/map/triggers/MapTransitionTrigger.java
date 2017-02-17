package map.triggers;

import main.Game;
import map.PathDirection;
import pattern.map.MapTransitionMatcher;
import trainer.CharacterData;
import util.JsonUtils;

class MapTransitionTrigger extends Trigger {
	private final MapTransitionMatcher mapTransitionMatcher;

	static String getTriggerSuffix(String contents) {
		MapTransitionMatcher matcher = JsonUtils.deserialize(contents, MapTransitionMatcher.class);
		return matcher.getPreviousMap() + "_" + matcher.getNextMap() + "_" + matcher.getNextEntranceName();
	}

	MapTransitionTrigger(String contents, String condition) {
		super(TriggerType.MAP_TRANSITION, contents, condition);

		this.mapTransitionMatcher = JsonUtils.deserialize(contents, MapTransitionMatcher.class);
	}
	
	protected void executeTrigger() {
		CharacterData player = Game.getPlayer();
		player.setMap(mapTransitionMatcher);
		mapTransitionMatcher.setTransitionIndex();

		PathDirection direction = mapTransitionMatcher.getDirection();
		if (direction != null && direction != PathDirection.WAIT) {
			player.setDirection(direction.getDirection());
		}

		if (mapTransitionMatcher.isDeathPortal()) {
			Game.getPlayer().setPokeCenter(mapTransitionMatcher);
		}

		player.setMapReset(true);
	}
}
