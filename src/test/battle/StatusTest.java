package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.status.Status;
import battle.effect.status.StatusNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import test.BaseTest;
import test.TestPokemon;
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
    public void testGiveStatus() {
        // TODO: this
    }

    @Test
    public void testStatChanges() {
        testStatChange(StatusNamesies.PARALYZED, Stat.SPEED, .25);
        testStatChange(StatusNamesies.BURNED, Stat.ATTACK, .5);

        // TODO: Test Guts
    }

    private void testStatChange(StatusNamesies statusCondition, Stat stat, double ratio) {
        TestBattle battle = TestBattle.create(PokemonNamesies.RAPIDASH, PokemonNamesies.WATCHOG);
        TestPokemon mahBoi = battle.getAttacking();
        TestPokemon uglyFace = battle.getDefending();

        int original = Stat.getStat(stat, uglyFace, mahBoi, battle);

        Status.applyStatus(battle, uglyFace, uglyFace, statusCondition);
        int afterStatus = Stat.getStat(stat, uglyFace, mahBoi, battle);
        Assert.assertTrue((int)(original*ratio) == afterStatus);

        uglyFace.removeStatus();
        int afterRemoved = Stat.getStat(stat, uglyFace, mahBoi, battle);
        Assert.assertTrue(original == afterRemoved);
    }

    @Test
    public void preventionTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        Assert.assertFalse(defending.hasStatus());
        Assert.assertFalse(defending.hasStatus(StatusNamesies.POISONED));
        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertTrue(defending.hasStatus(StatusNamesies.POISONED));
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));

        battle.defendingFight(AttackNamesies.PURIFY);
        Assert.assertFalse(defending.hasStatus());

        // Cannot poison a pokemon with immunity
        defending.withAbility(AbilityNamesies.IMMUNITY);
        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertFalse(defending.hasStatus());

        // Unless you have Mold Breaker
        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        attacking.apply(true, AttackNamesies.TOXIC, battle);
        Assert.assertTrue(defending.hasStatus(StatusNamesies.POISONED));

        battle.defendingFight(AttackNamesies.REFRESH);
        Assert.assertFalse(defending.hasStatus());

        // When executing a full turn, Toxic should be successful and poison the target
        // However, the end of the turn should restore its condition
        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertFalse(defending.hasStatus());

        // Suppressed ability -- should poison regardless of mold breaker
        battle.attackingFight(AttackNamesies.GASTRO_ACID);
        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertTrue(defending.hasStatus(StatusNamesies.POISONED));

        battle.emptyHeal();
        Assert.assertFalse(defending.hasStatus());

        // Poison-type Pokemon cannot be poisoned
        battle.defendingFight(AttackNamesies.REFLECT_TYPE);
        Assert.assertTrue(defending.isType(battle, Type.POISON));
        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertFalse(defending.hasStatus());

        // Unless you have Corrosion
        attacking.withAbility(AbilityNamesies.CORROSION);
        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertTrue(defending.hasStatus(StatusNamesies.POISONED));

        // Type should not heal at end of turn
        battle.attackingFight(AttackNamesies.SPLASH);
        Assert.assertTrue(defending.hasStatus(StatusNamesies.POISONED));

        battle.emptyHeal();
        Assert.assertFalse(defending.hasStatus());

        // Remove Poison-typing
        battle.attackingFight(AttackNamesies.SOAK);
        Assert.assertFalse(defending.isType(battle, Type.POISON));
        Assert.assertFalse(defending.hasStatus());

        // Safeguard has nothing to do with ability
        battle.defendingFight(AttackNamesies.SAFEGUARD);
        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertFalse(defending.hasStatus());

        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertFalse(defending.hasStatus());
    }

    @Test
    public void badlyPoisonedTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        battle.attackingFight(AttackNamesies.TOXIC);

        // After 1 turn -- 15/16 health ratio
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));
        attacking.assertFullHealth();
        defending.assertHealthRatio(15/16f);

        // After 2 turns -- 13/16 health ratio
        int prevHp = defending.getHP();
        battle.splashFight();
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));
        attacking.assertFullHealth();
        defending.assertHealthRatioDiff(prevHp, 2/16f);
        defending.assertHealthRatio(13/16f, 1);

        // After 3 turns -- 10/16 health ratio
        prevHp = defending.getHP();
        battle.splashFight();
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));
        attacking.assertFullHealth();
        defending.assertHealthRatioDiff(prevHp, 3/16f);
        defending.assertHealthRatio(10/16f, 2);

        // After 4 turns -- 6/16 health ratio
        prevHp = defending.getHP();
        battle.splashFight();
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));
        attacking.assertFullHealth();
        defending.assertHealthRatioDiff(prevHp, 4/16f);
        defending.assertHealthRatio(6/16f, 3);

        // After 5 turns -- 1/16 health ratio
        prevHp = defending.getHP();
        battle.splashFight();
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));
        attacking.assertFullHealth();
        defending.assertHealthRatioDiff(prevHp, 5/16f);
        defending.assertHealthRatio(1/16f, 4);

        // After 6 turns -- DEAD
        battle.splashFight();
        Assert.assertTrue(defending.isActuallyDead());
        attacking.assertFullHealth();
        defending.assertHealthRatio(0);
    }
}
