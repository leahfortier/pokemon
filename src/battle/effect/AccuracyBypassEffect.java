package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface AccuracyBypassEffect
{
	// Attacker is the Pokemon whose accuracy is being evalualted and is the Pokemon on which this effect is attached to
	public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending);
}
