package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface TargetSwapperEffect {
	boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent);
}
