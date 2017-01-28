package test;

import battle.attack.AttackNamesies;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import util.StringUtils;

class TestInfo {
    PokemonNamesies attackingName;
    PokemonNamesies defendingName;
    AttackNamesies attackName;
    PokemonManipulator manipulator;

    TestInfo() {
        this.attackingName = PokemonNamesies.BULBASAUR;
        this.defendingName = PokemonNamesies.CHARMANDER;
        this.attackName = AttackNamesies.TACKLE;
        this.manipulator = PokemonManipulator.empty();
    }

    private void updateManipulator(PokemonManipulator manipulator) {
        this.manipulator = PokemonManipulator.combine(this.manipulator, manipulator);
    }

    TestInfo attacking(PokemonNamesies pokemonName) {
        this.attackingName = pokemonName;
        return this;
    }

    TestInfo defending(PokemonNamesies pokemonName) {
        this.defendingName = pokemonName;
        return this;
    }

    TestInfo with(AttackNamesies attackName) {
        this.attackName = attackName;
        return this;
    }

    TestInfo attacking(AbilityNamesies abilityNamesies) {
        this.updateManipulator(PokemonManipulator.giveAttackingAbility(abilityNamesies));
        return this;
    }

    TestInfo defending(AbilityNamesies abilityNamesies) {
        this.updateManipulator(PokemonManipulator.giveDefendingAbility(abilityNamesies));
        return this;
    }

    @Override
    public String toString() {
        return StringUtils.spaceSeparated(attackingName, defendingName, attackingName);
    }
}
