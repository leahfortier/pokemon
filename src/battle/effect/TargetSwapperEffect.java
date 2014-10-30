package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface TargetSwapperEffect
{
	public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent);
}
