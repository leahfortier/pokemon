package map.entity;

import gui.TileSet;
import item.ItemNamesies;
import item.use.TechnicalMachine;
import map.Direction;
import map.condition.Condition;
import map.triggers.DialogueTrigger;
import map.triggers.GiveItemTrigger;
import map.triggers.GlobalTrigger;
import map.triggers.GroupTrigger;
import map.triggers.MedalCountTrigger;
import map.triggers.Trigger;
import trainer.player.medal.MedalTheme;
import util.Point;
import util.string.StringUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ItemEntity extends Entity {
    private final ItemNamesies itemName;
    private final boolean isHidden;
    private final boolean isTM;

    private boolean hasTriggered;
    private Trigger trigger;

    public ItemEntity(String name, Point location, Condition condition, ItemNamesies item, boolean isHidden) {
        super(location, name, condition);
        this.itemName = item;
        this.isHidden = isHidden;
        this.isTM = this.itemName.getItem() instanceof TechnicalMachine;

        this.hasTriggered = false;
    }

    public boolean isHiddenItem() {
        return this.isHidden;
    }

    @Override
    public boolean isPassable() {
        return this.isHidden;
    }

    @Override
    public boolean isHighPriorityEntity() {
        return !this.isHidden;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && !hasTriggered;
    }

    @Override
    protected BufferedImage getFrame() {
        if (this.isHidden) {
            return null;
        } else if (this.isTM) {
            return TileSet.TM_ITEM_POKEBALL;
        } else {
            return TileSet.ITEM_POKEBALL;
        }
    }

    @Override
    public void getAttention(Direction direction) {
        hasTriggered = true;
    }

    @Override
    public void reset() {
        hasTriggered = false;
    }

    private String getItemNameWithArticle() {
        return isTM
               ? "the " + this.itemName.getName()
               : StringUtils.articleString(this.itemName.getName());
    }

    @Override
    public Trigger getTrigger() {
        if (trigger == null) {
            List<Trigger> triggers = new ArrayList<>();
            triggers.add(new GlobalTrigger(this.getEntityName()));
            triggers.add(new DialogueTrigger("You found " + this.getItemNameWithArticle() + "!"));
            triggers.add(new GiveItemTrigger(this.itemName, 1));
            if (this.isHidden) {
                triggers.add(new MedalCountTrigger(MedalTheme.HIDDEN_ITEMS_FOUND));
            }

            this.trigger = new GroupTrigger(this.getCondition(), triggers);
        }

        return trigger;
    }
}
