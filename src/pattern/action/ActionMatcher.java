package pattern.action;

import main.Global;
import map.entity.EntityAction;
import map.entity.EntityAction.BattleAction;
import map.entity.EntityAction.ChoiceAction;
import map.entity.EntityAction.GlobalAction;
import map.entity.EntityAction.GroupTriggerAction;
import map.entity.EntityAction.TriggerAction;
import map.entity.EntityAction.UpdateAction;
import mapMaker.dialogs.action.ActionType;
import util.GeneralUtils;
import util.StringUtils;

public class ActionMatcher {
    private TriggerActionMatcher trigger;
    private BattleMatcher battle;
    private ChoiceActionMatcher choice;
    private String update;
    private String groupTrigger;
    private String global;

    private void confirmFormat() {
        if (!GeneralUtils.hasOnlyOneNonEmpty(trigger, battle, choice, update, groupTrigger, global)) {
            Global.error("Can only have one nonempty field for ActionMatcher");
        }
    }

    public ActionType getActionType() {
        this.confirmFormat();

        if (trigger != null) {
            return ActionType.TRIGGER;
        } else if (battle != null) {
            return ActionType.BATTLE;
        } else if (update != null) {
            return ActionType.UPDATE;
        } else if (groupTrigger != null) {
            return ActionType.GROUP_TRIGGER;
        } else if (choice != null) {
            return ActionType.CHOICE;
        } else if (global != null) {
            return ActionType.GLOBAL;
        }

        Global.error("No action found.");
        return null;
    }

    public EntityAction getAction(final String condition) {
        ActionType actionType = this.getActionType();
        switch (actionType) {
            case TRIGGER:
                return new TriggerAction(trigger.getTriggerType(), trigger.getTriggerContents(), condition);
            case BATTLE:
                return new BattleAction(battle);
            case UPDATE:
                return new UpdateAction(update);
            case GROUP_TRIGGER:
                return new GroupTriggerAction(groupTrigger);
            case CHOICE:
                return new ChoiceAction(choice);
            case GLOBAL:
                return new GlobalAction(global);
            default:
                Global.error("No action found.");
                return null;
        }
    }

    public TriggerActionMatcher getTrigger() {
        return this.trigger;
    }

    public void setTrigger(TriggerActionMatcher matcher) {
        this.trigger = matcher;

        this.confirmFormat();
    }

    public BattleMatcher getBattle() {
        return this.battle;
    }

    public void setBattle(BattleMatcher matcher) {
        this.battle = matcher;

        this.confirmFormat();
    }

    public String getActionString() {
        switch (this.getActionType()) {
            case UPDATE:
                return this.update;
            case GLOBAL:
                return this.global;
            case GROUP_TRIGGER:
                return this.groupTrigger;
            default:
                Global.error("Invalid string action type " + this.getActionType());
                return StringUtils.empty();
        }
    }

    public void setActionString(String contents, ActionType actionType) {
        switch (actionType) {
            case UPDATE:
                this.update = contents;
                break;
            case GLOBAL:
                this.global = contents;
                break;
            case GROUP_TRIGGER:
                this.groupTrigger = contents;
                break;
            default:
                Global.error("Invalid string action type " + actionType);
                break;
        }

        this.confirmFormat();
    }

    public ChoiceActionMatcher getChoice() {
        return this.choice;
    }

    public void setChoice(ChoiceActionMatcher choice) {
        this.choice = choice;

        this.confirmFormat();
    }
}
