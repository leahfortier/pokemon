package map.triggers;

import item.ItemNamesies;
import main.Game;

public class GiveItemTrigger extends Trigger {

    private final ItemNamesies item;
    private final int quantity; // TODO: Quantity

    GiveItemTrigger(String itemName, String condition) {
        super(TriggerType.GIVE_ITEM, itemName, condition);

        this.item = ItemNamesies.getValueOf(itemName);
        this.quantity = 1;
    }

    protected void executeTrigger() {
        Game.getPlayer().addItem(this.item.getItem(), quantity);
    }
}
