package map.triggers;

import item.ItemNamesies;
import main.Game;
import map.condition.Condition;

public class UseItemTrigger extends Trigger {
    private final ItemNamesies useItem;

    protected UseItemTrigger(String contents, Condition condition) {
        super(TriggerType.USE_ITEM, contents, condition);

        this.useItem = ItemNamesies.valueOf(contents);
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().getBag().useItem(this.useItem);
    }
}
