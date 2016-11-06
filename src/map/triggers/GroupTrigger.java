package map.triggers;

import main.Game;
import map.Condition;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.GroupTriggerMatcher;
import util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupTrigger extends Trigger {
	public final List<String> triggers;

	static String getTriggerSuffix(String contents) {
		GroupTriggerMatcher matcher = AreaDataMatcher.deserialize(contents, GroupTriggerMatcher.class);
		if (!StringUtils.isNullOrEmpty(matcher.suffix)) {
			return matcher.suffix;
		}

		return contents;
	}

	GroupTrigger(String contents, String condition) {
		this(contents, condition, AreaDataMatcher.deserialize(contents, GroupTriggerMatcher.class));
	}

	private GroupTrigger(String contents, String condition, GroupTriggerMatcher matcher) {
		super(TriggerType.GROUP, contents, Condition.and(condition, matcher.condition), matcher.globals);
		this.triggers = new ArrayList<>(Arrays.asList(matcher.triggers));
	}

	@Override
	protected void executeTrigger() {
		for (String triggerName: triggers) {
			Trigger trigger = Game.getData().getTrigger(triggerName);
			if (trigger != null && trigger.isTriggered()) {
				Messages.addMessage(new MessageUpdate(StringUtils.empty(), triggerName, Update.TRIGGER));
			}
		}
	}
}
