package pattern.map;

import mapMaker.model.TriggerModel.TriggerModelType;
import namesies.ItemNamesies;
import pattern.generic.SinglePointEntityMatcher;

public class ItemMatcher extends SinglePointEntityMatcher {
    private String item;

    private transient ItemNamesies itemNamesies;

    public ItemMatcher(ItemNamesies itemName) {
        this.item = itemName.getName();
        this.itemNamesies = itemName;
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.ITEM;
    }

    @Override
    public String getBasicName() {
        return item;
    }

    public ItemNamesies getItem() {
        if (this.itemNamesies != null) {
            return this.itemNamesies;
        }

        return this.itemNamesies = ItemNamesies.getValueOf(this.item);
    }
}
