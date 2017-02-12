package item.medicine;

import item.hold.HoldItem;
import item.use.TrainerUseItem;
import main.Game;
import main.Global;
import message.Messages;
import trainer.CharacterData;
import trainer.Trainer;

public interface Repelling extends HoldItem, TrainerUseItem {
    int repelSteps();

    default boolean use(Trainer t) {
        if (t != Game.getPlayer()) {
            Global.error("Only the character should be using a Repel item");
            return false;
        }

        CharacterData player = (CharacterData) t;
        if (player.isUsingRepel()) {
            return false;
        }

        int repelSteps = repelSteps();
        player.addRepelSteps(repelSteps);
        Messages.add("Weak wild Pok\u00e9mon will not appear for " + repelSteps + " steps!");

        return true;
    }
}
