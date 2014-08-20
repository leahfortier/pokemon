package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface PowerChangeEffect 
{
	public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim);
}
