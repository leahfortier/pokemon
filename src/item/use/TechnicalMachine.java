package item.use;

import battle.ActivePokemon;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import main.Global;
import pokemon.active.MoveList;

public interface TechnicalMachine extends MoveUseItem {
    AttackNamesies getAttack();

    @Override
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
        MoveList moveList = p.getActualMoves();

        // If they don't have a full move list, append to the end
        if (moveList.size() < MoveList.MAX_MOVES) {
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

    @Override
    default String getImageName() {
        return this.getAttack().getNewAttack().getActualType().getName().toLowerCase() + "tm";
    }
}
