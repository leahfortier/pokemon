package item.use;

import pokemon.ActivePokemon;
import battle.Battle;

public interface BallItem {
	double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b);
	void afterCaught(ActivePokemon p);
}
