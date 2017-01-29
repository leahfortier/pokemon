package test;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
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

    TestInfo callMove(AttackNamesies attackName) {
        return this.with((battle, attacking, defending) -> attacking.callNewMove(battle, defending, new Move(attackName)));
    }

    TestInfo with(PokemonManipulator manipulator) {
        this.updateManipulator(manipulator);
        return this;
    }

    TestInfo attacking(AbilityNamesies abilityNamesies) {
        this.updateManipulator(PokemonManipulator.giveAttackingAbility(abilityNamesies));
        return this;
    }

    TestInfo attacking(PokemonNamesies pokemonNamesies, EffectNamesies effectNamesies) {
        return this.attacking(pokemonNamesies).attacking(effectNamesies);
    }

    TestInfo attacking(PokemonNamesies pokemonNamesies, ItemNamesies itemNamesies) {
        return this.attacking(pokemonNamesies).attacking(itemNamesies);
    }

    TestInfo defending(PokemonNamesies pokemonNamesies, ItemNamesies itemNamesies) {
        return this.defending(pokemonNamesies).defending(itemNamesies);
    }

    TestInfo defending(PokemonNamesies pokemonNamesies, AbilityNamesies abilityNamesies) {
        return this.defending(pokemonNamesies).defending(abilityNamesies);
    }

    TestInfo defending(AbilityNamesies abilityNamesies, EffectNamesies effectNamesies) {
        return this.defending(abilityNamesies).defending(effectNamesies);
    }

    TestInfo defending(AbilityNamesies abilityNamesies) {
        this.updateManipulator(PokemonManipulator.giveDefendingAbility(abilityNamesies));
        return this;
    }

    TestInfo attacking(ItemNamesies itemNamesies) {
        this.updateManipulator(PokemonManipulator.giveAttackingItem(itemNamesies));
        return this;
    }

    TestInfo defending(ItemNamesies itemNamesies) {
        this.updateManipulator(PokemonManipulator.giveDefendingItem(itemNamesies));
        return this;
    }

    TestInfo attacking(EffectNamesies effectNamesies) {
        this.updateManipulator(PokemonManipulator.giveAttackingEffect(effectNamesies));
        return this;
    }

    TestInfo defending(EffectNamesies effectNamesies) {
        this.updateManipulator(PokemonManipulator.giveDefendingEffect(effectNamesies));
        return this;
    }

    @Override
    public String toString() {
        return StringUtils.spaceSeparated(attackingName, defendingName, attackName);
    }
}
