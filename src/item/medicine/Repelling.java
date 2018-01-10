package item.medicine;

import item.hold.HoldItem;
import item.use.PlayerUseItem;
import main.Game;
import message.Messages;
import trainer.player.RepelInfo;

public interface Repelling extends HoldItem, PlayerUseItem {
    int repelSteps();

    @Override
    default boolean use() {
        RepelInfo repelInfo = Game.getPlayer().getRepelInfo();
        if (repelInfo.isUsingRepel()) {
            return false;
        }

        int repelSteps = repelSteps();
        repelInfo.useItem(this.namesies(), repelSteps);
        Messages.add("Weak wild Pok\u00e9mon will not appear for " + repelSteps + " steps!");

        return true;
    }
}
