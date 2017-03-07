package pokemon.evolution;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.ItemNamesies;
import pokemon.ActivePokemon;

class MoveEvolution extends BaseEvolution {
    private static final long serialVersionUID = 1L;

    private final AttackNamesies move;

    MoveEvolution(String namesies, String m) {
        super(EvolutionMethod.MOVE, namesies);

        move = AttackNamesies.getValueOf(m);
    }

    @Override
    public BaseEvolution getEvolution(ActivePokemon toEvolve, ItemNamesies useItem) {
        for (Move move : toEvolve.getActualMoves()) {
            if (move.getAttack().namesies() == this.move) {
                return this;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return EvolutionType.MOVE + " " + super.getEvolution().namesies() + " " + this.move;
    }

    @Override
    public String getString() {
        return "Learn " + move.getName();
    }
}
