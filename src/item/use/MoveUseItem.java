package item.use;

import battle.Battle;
import battle.attack.Move;
import pokemon.ActivePokemon;
import trainer.Trainer;

public interface MoveUseItem extends UseItem {
	boolean use(ActivePokemon p, Move m);
	default boolean applies(ActivePokemon p, Move m) {
		return true;
	}

	default boolean use(Trainer t, Battle b, ActivePokemon p, Move m) {
		return this.use(p, m);
	}

	default boolean applies(Trainer t, Battle b, ActivePokemon p, Move m) {
		return this.applies(p, m);
	}
}
