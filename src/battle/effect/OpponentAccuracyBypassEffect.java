package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface OpponentAccuracyBypassEffect {
	// Attacker is the Pokemon whose accuracy is being evalualted, defender is the Pokemon on which this effect is attached to
	boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending);
}
