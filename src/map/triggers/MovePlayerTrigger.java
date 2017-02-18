package map.triggers;

import main.Game;
import map.PathDirection;
import map.entity.movable.PlayerEntity;

class MovePlayerTrigger extends Trigger {
    private final String path;

    MovePlayerTrigger(String contents, String condition)	{
        super(TriggerType.MOVE_PLAYER, contents, condition);

        this.path = PathDirection.defaultPath() + contents;
    }

    @Override
    protected void executeTrigger() {
        PlayerEntity playerEntity = Game.getPlayer().getEntity();
        playerEntity.setPath(this.path, playerEntity::unstall);
    }
}
