package item.use;

import battle.Battle;
import battle.attack.Move;
import pokemon.ActivePokemon;
import trainer.Trainer;

public interface UseItem {
	boolean use(Trainer t, Battle b, ActivePokemon p, Move m);
}
