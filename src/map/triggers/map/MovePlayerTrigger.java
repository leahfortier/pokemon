package map.triggers.map;

import main.Game;
import map.PathDirection;
import map.condition.Condition;
import map.entity.movable.PlayerEntity;
import map.triggers.Trigger;
import map.triggers.TriggerType;

public class MovePlayerTrigger extends Trigger {
    private final String path;

    public MovePlayerTrigger(String contents, Condition condition) {
        super(TriggerType.MOVE_PLAYER, contents, condition);

        this.path = PathDirection.defaultPath() + contents;
    }

    @Override
    protected void executeTrigger() {
        PlayerEntity playerEntity = Game.getPlayer().getEntity();
        playerEntity.setPath(this.path, playerEntity::unstall);
    }
}
