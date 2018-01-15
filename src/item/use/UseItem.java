package item.use;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;
import item.ItemInterface;

public interface UseItem extends ItemInterface {
    boolean use(Battle b, ActivePokemon p, Move m);
}
