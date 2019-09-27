package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import test.BaseTest;
import test.TestPokemon;
import trainer.EnemyTrainer;
import type.Type;

/*
TODO:
    Give each status to a raticate
    fail for two statuses -- maybe have a loop that tests all combos
    fail for fainted on a nonzero health poke
    rapidash can't be burned
    change dragonair's in-battle type to fire and then try to burn it -- should fail
    bulbasaur can't be poisoned
    jolteon can't be paralyzed
    
    speed is decreased from paralysis
    attack is halved from burn
    
    damage works correctly -- assert hp change is in range or deadsies
    deadsies -- set hp to 1, then murder -- make sure its's dead
 */
public class StatusTest extends BaseTest {
    @Test
    public void statChangeTest() {
        // Paralysis reduces speed by 75%
        statChangeTest(.25, AttackNamesies.THUNDER_WAVE, Stat.SPEED, new TestInfo());

        // Unless the victim has Quick Feet -- increases by 50%
        statChangeTest(1.5, AttackNamesies.THUNDER_WAVE, Stat.SPEED, new TestInfo().defending(AbilityNamesies.QUICK_FEET));

        // Using Facade should not effect anything but power
        statChangeTest(.25, AttackNamesies.THUNDER_WAVE, Stat.SPEED, new TestInfo().with((battle, attacking, defending) -> defending.setupMove(AttackNamesies.FACADE, battle)));

        // Burn reduces attack by 50%
        statChangeTest(.5, AttackNamesies.WILL_O_WISP, Stat.ATTACK, new TestInfo());

        // Unless the victim has Guts -- increases by 50%
        statChangeTest(1.5, AttackNamesies.WILL_O_WISP, Stat.ATTACK, new TestInfo().defending(AbilityNamesies.GUTS));

        // Or the victim is using the move Facade -- power is increased
        statChangeTest(1, AttackNamesies.WILL_O_WISP, Stat.ATTACK, new TestInfo().with((battle, attacking, defending) -> defending.setupMove(AttackNamesies.FACADE, battle)));

        // Quick Feet has nothing to do with attack
        statChangeTest(.5, AttackNamesies.WILL_O_WISP, Stat.ATTACK, new TestInfo().defending(AbilityNamesies.QUICK_FEET));
    }

    private void statChangeTest(double ratio, AttackNamesies statusAttack, Stat stat, TestInfo testInfo) {
        // Uglyface can receive all status conditions
        testInfo.defending(PokemonNamesies.WATCHOG);

        TestBattle battle = testInfo.createBattle();
        TestPokemon uglyFace = battle.getDefending();

        testInfo.manipulate(battle);
        int original = Stat.getStat(stat, uglyFace, battle);

        battle.attackingFight(statusAttack);
        Assert.assertTrue(uglyFace.hasStatus());
        testInfo.manipulate(battle);
        int afterStatus = Stat.getStat(stat, uglyFace, battle);
        Assert.assertEquals((int)(original*ratio), afterStatus);

        uglyFace.removeStatus();
        int afterRemoved = Stat.getStat(stat, uglyFace, battle);
        Assert.assertEquals(original, afterRemoved);
    }

    @Test
    public void preventionTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        defending.assertNoStatus();
        battle.attackingFight(AttackNamesies.TOXIC);
        defending.assertBadPoison();

        battle.defendingFight(AttackNamesies.PURIFY);
        defending.assertNoStatus();

        // Cannot poison a pokemon with immunity
        // TODO: Make sure this actually fails during the attack and not the end turn heal thingy
        defending.withAbility(AbilityNamesies.IMMUNITY);
        battle.attackingFight(AttackNamesies.TOXIC);
        defending.assertNoStatus();

        // Unless you have Mold Breaker
        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        attacking.apply(true, AttackNamesies.TOXIC, battle);
        defending.assertBadPoison();

        battle.defendingFight(AttackNamesies.REFRESH);
        defending.assertNoStatus();

        // When executing a full turn, Toxic should be successful and poison the target
        // However, the end of the turn should restore its condition
        battle.attackingFight(AttackNamesies.TOXIC);
        defending.assertNoStatus();

        // Suppressed ability -- should poison regardless of mold breaker
        battle.attackingFight(AttackNamesies.GASTRO_ACID);
        battle.attackingFight(AttackNamesies.TOXIC);
        defending.assertBadPoison();

        battle.emptyHeal();
        defending.assertNoStatus();

        // Poison-type Pokemon cannot be poisoned
        battle.defendingFight(AttackNamesies.REFLECT_TYPE);
        Assert.assertTrue(defending.isType(battle, Type.POISON));
        battle.attackingFight(AttackNamesies.TOXIC);
        defending.assertNoStatus();

        // Unless you have Corrosion
        attacking.withAbility(AbilityNamesies.CORROSION);
        battle.attackingFight(AttackNamesies.TOXIC);
        defending.assertBadPoison();

        // Type should not heal at end of turn
        battle.attackingFight(AttackNamesies.SPLASH);
        defending.assertBadPoison();

        battle.emptyHeal();
        defending.assertNoStatus();

        // Remove Poison-typing
        battle.attackingFight(AttackNamesies.SOAK);
        Assert.assertFalse(defending.isType(battle, Type.POISON));
        defending.assertNoStatus();

