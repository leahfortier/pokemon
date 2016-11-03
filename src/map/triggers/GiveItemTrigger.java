package map.triggers;

import item.Item;
import main.Game;
import namesies.ItemNamesies;
import trainer.CharacterData;
import util.StringUtils;

import java.util.ArrayList;

public class GiveItemTrigger extends Trigger {

    private final Item item;
    private final int quantity; // TODO: Quantity

    public GiveItemTrigger(String name, ItemNamesies itemNamesies) {
        super(name, StringUtils.empty());

        this.item = Item.getItem(itemNamesies);
        this.quantity = 1;
    }

    public GiveItemTrigger(String name, String contents) {
        super(name, contents);

        this.item = Item.getItem(ItemNamesies.getValueOf(contents));
        this.quantity = 1;
    }

    public void execute() {
        super.execute();
        Game.getPlayer().addItem(item, quantity);
    }
}
