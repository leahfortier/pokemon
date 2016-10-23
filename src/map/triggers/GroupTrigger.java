package map.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import main.Game;
import util.StringUtils;

public class GroupTrigger extends Trigger {
	
	public final List<String> triggers;
	
	public GroupTrigger(String name, String contents) {
		super(name, contents);
		triggers = new ArrayList<>();
		Matcher m = variablePattern.matcher(contents);
		while (m.find()) {
			if (m.group(1).equals("trigger")) {
				triggers.add(m.group(2));
			}
		}
	}

	@Override
	public void execute(Game game) {
		super.execute(game);
		for (String s: triggers) {
			Trigger trigger = game.data.getTrigger(s);
			if (trigger != null && trigger.isTriggered(game.characterData)) {
				trigger.execute(game);
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