        // Safeguard has nothing to do with ability
        battle.defendingFight(AttackNamesies.SAFEGUARD);
        battle.attackingFight(AttackNamesies.TOXIC);
        defending.assertNoStatus();
        Assert.assertTrue(battle.getTrainer(defending).hasEffect(TeamEffectNamesies.SAFEGUARD));

        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        battle.attackingFight(AttackNamesies.TOXIC);
        defending.assertNoStatus();
        Assert.assertTrue(battle.getTrainer(defending).hasEffect(TeamEffectNamesies.SAFEGUARD));

        // Unless that ability is Infiltrator
        attacking.withAbility(AbilityNamesies.INFILTRATOR);
        battle.attackingFight(AttackNamesies.TOXIC);
        defending.assertBadPoison();
        Assert.assertTrue(battle.getTrainer(defending).hasEffect(TeamEffectNamesies.SAFEGUARD));
    }

    @Test
    public void badlyPoisonedTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        battle.attackingFight(AttackNamesies.TOXIC);

        // After 1 turn -- 15/16 health ratio
        defending.assertBadPoison();
        attacking.assertFullHealth();
        defending.assertHealthRatio(15/16f);

        // After 2 turns -- 13/16 health ratio
        int prevHp = defending.getHP();
        battle.splashFight();
        defending.assertBadPoison();
        attacking.assertFullHealth();
        defending.assertHealthRatioDiff(prevHp, 2/16f);
        defending.assertHealthRatio(13/16f, 1);

        // After 3 turns -- 10/16 health ratio
        prevHp = defending.getHP();
        battle.splashFight();
        defending.assertBadPoison();
        attacking.assertFullHealth();
        defending.assertHealthRatioDiff(prevHp, 3/16f);
        defending.assertHealthRatio(10/16f, 2);

        // After 4 turns -- 6/16 health ratio
        prevHp = defending.getHP();
        battle.splashFight();
        defending.assertBadPoison();
        attacking.assertFullHealth();
        defending.assertHealthRatioDiff(prevHp, 4/16f);
        defending.assertHealthRatio(6/16f, 3);

        // After 5 turns -- 1/16 health ratio
        prevHp = defending.getHP();
        battle.splashFight();
        defending.assertBadPoison();
        attacking.assertFullHealth();
        defending.assertHealthRatioDiff(prevHp, 5/16f);
        defending.assertHealthRatio(1/16f, 4);

        // After 6 turns -- DEAD
        battle.splashFight();
        Assert.assertTrue(defending.isActuallyDead());
        attacking.assertFullHealth();
        defending.assertHealthRatio(0);
    }

    @Test
    public void healStatusTest() {
        TestBattle battle = TestBattle.createTrainerBattle(PokemonNamesies.EEVEE, PokemonNamesies.VAPOREON);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending1 = battle.getDefending();
        TestPokemon defending2 = TestPokemon.newTrainerPokemon(PokemonNamesies.ESPEON);
        ((EnemyTrainer)battle.getOpponent()).addPokemon(defending2);
        Assert.assertTrue(battle.getDefending() == defending1);

        // Basic Burn Heal, make sure Antidote fails for burns
        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        attacking.assertHasStatus(StatusNamesies.BURNED);
        PokemonManipulator.useItem(ItemNamesies.ANTIDOTE, true, false).manipulate(battle);
        attacking.assertHasStatus(StatusNamesies.BURNED);
        PokemonManipulator.useItem(ItemNamesies.BURN_HEAL).manipulate(battle);
        attacking.assertNoStatus();

        // Make sure Antidote works for both bad poison and regular poison
        battle.defendingFight(AttackNamesies.TOXIC);
        attacking.assertBadPoison();

        // Make sure burn heal doesn't heal poison
        PokemonManipulator.useItem(ItemNamesies.BURN_HEAL, true, false).manipulate(battle);
        attacking.assertBadPoison();

        PokemonManipulator.useItem(ItemNamesies.ANTIDOTE).manipulate(battle);
        attacking.assertNoStatus();

        battle.defendingFight(AttackNamesies.POISON_POWDER);
        attacking.assertRegularPoison();

        PokemonManipulator.useItem(ItemNamesies.ANTIDOTE).manipulate(battle);
        attacking.assertNoStatus();

        // Full Heal can't heal a status you don't have
        PokemonManipulator.useItem(ItemNamesies.FULL_HEAL, true, false).manipulate(battle);
        attacking.assertNoStatus();

        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending1.isActuallyDead());
        Assert.assertTrue(battle.getDefending() == defending2);

        // Full Heal doesn't work on fainted Pokemon
        PokemonManipulator.useItem(ItemNamesies.FULL_HEAL, false, false).manipulate(battle, defending1, attacking);
        Assert.assertTrue(defending1.isActuallyDead());
        Assert.assertTrue(battle.getDefending() == defending2);

        // But revive does!
        PokemonManipulator.useItem(ItemNamesies.REVIVE, false, true).manipulate(battle, defending1, attacking);
        Assert.assertFalse(defending1.isActuallyDead());
        defending1.assertNoStatus();
        defending1.assertHealthRatio(.5, 1);
        Assert.assertTrue(battle.getDefending() == defending2);
    }
}
