package item.use;

import battle.Battle;
import battle.attack.Move;
import item.bag.Bag;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import trainer.Trainer;
import util.StringUtils;

public interface UseItem {
	String getName();

	boolean use(Trainer t, Battle b, ActivePokemon p, Move m);
	default boolean applies(Trainer t, Battle b, ActivePokemon p, Move m) {
		return true;
	}

	default boolean apply(Trainer t, Battle b, ActivePokemon p, Move m) {
		if (this.applies(t, b, p, m)) {
			Messages.add(new MessageUpdate(t.getName() + " used the " + this.getName() + "!"));
			this.use(t, b, p, m);
			return true;
		}

		return false;
	}

	default String getSuccessMessage(ActivePokemon p) {
		return StringUtils.empty();
	}
}
