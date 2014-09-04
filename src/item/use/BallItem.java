package item.use;

import pokemon.ActivePokemon;
import battle.Battle;

public interface BallItem
{
	public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b);
	public void afterCaught(ActivePokemon p);
}
