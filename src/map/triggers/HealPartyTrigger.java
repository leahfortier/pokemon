package map.triggers;

import main.Game;

public class HealPartyTrigger extends Trigger {
	
	public HealPartyTrigger(String name, String contents) {
		super(name, contents);
	}

	@Override
	public void execute(Game game) {
		super.execute(game);
		
		game.characterData.healAll();
	}

	@Override
	public String toString() {
		return "HealPartyTrigger: " + name;
	}
}
