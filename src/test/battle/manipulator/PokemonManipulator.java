package test.battle.manipulator;

import test.battle.TestBattle;
import test.pokemon.TestPokemon;

@FunctionalInterface
public interface PokemonManipulator {
    void manipulate(TestBattle battle, TestPokemon attacking, TestPokemon defending);

    default void manipulate(TestBattle battle) {
        this.manipulate(battle, battle.getAttacking(), battle.getDefending());
    }

    // Returns a new manipulator combining the actions of this and the parameter manipulators (in that order)
    // Note: This is NOT a mutable operation (does not alter the current manipulator)
    default PokemonManipulator add(PokemonManipulator... manipulators) {
        return (battle, attacking, defending) -> {
            this.manipulate(battle, attacking, defending);
            for (PokemonManipulator manipulator : manipulators) {
                manipulator.manipulate(battle, attacking, defending);
            }
        };
    }

    static PokemonManipulator empty() {
        return (battle, attacking, defending) -> {};
    }

    @FunctionalInterface
    interface SingleManipulator {
        void manipulate(TestBattle b, TestPokemon p);

        static SingleManipulator empty() {
            return (b, p) -> {};
        }
    }
}
