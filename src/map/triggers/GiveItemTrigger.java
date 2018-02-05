package map.triggers;

import item.ItemNamesies;
import main.Game;
import map.condition.Condition;

class GiveItemTrigger extends Trigger {
    private final ItemNamesies item;
    private final int quantity; // TODO: Quantity

    GiveItemTrigger(String itemName, Condition condition) {
        super(TriggerType.GIVE_ITEM, itemName, condition);

        this.item = ItemNamesies.getValueOf(itemName);
        this.quantity = 1;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().getBag().addItem(this.item, quantity);
    }
}
