package map.triggers;

import main.Game;

public class LastPokeCenterTrigger extends Trigger
{

	int badgeIndex;

	public LastPokeCenterTrigger(String name, String contents)
	{
		super(name, contents);
	}

	public void execute(Game game)
	{
		super.execute(game);
		game.charData.setPokeCenter();
	}

	public String toString()
	{
		return "LastPokeCenterTrigger " + name;
	}
}
