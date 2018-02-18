package pattern.action;

import main.Global;
import mapMaker.dialogs.action.ActionType;
import pattern.action.ActionMatcher2.BattleActionMatcher;
import pattern.action.ActionMatcher2.ChoiceActionMatcher2;
import pattern.action.ActionMatcher2.GlobalActionMatcher;
import pattern.action.ActionMatcher2.GroupTriggerActionMatcher;
import pattern.action.ActionMatcher2.TriggerActionMatcher2;
import pattern.action.ActionMatcher2.UpdateActionMatcher;
import util.GeneralUtils;
import util.StringUtils;

public class ActionMatcher {
    private TriggerActionMatcher trigger;
    private BattleMatcher battle;
    private ChoiceActionMatcher choice;
    private String update;
    private String groupTrigger;
    private String global;

    public ActionMatcher2 getNewMatcher() {
        this.confirmFormat();

        if (trigger != null) {
            return new TriggerActionMatcher2(trigger.getTriggerType(), trigger.getTriggerContents());
        } else if (battle != null) {
            return new BattleActionMatcher(battle.name, battle.cashMoney, battle.maxPokemonLimit, battle.pokemon, battle.update);
        } else if (update != null) {
            return new UpdateActionMatcher(update);
        } else if (groupTrigger != null) {
            return new GroupTriggerActionMatcher(groupTrigger);
        } else if (choice != null) {
            return new ChoiceActionMatcher2(choice.question, choice.choices);
        } else if (global != null) {
            return new GlobalActionMatcher(global);
        }

        Global.error("No action found.");
        return null;
    }

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
