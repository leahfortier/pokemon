package item.medicine;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;
import item.hold.HoldItem;
import item.use.BattlePokemonUseItem;
import message.Messages;

import java.util.List;

public interface AllPPHealer extends BattlePokemonUseItem, HoldItem {
    int restoreAmount(Move toRestore);

    @Override
    default boolean use(ActivePokemon p, Battle b) {
        List<Move> moves = p.getMoves(b);

        boolean changed = false;
        for (Move m : moves) {
            changed |= m.increasePP(this.restoreAmount(m));
        }

        if (changed) {
            Messages.add(p.getName() + "'s PP was restored!");
        }

        return changed;
    }
}
