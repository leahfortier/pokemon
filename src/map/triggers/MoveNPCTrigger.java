package map.triggers;

import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.PathDirection;
import map.entity.movable.MovableEntity;
import map.entity.movable.PlayerEntity;
import pattern.MoveNPCTriggerMatcher;
import trainer.CharacterData;
import util.JsonUtils;
import util.Point;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

class MoveNPCTrigger extends Trigger {
    private final MoveNPCTriggerMatcher matcher;

    MoveNPCTrigger(String contents, String condition) {
        super(TriggerType.MOVE_NPC, contents, condition);
        this.matcher = JsonUtils.deserialize(contents, MoveNPCTriggerMatcher.class);
    }

    @Override
    protected void executeTrigger() {
        CharacterData player = Game.getPlayer();
        PlayerEntity playerEntity = player.getEntity();
        playerEntity.stall();

        MapData map = Game.getData().getMap(player.getMapName());
        MovableEntity entity = (MovableEntity)map.getEntity(this.matcher.getNpcEntityName());

        String path = getPath(entity, map);
        if (path == null) {
            Global.error("Cannot find valid path to exit :(");
        }

        entity.setTempPath(path, HaltTrigger::resume);
        HaltTrigger.addHaltTrigger();
    }

    private String getPath(MovableEntity entity, MapData map) {
        CharacterData player = Game.getPlayer();

        Point start = entity.getLocation();
        Point end = matcher.endLocationIsPlayer() ? player.getLocation() : map.getEntranceLocation(matcher.getEndEntranceName());

        Queue<PathState> queue = new ArrayDeque<>();
        Set<Point> visited = new HashSet<>();

        queue.add(new PathState(start, PathDirection.defaultPath()));
        visited.add(start);

        while (!queue.isEmpty()) {
            PathState currentState = queue.poll();
            if (end.equals(currentState.location)) {
                if (matcher.endLocationIsPlayer()) {
                    // Remove the last character so they're not on top of the player
                    return currentState.path.substring(0, currentState.path.length() - 1);
                } else {
                    return currentState.path + map.getExitDirection(matcher.getEndEntranceName()).getCharacter();
                }
            }

            for (Direction direction : Direction.values()) {
                Point newLocation = entity.getNewLocation(currentState.location, direction, map);
                if (newLocation != null && !visited.contains(newLocation)) {
                    visited.add(newLocation);
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
        Point location;
        String path;

        private PathState(Point location, String path) {
            this.location = location;
            this.path = path;
        }
    }

}
