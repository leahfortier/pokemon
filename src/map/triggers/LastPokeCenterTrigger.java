package map.triggers;

import main.Game;

public class LastPokeCenterTrigger extends Trigger {
	public LastPokeCenterTrigger(String name, String contents) {
		super(name, contents);
	}

	public void execute() {
		super.execute();
		Game.getPlayer().setPokeCenter();
	}

	public String toString() {
		return "LastPokeCenterTrigger " + name;
	}
}
