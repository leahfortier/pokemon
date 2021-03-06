package map.triggers.map;

import main.Game;
import map.PathDirection;
import map.triggers.Trigger;
import pattern.map.MapTransitionMatcher;
import trainer.player.Player;

public class MapTransitionTrigger extends Trigger {
    private final MapTransitionMatcher mapTransitionMatcher;

    public MapTransitionTrigger(MapTransitionMatcher matcher) {
        this.mapTransitionMatcher = matcher;
    }

    @Override
    public void execute() {
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
}
