package item.berry;

import pokemon.ActivePokemon;
import battle.Battle;

public interface GainableEffectBerry extends Berry
{
	public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp);
}
