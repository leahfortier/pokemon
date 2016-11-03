package map.triggers;

import main.Game;

public class BadgeTrigger extends Trigger {
	private int badgeIndex;

	public BadgeTrigger(String name, String contents) {
		super(name, contents);
		this.badgeIndex = Integer.parseInt(contents);
	}

	public void execute() {
		super.execute();
		Game.getPlayer().giveBadge(badgeIndex);
	}

	public String toString() {
		return "BadgeTrigger: " + badgeIndex;
	}
}
