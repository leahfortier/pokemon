package map.triggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import main.Game;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.GroupTriggerMatcher;
import util.StringUtils;

public class GroupTrigger extends Trigger {
	
	public final List<String> triggers;

	public GroupTrigger(String name, GroupTriggerMatcher matcher) {
		super(name, matcher.condition);
		super.globals.addAll(matcher.globals);

		this.triggers = new ArrayList<>(Arrays.asList(matcher.triggers));
	}

	public GroupTrigger(String name, String contents) {
		super(name, contents);

		GroupTriggerMatcher matcher = AreaDataMatcher.deserialize(contents, GroupTriggerMatcher.class);
		this.triggers = new ArrayList<>(Arrays.asList(matcher.triggers));
	}

	@Override
	public void execute(Game game) {
		super.execute(game);
		for (String triggerName: triggers) {
			Trigger trigger = game.data.getTrigger(triggerName);
			if (trigger != null && trigger.isTriggered(game.characterData)) {
				Messages.addMessage(new MessageUpdate(StringUtils.empty(), triggerName, Update.TRIGGER));
			}
		}
	}
	
	@Override
	public String toString() {
		return "GroupTrigger: " + name + " triggers: " + triggers.toString();
	}
	
	@Override
	public String triggerDataAsString() {
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		
		for (String trigger: triggers) {
			StringUtils.appendLine(ret, "\ttrigger: " + trigger);
		}
		
		return ret.toString();
	}
}
