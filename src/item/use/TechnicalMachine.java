package item.use;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import main.Global;
import pokemon.ActivePokemon;

import java.util.List;

public interface TechnicalMachine extends MoveUseItem {
    AttackNamesies getAttack();
    
    default boolean use(ActivePokemon p, Move m) {
        AttackNamesies attack = getAttack();
        
        // Cannot learn if you already know the move
        if (p.hasActualMove(attack)) {
            return false;
        }
        
        // Cannot learn if the TM is not compatible with the Pokemon
        if (!p.getPokemonInfo().canLearnMove(attack)) {
            return false;
        }
        
        Move tmMove = new Move(attack);
        List<Move> moveList = p.getActualMoves();
        
        // If they don't have a full move list, append to the end
        if (moveList.size() < Move.MAX_MOVES) {
            p.addMove(tmMove, moveList.size(), false);
            return true;
        }
        
        // Otherwise, go through their moves and find the one that matches and replace with the TM move
        for (int i = 0; i < moveList.size(); i++) {
            if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
                p.addMove(tmMove, i, false);
                return true;
            }
        }
        
        // Did not find move to replace -- throw error
        Global.error(p.getName() + " does not have move to replace " + m.getAttack().getName());
        return false;
    }
    
    default String getImageName() {
        return this.getAttack().getAttack().getActualType().getName().toLowerCase() + "tm";
    }
}
