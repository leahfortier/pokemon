package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface PowerChangeEffect {
	double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim);
}
