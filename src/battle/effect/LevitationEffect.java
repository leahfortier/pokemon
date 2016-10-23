package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface LevitationEffect {
	void fall(Battle b, ActivePokemon fallen);
}
