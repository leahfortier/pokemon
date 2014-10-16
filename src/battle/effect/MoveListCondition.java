package battle.effect;

import pokemon.ActivePokemon;
import battle.Move;

public interface MoveListCondition 
{
	public Move[] getMoveList(ActivePokemon p, Move[] moves);
}
