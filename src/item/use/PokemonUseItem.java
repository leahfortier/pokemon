package item.use;

import battle.Battle;
import battle.attack.Move;
import pokemon.ActivePokemon;
import trainer.Trainer;

public interface PokemonUseItem extends UseItem {
	boolean use(ActivePokemon p);

	default boolean use(Trainer t, Battle b, ActivePokemon p, Move m) {
		return this.use(p);
	}
}
