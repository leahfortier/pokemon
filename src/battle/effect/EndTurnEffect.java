package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface EndTurnEffect
{
	public void applyEndTurn(ActivePokemon victim, Battle b);		
}
