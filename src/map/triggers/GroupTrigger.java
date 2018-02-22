package map.triggers;

import map.condition.Condition;
import map.condition.ConditionHolder.AndCondition;
import message.MessageUpdate;
import message.Messages;
import pattern.GroupTriggerMatcher;
import util.StringUtils;

import java.util.List;

public class GroupTrigger extends Trigger {
    private final List<Trigger> triggers;

    public GroupTrigger(GroupTriggerMatcher matcher, Condition condition) {
        super(getTriggerSuffix(matcher), new AndCondition(condition, matcher.getCondition()));
        this.triggers = matcher.getTriggers();
    }

    @Override
    public void execute() {
        // Add all triggers in the group to the beginning of the message queue
        for (int i = triggers.size() - 1; i >= 0; i--) {
            Trigger trigger = triggers.get(i);
            if (trigger != null && trigger.isTriggered()) {
                Messages.addToFront(new MessageUpdate().withTrigger(trigger));
            }
        }
    }

    private static String getTriggerSuffix(GroupTriggerMatcher matcher) {
        if (!StringUtils.isNullOrEmpty(matcher.getSuffix())) {
            return matcher.getSuffix();
        }

        return matcher.getJson();
    }
}
