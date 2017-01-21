package battle.effect;

import battle.Battle;
import main.Type;
import pokemon.ActivePokemon;

public interface DamageBlocker {
	boolean block(Type attacking, ActivePokemon victim);
	void alternateEffect(Battle b, ActivePokemon victim);
}
