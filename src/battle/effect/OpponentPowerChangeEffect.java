package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface OpponentPowerChangeEffect 
{
	public double getOppMultiplier(Battle b, ActivePokemon user, ActivePokemon victim);
}
