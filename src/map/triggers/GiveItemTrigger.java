package map.triggers;

import item.ItemNamesies;
import main.Game;

public class GiveItemTrigger extends Trigger {
    private final ItemNamesies item;
    private final int quantity;

    public GiveItemTrigger(ItemNamesies itemNamesies, int quantity) {
        super(itemNamesies.name() + " " + quantity);
        this.item = itemNamesies;
        this.quantity = quantity;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().getBag().addItem(this.item, quantity);
    }
}
