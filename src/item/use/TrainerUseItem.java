package item.use;

import battle.Battle;
import battle.attack.Move;
import pokemon.ActivePokemon;
import trainer.Trainer;

public interface TrainerUseItem extends UseItem {
	boolean use(Trainer t);
	default boolean applies(Trainer t) {
		return true;
	}

	default boolean apply(Trainer t) {
		return this.apply(t, null, null, null);
	}

	default boolean use(Trainer t, Battle b, ActivePokemon p, Move m) {
		return this.use(t);
	}

	default boolean applies(Trainer t, Battle b, ActivePokemon p, Move m) {
		return this.applies(t);
	}
}
