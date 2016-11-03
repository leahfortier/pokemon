package map.triggers;

import java.util.regex.Matcher;

import main.Game;

public class BadgeTrigger extends Trigger {
	private int badgeIndex;

	public BadgeTrigger(String name, String contents) {
		super(name, contents);

		Matcher m = variablePattern.matcher(contents);
		while (m.find()) {
			switch (m.group(1)) {
				case "badgeIndex":
					badgeIndex = Integer.parseInt(m.group(2));
					break;
			}
		}
	}

	public void execute() {
		super.execute();
		Game.getPlayer().giveBadge(badgeIndex);
	}

	public String toString() {
		return "BadgeTrigger: " + badgeIndex;
	}
}
