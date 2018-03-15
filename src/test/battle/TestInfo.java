package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.EffectNamesies;
import item.ItemNamesies;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import test.TestPokemon;
import util.string.StringUtils;

class TestInfo {
    PokemonNamesies attackingName;
    PokemonNamesies defendingName;
    AttackNamesies attackName;
    private PokemonManipulator manipulator;

    TestInfo() {
        this.attackingName = PokemonNamesies.BULBASAUR;
        this.defendingName = PokemonNamesies.CHARMANDER;
        this.attackName = AttackNamesies.TACKLE;
        this.manipulator = PokemonManipulator.empty();
    }

    public void manipulate(TestBattle battle) {
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        this.manipulator.manipulate(battle, attacking, defending);

        // Setup the move, unless explicitly set to null to avoid this
        if (attackName != null) {
            attacking.setupMove(attackName, battle);
        }
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

    TestInfo fight(AttackNamesies attackingName, AttackNamesies defendingName) {
        return this.with((battle, attacking, defending) -> battle.fight(attackingName, defendingName));
    }

    TestInfo attackingFight(AttackNamesies attackName) {
        return this.with((battle, attacking, defending) -> battle.attackingFight(attackName));
    }

    TestInfo defendingFight(AttackNamesies attackName) {
        return this.with((battle, attacking, defending) -> battle.defendingFight(attackName));
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

    public TestBattle createBattle() {
        TestBattle battle = TestBattle.create(this.attackingName, this.defendingName);
        battle.getAttacking().setupMove(this.attackName, battle);
        return battle;
    }

    @Override
    public String toString() {
        return StringUtils.spaceSeparated(attackingName, defendingName, attackName);
    }
}
