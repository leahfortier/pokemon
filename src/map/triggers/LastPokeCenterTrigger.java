package map.triggers;

import main.Game;

public class LastPokeCenterTrigger extends Trigger {
	public LastPokeCenterTrigger(String name, String contents) {
		super(name, contents);
	}

	public void execute(Game game) {
		super.execute(game);
		game.characterData.setPokeCenter();
	}

	public String toString() {
		return "LastPokeCenterTrigger " + name;
	}
}
