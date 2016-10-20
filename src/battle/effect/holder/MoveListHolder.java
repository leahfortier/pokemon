package battle.effect.holder;

import pokemon.ActivePokemon;
import battle.Move;

public interface MoveListHolder {
	public Move[] getMoveList(ActivePokemon p, Move[] moves);
}
