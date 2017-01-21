package item.use;

import battle.Battle;
import pokemon.ActivePokemon;

public interface BallItem {
	double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b);
	default void afterCaught(ActivePokemon p) {}
}
