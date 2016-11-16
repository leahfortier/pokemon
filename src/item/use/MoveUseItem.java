package item.use;

import pokemon.ActivePokemon;
import battle.attack.Move;

public interface MoveUseItem extends UseItem {
	boolean use(ActivePokemon p, Move m);
}
