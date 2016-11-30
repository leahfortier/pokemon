package map.triggers;

import main.Game;
import map.PathDirection;

class MovePlayerTrigger extends Trigger {
    private final String path;

    MovePlayerTrigger(String contents, String condition)	{
        super(TriggerType.MOVE_PLAYER, contents, condition);

        this.path = PathDirection.defaultPath() + contents;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().getEntity().setPath(this.path);
    }
}
