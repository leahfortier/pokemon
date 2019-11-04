package pattern.map;

import item.ItemNamesies;
import map.condition.Condition;
import map.condition.Condition.GlobalCondition;
import map.condition.ConditionHolder.AndCondition;
import map.condition.ConditionHolder.NotCondition;
import map.entity.Entity;
import map.entity.ItemEntity;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.EntityMatcher.SingleEntityMatcher;
import pattern.generic.SinglePointTriggerMatcher;

public class ItemMatcher extends SinglePointTriggerMatcher implements SingleEntityMatcher {
    private ItemNamesies item;
    private boolean isHidden;

    public ItemMatcher(ItemNamesies itemName) {
        this.item = itemName;
    }

    public void setHidden() {
        this.isHidden = true;
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return this.isHidden() ? TriggerModelType.HIDDEN_ITEM : TriggerModelType.ITEM;
    }

    @Override
    public String getBasicName() {
        return item.name();
    }

    @Override
    public Condition getCondition() {
        return new AndCondition(super.getCondition(), new NotCondition(new GlobalCondition(this.getTriggerName())));
    }

    public ItemNamesies getItem() {
        return this.item;
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
