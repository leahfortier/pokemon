package pattern.map;

import item.ItemNamesies;
import map.condition.Condition;
import map.entity.Entity;
import map.entity.ItemEntity;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.EntityMatcher;
import pattern.generic.SinglePointTriggerMatcher;

public class ItemMatcher extends SinglePointTriggerMatcher implements EntityMatcher {
    private String item;
    private boolean isHidden;
    
    public ItemMatcher(ItemNamesies itemName) {
        this.item = itemName.getName();
    }
    
    public ItemMatcher setHidden() {
        this.isHidden = true;
        return this;
    }
    
    @Override
    public TriggerModelType getTriggerModelType() {
        return this.isHidden() ? TriggerModelType.HIDDEN_ITEM : TriggerModelType.ITEM;
    }
    
    @Override
    public String getBasicName() {
        return item;
    }
    
    @Override
    public String getCondition() {
        return Condition.and(super.getCondition(), "!has" + this.getTriggerName());
    }
    
    public ItemNamesies getItem() {
        return ItemNamesies.getValueOf(this.item);
    }
    
    public boolean isHidden() {
        return this.isHidden;
    }
    
    @Override
    public Entity createEntity() {
        return new ItemEntity(
                this.getTriggerName(),
                this.getLocation(),
                this.getCondition(),
                this.getItem(),
                this.isHidden()
        );
    }
}
