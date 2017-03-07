package item.medicine;

import item.hold.HoldItem;
import item.use.PlayerUseItem;
import main.Game;
import message.Messages;
import trainer.player.Player;

public interface Repelling extends HoldItem, PlayerUseItem {
    int repelSteps();

    default boolean use() {
        Player player = Game.getPlayer();
        if (player.isUsingRepel()) {
            return false;
        }

        int repelSteps = repelSteps();
        player.addRepelSteps(repelSteps);
        Messages.add("Weak wild Pok\u00e9mon will not appear for " + repelSteps + " steps!");

        return true;
    }
}
