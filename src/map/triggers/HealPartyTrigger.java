package map.triggers;

import main.Game;

public class HealPartyTrigger extends Trigger {
	
	public HealPartyTrigger(String name, String contents) {
		super(name, contents);
	}

	@Override
	public void execute() {
		super.execute();
		
		Game.getPlayer().healAll();
	}

	@Override
	public String toString() {
		return "HealPartyTrigger: " + name;
	}
}
