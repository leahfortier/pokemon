package item.use;

import battle.attack.Move;
import pokemon.ActivePokemon;

public interface MoveUseItem extends UseItem {
	boolean use(ActivePokemon p, Move m);
}
