package map.entity;

import util.Point;

import java.util.List;

public class MiscEntity extends Entity {
    private final List<EntityAction> actions;
    
    private boolean dataCreated;
    
    public MiscEntity(String name, Point location, String condition, List<EntityAction> actions) {
        super(location, name, condition);
        this.actions = actions;
        this.dataCreated = false;
    }
    
    @Override
    public void addData() {
        if (dataCreated) {
            return;
        }
        
        EntityAction.addActionGroupTrigger(this.getEntityName(), this.getTriggerSuffix(), this.getConditionString(), this.actions);
        dataCreated = true;
    }
}
