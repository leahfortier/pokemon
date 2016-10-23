package battle.effect;

import main.Type;
import pokemon.ActivePokemon;
import battle.Battle;

public interface DamageBlocker {
	boolean block(Type attacking, ActivePokemon victim);
	void alternateEffect(Battle b, ActivePokemon victim);
}
