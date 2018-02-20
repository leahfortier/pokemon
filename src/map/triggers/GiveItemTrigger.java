package map.triggers;

import item.ItemNamesies;
import main.Game;

public class GiveItemTrigger extends Trigger {
    private final ItemNamesies item;
    private final int quantity;

    public GiveItemTrigger(ItemNamesies itemNamesies, int quantity) {
        super(itemNamesies.name() + "_" + quantity);
        this.item = itemNamesies;
        this.quantity = quantity;
    }

    @Override
    public void execute() {
        Game.getPlayer().getBag().addItem(this.item, quantity);
    }
}
