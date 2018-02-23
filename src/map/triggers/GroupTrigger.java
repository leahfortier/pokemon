package map.triggers;

import map.condition.Condition;
import message.MessageUpdate;
import message.Messages;

import java.util.Arrays;
import java.util.List;

public class GroupTrigger extends Trigger {
    private final List<Trigger> triggers;

    public GroupTrigger(Trigger... triggers) {
        this(null, triggers);
    }

    public GroupTrigger(Condition condition, Trigger... triggers) {
        this(condition, Arrays.asList(triggers));
    }

    public GroupTrigger(Condition condition, List<Trigger> triggers) {
        super(condition);
        this.triggers = triggers;
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
}
