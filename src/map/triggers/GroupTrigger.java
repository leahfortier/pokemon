package map.triggers;

import main.Game;
import map.condition.ConditionHolder.AndCondition;
import map.condition.Condition;
import message.MessageUpdate;
import message.Messages;
import pattern.GroupTriggerMatcher;
import util.SerializationUtils;
import util.StringUtils;

import java.util.List;

class GroupTrigger extends Trigger {
    private final List<String> triggers;

    GroupTrigger(String contents, Condition condition) {
        this(contents, condition, SerializationUtils.deserializeJson(contents, GroupTriggerMatcher.class));
    }

    private GroupTrigger(String contents, Condition condition, GroupTriggerMatcher matcher) {
        super(TriggerType.GROUP, contents, new AndCondition(condition, matcher.getCondition()), matcher.getGlobals());
        this.triggers = matcher.getTriggers();
    }

    @Override
    protected void executeTrigger() {
        // Add all triggers in the group to the beginning of the message queue
        for (int i = triggers.size() - 1; i >= 0; i--) {
            String triggerName = triggers.get(i);
            Trigger trigger = Game.getData().getTrigger(triggerName);
            if (trigger != null && trigger.isTriggered()) {
                Messages.addToFront(new MessageUpdate().withTrigger(triggerName));
            }
        }
    }

    static String getTriggerSuffix(String contents) {
        GroupTriggerMatcher matcher = SerializationUtils.deserializeJson(contents, GroupTriggerMatcher.class);
        if (!StringUtils.isNullOrEmpty(matcher.getSuffix())) {
            return matcher.getSuffix();
        }

        return contents;
    }
}
