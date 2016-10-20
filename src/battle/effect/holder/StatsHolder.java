package battle.effect.holder;

import pokemon.ActivePokemon;
import pokemon.Stat;

public interface StatsHolder {
	int getStat(ActivePokemon user, Stat stat);
}
