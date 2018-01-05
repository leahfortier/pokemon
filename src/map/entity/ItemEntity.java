package map.entity;

import gui.GameData;
import gui.TileSet;
import item.ItemNamesies;
import item.use.TechnicalMachine;
import main.Game;
import map.Direction;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import pattern.GroupTriggerMatcher;
import trainer.player.medal.MedalTheme;
import util.Point;
import util.SerializationUtils;
import util.StringUtils;

import java.awt.image.BufferedImage;

public class ItemEntity extends Entity {
    private final ItemNamesies itemName;
    private final boolean isHidden;
    private final boolean isTM;
    
    private boolean hasTriggered;
    private boolean dataCreated;
    
    public ItemEntity(String name, Point location, String condition, ItemNamesies item, boolean isHidden) {
        super(location, name, condition);
        this.itemName = item;
        this.isHidden = isHidden;
        this.isTM = this.itemName.getItem() instanceof TechnicalMachine;
        
        this.hasTriggered = false;
        this.dataCreated = false;
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
    public boolean isVisible() {
        return super.isVisible() && !hasTriggered;
    }
    
    @Override
    protected BufferedImage getFrame() {
        return this.isHidden ? null : (isTM ? TileSet.TM_ITEM_POKEBALL : TileSet.ITEM_POKEBALL);
    }
    
    @Override
    public void getAttention(Direction direction) {
        hasTriggered = true;
    }
    
    @Override
    public void reset() {
        hasTriggered = false;
    }
    
    @Override
    public void addData() {
        if (dataCreated) {
            return;
        }
        
        GameData data = Game.getData();
        final String itemTriggerSuffix = "ItemEntity_" + TriggerType.GIVE_ITEM.getTriggerName(this.itemName.getName());
        final String itemTriggerName = TriggerType.GROUP.getTriggerNameFromSuffix(itemTriggerSuffix);
        
        // Create a universal trigger for this item
        if (!data.hasTrigger(itemTriggerName)) {
            String itemDialogue =
                    "You found " +
                            (isTM
                                    ? "the " + this.itemName.getName()
                                    : StringUtils.articleString(this.itemName.getName())
                            ) +
                            "!";
            
            Trigger dialogue = TriggerType.DIALOGUE.createTrigger(itemDialogue, null);
            Trigger giveItem = TriggerType.GIVE_ITEM.createTrigger(this.itemName.getName(), null);
            
            
            GroupTriggerMatcher groupTriggerMatcher;
            if (this.isHidden) {
                Trigger medalTrigger = TriggerType.MEDAL_COUNT.createTrigger(MedalTheme.HIDDEN_ITEMS_FOUND.name(), null);
                groupTriggerMatcher = new GroupTriggerMatcher(itemTriggerSuffix, dialogue.getName(), giveItem.getName(), medalTrigger.getName());
            } else {
                groupTriggerMatcher = new GroupTriggerMatcher(itemTriggerSuffix, dialogue.getName(), giveItem.getName());
            }
            TriggerType.GROUP.createTrigger(SerializationUtils.getJson(groupTriggerMatcher), null);
        }
        
        // This trigger will only call the item trigger when the conditions apply
        GroupTriggerMatcher matcher = new GroupTriggerMatcher(this.getTriggerSuffix(), itemTriggerName);
        matcher.addGlobals("has" + this.getEntityName());
        
        TriggerType.GROUP.createTrigger(SerializationUtils.getJson(matcher), null);
        
        dataCreated = true;
    }
}
