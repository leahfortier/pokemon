package item.berry;

import pokemon.ActivePokemon;
import battle.Battle;

public interface HealthTriggeredBerry extends GainableEffectBerry
{
	public boolean useHealthTriggerBerry(Battle b, ActivePokemon user);
	public double healthTriggerRatio();
}
