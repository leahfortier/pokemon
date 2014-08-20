package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface EffectBlockerEffect 
{
	public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim);
}
