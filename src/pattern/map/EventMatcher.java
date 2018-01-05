package pattern.map;

import map.entity.EntityAction;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.action.ActionMatcher;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventMatcher extends MultiPointTriggerMatcher {
    private String name;
    private ActionMatcher[] actions;
    
    public EventMatcher(String name, String condition, ActionMatcher[] actions) {
        this.name = name;
        this.actions = actions;
        
        super.setCondition(condition);
    }
    
    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.EVENT;
    }
    
    @Override
    public String getBasicName() {
        return name;
    }
    
    public List<ActionMatcher> getActionMatcherList() {
        return Arrays.asList(this.actions);
    }
    
    public List<EntityAction> getActions() {
        List<EntityAction> entityActions = new ArrayList<>();
        for (ActionMatcher matcher : this.actions) {
            entityActions.add(matcher.getAction(this.getCondition()));
        }
        
        return entityActions;
    }
}
