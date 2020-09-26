package test.battle.manipulator;

import test.battle.TestBattle;
import test.pokemon.TestPokemon;

// Creates a PokemonManipulator that can be built using the same TestInfo methods,
// but doesn't set up a battle or anything
public class TestAction extends BaseTestAction<TestAction> implements PokemonManipulator {
    @Override
    protected TestAction getThis() {
        return this;
    }

    @Override
    public void manipulate(TestBattle battle, TestPokemon attacking, TestPokemon defending) {
        this.manipulator.manipulate(battle, attacking, defending);
    }
}
