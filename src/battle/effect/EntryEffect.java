package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface EntryEffect {
	void enter(Battle b, ActivePokemon victim);
}
