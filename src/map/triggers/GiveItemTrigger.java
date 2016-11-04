package map.triggers;

import item.Item;
import main.Game;
import namesies.ItemNamesies;

public class GiveItemTrigger extends Trigger {

    private final ItemNamesies item;
    private final int quantity; // TODO: Quantity

    GiveItemTrigger(String itemName) {
        super(TriggerType.GIVE_ITEM, itemName);

        this.item = ItemNamesies.getValueOf(itemName);
        this.quantity = 1;
    }

    protected void executeTrigger() {
        Game.getPlayer().addItem(Item.getItem(this.item), quantity);
    }
}
