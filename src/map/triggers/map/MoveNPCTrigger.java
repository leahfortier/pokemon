package map.triggers.map;

import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.PathDirection;
import map.condition.Condition;
import map.entity.movable.NPCEntity;
import map.entity.movable.PlayerEntity;
import map.triggers.HaltTrigger;
import map.triggers.Trigger;
import pattern.MoveNPCTriggerMatcher;
import trainer.player.Player;
import util.Point;
import util.SerializationUtils;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class MoveNPCTrigger extends Trigger {
    private final MoveNPCTriggerMatcher matcher;

    public MoveNPCTrigger(String contents, Condition condition) {
        this(SerializationUtils.deserializeJson(contents, MoveNPCTriggerMatcher.class), condition);
    }

    public MoveNPCTrigger(MoveNPCTriggerMatcher matcher, Condition condition) {
        super(matcher.getJson(), condition);
        this.matcher = matcher;
    }

    @Override
    protected void executeTrigger() {
        Player player = Game.getPlayer();
        PlayerEntity playerEntity = player.getEntity();
        playerEntity.stall();

        MapData map = Game.getData().getMap(player.getMapName());
        NPCEntity entity = (NPCEntity)map.getEntity(this.matcher.getNpcEntityName());

        String path = getPath(entity, map);
        if (path == null) {
            Global.error("Cannot find valid path to exit :(");
        }

        HaltTrigger.addHaltTrigger();
        entity.setTempPath(path, HaltTrigger::resume);
    }

    private String getPath(NPCEntity entity, MapData map) {
        Point start = entity.getLocation();
        Point end = matcher.endLocationIsPlayer()
                ? Game.getPlayer().getLocation()
                : map.getEntranceLocation(matcher.getEndEntranceName(), 0, 1);

        Queue<PathState> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.add(new PathState(start, PathDirection.defaultPath()));
        visited.add(start.toString());

        while (!queue.isEmpty()) {
            PathState currentState = queue.poll();

            if (!matcher.endLocationIsPlayer() && end.equals(currentState.location)) {
                PathDirection pathDirection = map.getExitDirection(matcher.getEndEntranceName());
                if (pathDirection != null) {
                    return currentState.path + pathDirection.getCharacter();
                }

                return currentState.path;
            }

            for (Direction direction : Direction.values()) {
                if (matcher.endLocationIsPlayer()) {
                    Point newLocation = Point.add(currentState.location, direction.getDeltaPoint());
                    if (newLocation.equals(end)) {
                        return currentState.path;
                    }
                }

                Point newLocation = entity.getNewLocation(currentState.location, direction, map);
                if (newLocation == null) {
                    continue;
                }

                if (!visited.contains(newLocation.toString())) {
                    visited.add(newLocation.toString());
                    queue.add(new PathState(
                            newLocation,
                            currentState.path + direction.getPathDirection().getCharacter()
                    ));
                }
            }
        }

        return null;
    }

    private class PathState {
        private final Point location;
        private final String path;

        private PathState(Point location, String path) {
            this.location = location;
            this.path = path;
        }
    }
}
