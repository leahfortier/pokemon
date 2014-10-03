package battle.effect;

import pokemon.ActivePokemon;
import pokemon.Stat;

public interface StatsCondition
{
	public int getStat(ActivePokemon user, Stat stat);
}
