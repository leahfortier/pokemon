package battle.effect;

import java.util.List;

import pokemon.ActivePokemon;
import battle.Move;

public interface MoveListCondition 
{
	public List<Move> getMoveList(ActivePokemon p, List<Move> moves);
}
