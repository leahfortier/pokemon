package item.use;

import item.hold.HoldItem;
import pokemon.ActivePokemon;
import battle.Battle;

public interface BallItem extends HoldItem
{
	public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b);
	public void afterCaught(ActivePokemon p);
}
