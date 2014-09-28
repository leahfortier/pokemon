package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface OpponentPowerChangeEffect 
{
	public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim);
}
