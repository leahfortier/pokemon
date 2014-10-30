package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;


public interface DefogRelease 
{
	public void releaseDefog(Battle b, ActivePokemon victim);
}
