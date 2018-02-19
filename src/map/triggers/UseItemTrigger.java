package map.triggers;

import item.ItemNamesies;
import main.Game;

public class UseItemTrigger extends Trigger {
    private final ItemNamesies useItem;

    public UseItemTrigger(ItemNamesies itemNamesies) {
        super(itemNamesies.name());
        this.useItem = itemNamesies;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().getBag().useItem(this.useItem);
    }
}
