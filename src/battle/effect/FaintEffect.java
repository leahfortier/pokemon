package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface FaintEffect {
	void deathwish(Battle b, ActivePokemon dead, ActivePokemon murderer);
}
