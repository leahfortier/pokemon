package map.triggers;

import item.ItemNamesies;
import main.Game;
import map.condition.Condition;

public class UseItemTrigger extends Trigger {
    private final ItemNamesies useItem;

    public UseItemTrigger(ItemNamesies itemNamesies, Condition condition) {
        super(itemNamesies.name(), condition);
        this.useItem = itemNamesies;
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().getBag().useItem(this.useItem);
    }
}
