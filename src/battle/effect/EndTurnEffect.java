package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface EndTurnEffect
{
	public void apply(ActivePokemon victim, Battle b);		
}
