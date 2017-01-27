package battle.effect;

import battle.Battle;
import type.Type;
import pokemon.ActivePokemon;

public interface DamageBlocker {
	boolean block(Type attacking, ActivePokemon victim);
	void alternateEffect(Battle b, ActivePokemon victim);
}
