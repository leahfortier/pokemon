package pattern;

import main.Global;
import map.entity.EntityAction;
import map.entity.EntityAction.BattleAction;
import map.entity.EntityAction.ChoiceAction;
import map.entity.EntityAction.GlobalAction;
import map.entity.EntityAction.GroupTriggerAction;
import map.entity.EntityAction.TriggerAction;
import map.entity.EntityAction.UpdateAction;
import map.triggers.TriggerType;
import mapMaker.dialogs.action.ActionType;

import java.util.ArrayList;
import java.util.List;

public class ActionMatcher {
    public TriggerActionMatcher trigger;
    public BattleMatcher battle;
    public ChoiceActionMatcher choice;
    public String update;
    public String groupTrigger;
    public String global;

    private transient EntityAction entityAction;

    public ActionType getActionType() {
        if (!MapDataMatcher.hasOnlyOneNonEmpty(trigger, battle, choice, update, groupTrigger, global)) {
            Global.error("Can only have one nonempty field for ActionMatcher");
        }

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
        if (entityAction != null) {
            return entityAction;
        }

        ActionType actionType = this.getActionType();
        switch (actionType) {
            case TRIGGER:
                this.entityAction = new TriggerAction(trigger.getTriggerType(), trigger.triggerContents, condition);
                break;
            case BATTLE:
                this.entityAction = new BattleAction(battle);
                break;
            case UPDATE:
                this.entityAction = new UpdateAction(update);
                break;
            case GROUP_TRIGGER:
                this.entityAction = new GroupTriggerAction(groupTrigger);
                break;
            case CHOICE:
                this.entityAction = new ChoiceAction(choice);
                break;
            case GLOBAL:
                this.entityAction = new GlobalAction(global);
                break;
            default:
                Global.error("No action found.");
                break;
        }

        return this.entityAction;
    }

    public static class ChoiceActionMatcher {
        public String question;
        public ChoiceMatcher[] choices;

        public static class ChoiceMatcher {
            public String text;
            private ActionMatcher[] actions;

            public List<EntityAction> getActions() {
                List<EntityAction> actions = new ArrayList<>();
                for (ActionMatcher action : this.actions) {
                    actions.add(action.getAction(null));
                }

                return actions;
            }
        }
    }

    public static class BattleMatcher {
        public String name;
        public int cashMoney;
        public String[] pokemon;
        public String update;

        public BattleMatcher(String name, int cashMoney, String[] pokemon, String update) {
            this.name = name;
            this.cashMoney = cashMoney;
            this.pokemon = pokemon;
            this.update = update;
        }
    }

    public static class TriggerActionMatcher {
        private String triggerType;
        public String triggerContents;

        public TriggerActionMatcher(TriggerType triggerType, String triggerContents) {
            this.triggerType = triggerType.name();
            this.triggerContents = triggerContents;
        }

        public TriggerType getTriggerType() {
            return TriggerType.getTriggerType(this.triggerType);
        }
    }

    public static class UpdateMatcher {
        public String npcEntityName;
        public String interactionName;

        public UpdateMatcher(final String npcEntityName, final String interactionName) {
            this.npcEntityName = npcEntityName;
            this.interactionName = interactionName;
        }
    }
}
