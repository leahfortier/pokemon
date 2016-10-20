package item.berry;

import pokemon.ActivePokemon;
import battle.Battle;
import battle.effect.generic.Effect.CastSource;

public interface GainableEffectBerry extends Berry
{
	public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source);
}
