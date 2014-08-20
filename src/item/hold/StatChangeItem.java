package item.hold;

import pokemon.ActivePokemon;
import pokemon.Stat;

public interface StatChangeItem
{
	public double getMultiplier(Stat s, ActivePokemon p);
}
