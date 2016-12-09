package pokemon.evolution;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.ItemNamesies;
import pokemon.ActivePokemon;

class MoveEvolution extends BaseEvolution {
    private static final long serialVersionUID = 1L;

    private final AttackNamesies move;

    MoveEvolution(int num, String m) {
        super(EvolutionMethod.MOVE, num);

        move = AttackNamesies.getValueOf(m);
    }

    @Override
    public Evolution getEvolution(ActivePokemon toEvolve, ItemNamesies useItem) {
        for (Move move : toEvolve.getActualMoves()) {
            if (move.getAttack().namesies() == this.move) {
                return this;
            }
        }

        return null;
    }
}
