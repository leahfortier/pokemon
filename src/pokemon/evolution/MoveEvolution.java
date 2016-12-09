package pokemon.evolution;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;

class MoveEvolution extends Evolution implements BaseEvolution {
    private static final long serialVersionUID = 1L;

    private int evolutionNumber;
    private AttackNamesies move;

    MoveEvolution(int num, String m) {
        evolutionNumber = num;

        move = AttackNamesies.getValueOf(m);
    }

    @Override
    public PokemonInfo getEvolution() {
        return PokemonInfo.getPokemonInfo(evolutionNumber);
    }

    @Override
    public Evolution getEvolution(EvolutionMethod type, ActivePokemon p, ItemNamesies use) {
        if (type != EvolutionMethod.MOVE) {
            return null;
        }

        for (Move m : p.getActualMoves()) {
            if (m.getAttack().namesies() == move) {
                return this;
            }
        }

        return null;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return new PokemonNamesies[] { PokemonInfo.getPokemonInfo(evolutionNumber).namesies() };
    }
}
