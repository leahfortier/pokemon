package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.EffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import test.TestPokemon;
import test.TestUtils;
import util.string.StringAppender;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.List;

class TestInfo {
    private PokemonNamesies attackingName;
    private PokemonNamesies defendingName;
    private PokemonManipulator setupManipulator;
    private PokemonManipulator manipulator;
    private boolean isTrainerBattle;
    private List<String> toString;

    TestInfo() {
        this(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
    }

    TestInfo(PokemonNamesies attacking, PokemonNamesies defending) {
        this.attackingName = attacking;
        this.defendingName = defending;
        this.setupManipulator = PokemonManipulator.empty();
        this.manipulator = PokemonManipulator.empty();
        this.isTrainerBattle = false;
        this.toString = new ArrayList<>();
    }

    public void manipulate(TestBattle battle) {
        this.manipulator.manipulate(battle);
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

    TestInfo setup(PokemonManipulator setupManipulator) {
        this.setupManipulator = this.setupManipulator.add(setupManipulator);
        return this;
    }

    TestInfo with(AttackNamesies attackName) {
        this.toString.add(attackName.getName());
        return this.with((battle, attacking, defending) -> attacking.setupMove(attackName, battle));
    }

    TestInfo asTrainerBattle() {
        this.isTrainerBattle = true;
        return this;
    }

    TestInfo fight(AttackNamesies attackingMove, AttackNamesies defendingMove) {
        this.toString.add("FIGHT[" + attackingMove.getName() + ", " + defendingMove.getName() + "]");
        return this.with((battle, attacking, defending) -> battle.fight(attackingMove, defendingMove));
    }

    TestInfo attackingFight(AttackNamesies attackName) {
        this.addString(true, attackName.getName());
        return this.with((battle, attacking, defending) -> battle.attackingFight(attackName));
    }

    TestInfo defendingFight(AttackNamesies attackName) {
        this.addString(false, attackName.getName());
        return this.with((battle, attacking, defending) -> battle.defendingFight(attackName));
    }

    TestInfo with(PokemonManipulator manipulator) {
        this.updateManipulator(manipulator);
        return this;
    }

    TestInfo attacking(AbilityNamesies abilityNamesies) {
        this.addString(true, abilityNamesies.getName());
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
        this.addString(false, abilityNamesies.getName());
        this.updateManipulator(PokemonManipulator.giveDefendingAbility(abilityNamesies));
        return this;
    }

    TestInfo attacking(ItemNamesies itemNamesies) {
        this.addString(true, itemNamesies.getName());
        this.updateManipulator(PokemonManipulator.giveAttackingItem(itemNamesies));
        return this;
    }

    TestInfo defending(ItemNamesies itemNamesies) {
        this.addString(false, itemNamesies.getName());
        this.updateManipulator(PokemonManipulator.giveDefendingItem(itemNamesies));
        return this;
    }

    TestInfo attacking(EffectNamesies effectNamesies) {
        this.addEffectString(true, effectNamesies);
        this.updateManipulator(PokemonManipulator.giveAttackingEffect(effectNamesies));
        return this;
    }

    TestInfo defending(EffectNamesies effectNamesies) {
        this.addEffectString(false, effectNamesies);
        this.updateManipulator(PokemonManipulator.giveDefendingEffect(effectNamesies));
        return this;
    }

    private void addEffectString(boolean attacking, EffectNamesies effectNamesies) {
        this.addString(attacking, StringUtils.properCase(effectNamesies.toString().toLowerCase().replaceAll("_", " ")));
    }

    private void addString(boolean attacking, String name) {
        this.toString.add((attacking ? "ATTACKING" : "DEFENDING") + "[" + name + "]");
    }

    public TestBattle createBattle() {
        TestBattle battle = TestBattle.create(this.isTrainerBattle, this.attackingName, this.defendingName);
        battle.getAttacking().withAbility(AbilityNamesies.NO_ABILITY);
        battle.getDefending().withAbility(AbilityNamesies.NO_ABILITY);
        this.setupManipulator.manipulate(battle);
        return battle;
    }

    // For when the result is the same with or without the ability
    public void doubleTakeSamesies(AbilityNamesies abilityNamesies, PokemonManipulator samesies) {
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

    public void doubleTakeSamesies(PokemonManipulator manipulator, PokemonManipulator samesies) {
        this.doubleTake(manipulator, samesies, samesies);
    }

    // Defining case goes in manipulator (to test with and without this condition)
    // Manipulator inside TestInfo will be called AFTER defining manipulator
    // withoutManipulator and withManipulator should mostly be checking conditions afterwards to make sure it happened as expected
    public void doubleTake(PokemonManipulator manipulator, PokemonManipulator withoutManipulator, PokemonManipulator withManipulator) {
        handle(PokemonManipulator.empty(), withoutManipulator);
        handle(manipulator, withManipulator);
    }

    // Handles all the manipulators in the proper order
    private void handle(PokemonManipulator manipulator, PokemonManipulator afterCheck) {
        TestBattle battle = this.createBattle();
        manipulator.manipulate(battle);
        this.manipulate(battle);
        afterCheck.manipulate(battle);
    }

    public void checkCritStage(int expectedStage) {
        TestBattle battle = this.createBattle();
        TestPokemon attacking = battle.getAttacking();

        int beforeStage = battle.getCritStage(attacking);
        Assert.assertEquals(1, beforeStage);

        this.manipulate(battle);

        int afterStage = battle.getCritStage(attacking);
        Assert.assertEquals(expectedStage, afterStage);
    }

    // By default, check the stat on the more relevant Pokemon
    // Attack, Sp. Attack, Accuracy, Speed uses attacking
    // Defense, Sp. Defense, Evasion uses defending
    public void statModifierTest(double expectedChange, Stat stat) {
        this.statModifierTest(expectedChange, stat, stat.user());
    }

    // user should be true if checking the stat on the attacking pokemon and false for the defending pokemon
    public void statModifierTest(double expectedChange, Stat stat, boolean user) {
        TestBattle battle = this.createBattle();
        TestPokemon statPokemon = user ? battle.getAttacking() : battle.getDefending();

        int beforeStat = Stat.getStat(stat, statPokemon, battle);
        this.manipulate(battle);
        int afterStat = Stat.getStat(stat, statPokemon, battle);

        Assert.assertEquals(
                StringUtils.spaceSeparated(beforeStat, afterStat, expectedChange, this),
                (int)(beforeStat*expectedChange),
                afterStat
        );
    }

    // No modifier without manipulation, expectedModifier with it
    public void powerChangeTest(double expectedModifier, AttackNamesies attackNamesies) {
        TestBattle battle = this.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Check modifiers manually
        double beforeModifier = battle.getDamageModifier(attacking, defending);
        TestUtils.assertEquals(1, beforeModifier);

        this.manipulate(battle);
        attacking.setupMove(attackNamesies, battle);
        double afterModifier = battle.getDamageModifier(attacking, defending);

        TestUtils.assertEquals(
                StringUtils.spaceSeparated(attackNamesies, this),
                expectedModifier,
                afterModifier
        );

        // Make sure modifiers actually happen in battle
        powerChangeTest(expectedModifier, false, attackNamesies);
        powerChangeTest(expectedModifier, true, attackNamesies);
    }

    private void powerChangeTest(double expectedModifier, boolean manipulate, AttackNamesies attackNamesies) {
        TestBattle battle = this.createBattle();
        if (manipulate) {
            this.manipulate(battle);
        }

        battle.getAttacking().setExpectedDamageModifier(manipulate ? expectedModifier : 1);
        battle.attackingFight(attackNamesies);
    }

    // Differs from the powerChangeTest in that it only checks once
    // Immediately applies manipulations in the testInfo and confirms the power modifier
    public void powerModifierTest(double expectedModifier, AttackNamesies attackNamesies) {
        TestBattle battle = this.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        this.manipulate(battle);

        // Manual check
        attacking.setupMove(attackNamesies, battle);
        TestUtils.assertEquals(expectedModifier, battle.getDamageModifier(attacking, defending));

        // Battle check
        attacking.setExpectedDamageModifier(expectedModifier);
        battle.attackingFight(attackNamesies);
    }

    public void stageChangeTest(int expectedStage, Stat stat) {
        TestBattle battle = this.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        TestPokemon statPokemon = stat.user() ? attacking : defending;
        TestPokemon otherPokemon = stat.user() ? defending : attacking;

        int beforeStage = stat.getStage(statPokemon, otherPokemon, battle);
        Assert.assertEquals(0, beforeStage);

        this.manipulate(battle);
        int afterStage = stat.getStage(statPokemon, otherPokemon, battle);

        Assert.assertEquals(
                StringUtils.spaceSeparated(afterStage, expectedStage, stat, this),
                expectedStage,
                afterStage
        );
    }

    @Override
    public String toString() {
        return new StringAppender()
                .append(attackingName.getName())
                .append(" ")
                .append(defendingName.getName())
                .append(" ")
                .appendJoin(" ", this.toString)
                .toString();
    }
}
