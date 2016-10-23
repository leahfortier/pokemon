package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface OpponentPowerChangeEffect {
	double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim);
}
