package item.use;

import battle.Battle;
import battle.attack.Move;
import pokemon.ActivePokemon;
import trainer.Trainer;

public interface BattleUseItem extends UseItem {
	boolean use(ActivePokemon p, Battle b);
	default boolean applies(ActivePokemon p, Battle b) {
		return true;
	}

	default boolean apply(ActivePokemon p, Battle b) {
		return this.apply((Trainer)b.getTrainer(p), b, p, null);
	}

	default boolean use(Trainer t, Battle b, ActivePokemon p, Move m) {
		return this.use(p, b);
	}

	default boolean applies(Trainer t, Battle b, ActivePokemon p, Move m) {
		return this.applies(p, b);
	}
}
