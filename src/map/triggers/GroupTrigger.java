package map.triggers;

import main.Game;
import map.Condition;
import message.MessageUpdate;
import message.Messages;
import pattern.GroupTriggerMatcher;
import util.JsonUtils;
import util.StringUtils;

import java.util.List;

class GroupTrigger extends Trigger {
	public final List<String> triggers;

	static String getTriggerSuffix(String contents) {
		GroupTriggerMatcher matcher = JsonUtils.deserialize(contents, GroupTriggerMatcher.class);
		if (!StringUtils.isNullOrEmpty(matcher.getSuffix())) {
			return matcher.getSuffix();
		}

		return contents;
	}

	GroupTrigger(String contents, String condition) {
		this(contents, condition, JsonUtils.deserialize(contents, GroupTriggerMatcher.class));
	}

	private GroupTrigger(String contents, String condition, GroupTriggerMatcher matcher) {
		super(TriggerType.GROUP, contents, Condition.and(condition, matcher.getCondition()), matcher.getGlobals());
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
}
