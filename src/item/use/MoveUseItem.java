package item.use;

import battle.Battle;
import battle.attack.Move;
import pokemon.ActivePokemon;

public interface MoveUseItem extends UseItem {
	boolean use(ActivePokemon p, Move m);

	default boolean use(Battle b, ActivePokemon p, Move m) {
		return this.use(p, m);
	}
}
