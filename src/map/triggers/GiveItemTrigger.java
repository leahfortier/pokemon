package map.triggers;

import item.ItemNamesies;
import main.Game;
import map.condition.Condition;

public class GiveItemTrigger extends Trigger {
    private final ItemNamesies item;
    private final int quantity;

    public GiveItemTrigger(ItemNamesies itemNamesies, int quantity, Condition condition) {
        super(itemNamesies.name() + " " + quantity, condition);
        this.item = itemNamesies;
        this.quantity = quantity;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().getBag().addItem(this.item, quantity);
    }
}
