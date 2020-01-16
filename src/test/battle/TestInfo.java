package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.EffectInterfaces.PokemonHolder;
import battle.effect.EffectNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import pokemon.stat.User;
import test.general.TestUtils;
import test.pokemon.TestPokemon;
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
        battle.getAttacking().assertAbility(AbilityNamesies.NO_ABILITY);
        battle.getDefending().assertAbility(AbilityNamesies.NO_ABILITY);
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
        Assert.assertEquals(0, beforeStage);

        this.manipulate(battle);

        int afterStage = battle.getCritStage(attacking);
        Assert.assertEquals(expectedStage, afterStage);
    }

    // By default, check the stat on the more relevant Pokemon
    // Attack, Sp. Attack, Accuracy uses attacking
    // Defense, Sp. Defense, Evasion uses defending
    // For ambiguous stats (like Speed), must use the more explicit method
    public void statModifierTest(double expectedChange, Stat stat) {
        this.statModifierTest(expectedChange, stat, stat.user());
    }

    public void statModifierTest(double expectedChange, Stat stat, User user) {
        // HP not a relevant stat to modify
        // user should be explicitly stated for ambiguous stats (like Speed)
        Assert.assertNotEquals(Stat.HP, stat);
        Assert.assertNotEquals(User.BOTH, user);

        TestBattle battle = this.createBattle();
        TestPokemon statPokemon = user.isAttacking() ? battle.getAttacking() : battle.getDefending();

        int beforeStat = Stat.getStat(stat, statPokemon, battle);
        this.manipulate(battle);
        int afterStat = Stat.getStat(stat, statPokemon, battle);

        // If the Pokemon is now transformed, need to adjust stats
        // Note: Will likely need to update in future to also include stance change abilities and such
        int delta = 0;
        if (statPokemon.hasEffect(PokemonEffectNamesies.TRANSFORMED)) {
            // Calculate what the stat should be with different base stats (but no effects)
            PokemonHolder transformed = (PokemonHolder)statPokemon.getEffect(PokemonEffectNamesies.TRANSFORMED);
            int basePokemonStat = statPokemon.stats().calculate(stat, statPokemon.getPokemonInfo().getStats());
            int transformPokemonStat = statPokemon.stats().calculate(stat, transformed.getPokemon().getInfo().getStats());

            expectedChange *= (double)transformPokemonStat/basePokemonStat;
            delta = 1;
        }

        TestUtils.assertAlmostEquals(
                StringUtils.spaceSeparated(beforeStat, afterStat, expectedChange, this),
                (int)(beforeStat*expectedChange),
                afterStat,
                delta
        );
    }

    // No modifier without manipulation, expectedModifier with it
    public void powerChangeTest(double expectedModifier, AttackNamesies attackNamesies) {
        powerChangeTest(1, expectedModifier, attackNamesies);
    }

    public void powerChangeTest(double withoutModifier, double expectedModifier, AttackNamesies attackNamesies) {
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
        powerChangeTest(withoutModifier, PokemonManipulator.empty(), attackNamesies);
        powerChangeTest(expectedModifier, this.manipulator, attackNamesies);
    }

    private void powerChangeTest(double expectedModifier, PokemonManipulator manipulator, AttackNamesies attackNamesies) {
        TestBattle battle = this.createBattle();
        manipulator.manipulate(battle);

        battle.getAttacking().setExpectedDamageModifier(expectedModifier);
        battle.attackingFight(attackNamesies);
    }

    // Checks each stage for the relevant Pokemon after the manipulator to match the expected stages
    // Note: The stages here will not all be for the same Pokemon
    // Attack, Sp. Attack, Accuracy, and Speed will look at the attacking Pokemon stages
    // Defense, Sp. Defense, and Evasion will look at the defending Pokemon stages
    public void stageChangeTest(TestStages expectedStages) {
        TestBattle battle = this.createBattle();
        this.assertExpectedStages(battle, new TestStages());

        this.manipulate(battle);
        this.assertExpectedStages(battle, expectedStages);
    }

    // Make sure every stat has the expected stage
    // The stages here will not all be for the same Pokemon
    // Attack, Sp. Attack, Accuracy, and Speed will look at the attacking Pokemon stages
    // Defense, Sp. Defense, and Evasion will look at the defending Pokemon stages
    private void assertExpectedStages(TestBattle battle, TestStages expectedStages) {
        for (Stat stat : Stat.BATTLE_STATS) {
            TestPokemon statPokemon = stat.isAttacking() ? battle.getAttacking() : battle.getDefending();
            TestPokemon otherPokemon = battle.getOtherPokemon(statPokemon);

            int expectedStage = expectedStages.get(stat);
            int afterStage = stat.getStage(statPokemon, otherPokemon, battle);
            Assert.assertEquals(
                    StringUtils.spaceSeparated(afterStage, expectedStage, stat, this),
                    expectedStage,
                    afterStage
            );
        }
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
