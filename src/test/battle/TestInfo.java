package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.EffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import util.string.StringUtils;

class TestInfo {
    private PokemonNamesies attackingName;
    private PokemonNamesies defendingName;
    private AttackNamesies attackName;
    private PokemonManipulator manipulator;

    TestInfo() {
        this(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
    }

    TestInfo(PokemonNamesies attacking, PokemonNamesies defending) {
        this.attackingName = attacking;
        this.defendingName = defending;
        this.attackName = AttackNamesies.TACKLE;
        this.manipulator = PokemonManipulator.empty();
    }

    public void manipulate(TestBattle battle) {
        this.manipulator.manipulate(battle);

        // Setup the move, unless explicitly set to null to avoid this
        if (attackName != null) {
            battle.getAttacking().setupMove(attackName, battle);
        }
    }

    private void updateManipulator(PokemonManipulator manipulator) {
        this.manipulator = this.manipulator.add(manipulator);
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

    TestInfo fight(AttackNamesies attackingMove, AttackNamesies defendingMove) {
        return this.with((battle, attacking, defending) -> battle.fight(attackingMove, defendingMove));
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

    TestInfo attacking(PokemonNamesies pokemonNamesies, AbilityNamesies abilityNamesies) {
        return this.attacking(pokemonNamesies).attacking(abilityNamesies);
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
        return TestBattle.create(this.attackingName, this.defendingName);
    }

    // For when the result is the same with or without the ability
    public void doubleTake(AbilityNamesies abilityNamesies, PokemonManipulator samesies) {
        this.doubleTake(abilityNamesies, samesies, samesies);
    }

    public void doubleTake(AbilityNamesies abilityNamesies, PokemonManipulator withoutManipulator, PokemonManipulator withManipulator) {
        this.doubleTake(
                (battle, attacking, defending) -> defending.withAbility(abilityNamesies),
                (battle, attacking, defending) -> {
                    Assert.assertFalse(defending.hasAbility(abilityNamesies));
                    withoutManipulator.manipulate(battle, attacking, defending);
                },
                withManipulator
        );
    }

    public void doubleTake(PokemonManipulator manipulator, PokemonManipulator withoutManipulator, PokemonManipulator withManipulator) {
        TestBattle battle = this.createBattle();
        this.manipulate(battle);
        withoutManipulator.manipulate(battle);

        battle = this.createBattle();
        manipulator.manipulate(battle);
        this.manipulate(battle);
        withManipulator.manipulate(battle);
    }

    @Override
    public String toString() {
        return StringUtils.spaceSeparated(attackingName, defendingName, attackName);
    }
}
