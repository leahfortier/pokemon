package item.use;

import battle.Battle;
import battle.attack.Move;
import item.ItemInterface;
import battle.ActivePokemon;

public interface UseItem extends ItemInterface {
    boolean use(Battle b, ActivePokemon p, Move m);
}
