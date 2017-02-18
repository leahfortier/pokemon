package item.use;

import battle.Battle;
import battle.attack.Move;
import pokemon.ActivePokemon;
import trainer.Trainer;

public interface BattleUseItem extends UseItem {
	boolean use(ActivePokemon p, Battle b);

	default boolean use(Battle b, ActivePokemon p, Move m) {
		return this.use(p, b);
	}
}
