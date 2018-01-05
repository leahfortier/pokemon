package item.medicine;

import battle.Battle;
import battle.attack.Move;
import item.hold.HoldItem;
import item.use.BattleUseItem;
import item.use.PokemonUseItem;
import message.Messages;
import pokemon.ActivePokemon;

import java.util.List;

public interface AllPPHealer extends PokemonUseItem, BattleUseItem, HoldItem {
    int restoreAmount(Move toRestore);
    
    default boolean use(ActivePokemon p, List<Move> moves) {
        boolean changed = false;
        for (Move m : moves) {
            changed |= m.increasePP(this.restoreAmount(m));
        }
        
        if (changed) {
            Messages.add(p.getName() + "'s PP was restored!");
        }
        
        return changed;
    }
    
    default boolean use(ActivePokemon p) {
        return use(p, p.getActualMoves());
    }
    
    default boolean use(ActivePokemon p, Battle b) {
        return use(p, p.getMoves(b));
    }
    
    default boolean use(Battle b, ActivePokemon p, Move m) {
        return b == null ? use(p) : use(p, b);
    }
}
