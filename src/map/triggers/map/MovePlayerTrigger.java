package map.triggers.map;

import main.Game;
import map.PathDirection;
import map.entity.movable.PlayerEntity;
import map.triggers.Trigger;

public class MovePlayerTrigger extends Trigger {
    private final String path;

    public MovePlayerTrigger(String path) {
        this.path = PathDirection.defaultPath() + path;
    }

    @Override
    public void execute() {
        PlayerEntity playerEntity = Game.getPlayer().getEntity();
        playerEntity.setPath(this.path, playerEntity::unstall);
    }
}
