package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface DefogRelease {
	void releaseDefog(Battle b, ActivePokemon victim);
}
