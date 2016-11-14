package pattern;

import map.entity.EntityAction;
import map.entity.EntityAction.TriggerAction;
import map.triggers.TriggerType;
import mapMaker.model.TriggerModel.TriggerModelType;
import util.JsonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventMatcher extends MultiPointEntityMatcher {
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

    public boolean isWildBattleTrigger() {
        return this.getWildBattleTriggerContents() != null;
    }

    public WildBattleMatcher getWildBattleTriggerContents() {
        for (EntityAction action : this.getActions()) {
            if (action instanceof TriggerAction) {
                TriggerAction triggerAction = (TriggerAction)action;
                if (triggerAction.getTriggerType() == TriggerType.WILD_BATTLE) {
                    String contents = triggerAction.getTriggerContents(this.name);
                    return JsonUtils.deserialize(contents, WildBattleMatcher.class);
                }
            }
        }

        return null;
    }
}
