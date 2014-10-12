package item.use;

import pokemon.ActivePokemon;
import battle.Move;

public interface MoveUseItem extends UseItem
{
	public boolean use(ActivePokemon p, Move m);
}
