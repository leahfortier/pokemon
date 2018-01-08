package map.triggers.map;

import main.Game;
import map.PathDirection;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import pattern.map.MapTransitionMatcher;
import trainer.player.Player;
import util.SerializationUtils;

public class MapTransitionTrigger extends Trigger {
    private final MapTransitionMatcher mapTransitionMatcher;

    public MapTransitionTrigger(String contents, String condition) {
        super(TriggerType.MAP_TRANSITION, contents, condition);

        this.mapTransitionMatcher = SerializationUtils.deserializeJson(contents, MapTransitionMatcher.class);
    }

    protected void executeTrigger() {
        Player player = Game.getPlayer();
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

    public static String getTriggerSuffix(String contents) {
        MapTransitionMatcher matcher = SerializationUtils.deserializeJson(contents, MapTransitionMatcher.class);
        return matcher.getPreviousMap() + "_" + matcher.getNextMap() + "_" + matcher.getNextEntranceName();
    }
}
