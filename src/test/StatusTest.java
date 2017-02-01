package test;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import pokemon.Stat;

import java.util.EnumMap;
import java.util.Map;

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
public class StatusTest {
    @Test
    public void testGiveStatus() {
        ActivePokemon uglyFace = new TestPokemon(PokemonNamesies.RATICATE);
    }

    @Test
    public void testStatChanges() {
        testStatChange(StatusCondition.PARALYZED, Stat.SPEED, .25);
        testStatChange(StatusCondition.BURNED, Stat.ATTACK, .5);

        // TODO: Test Guts
    }

    private void testStatChange(StatusCondition statusCondition, Stat stat, double ratio) {
        TestPokemon mahBoi = new TestPokemon(PokemonNamesies.RAPIDASH);
        TestPokemon uglyFace = new TestPokemon(PokemonNamesies.WATCHOG);

        Battle b = TestBattle.create(mahBoi, uglyFace);

        int original = Stat.getStat(stat, uglyFace, mahBoi, b);

        Status.giveStatus(b, uglyFace, uglyFace, statusCondition);
        int afterStatus = Stat.getStat(stat, uglyFace, mahBoi, b);
        Assert.assertTrue((int)(original*ratio) == afterStatus);

        uglyFace.removeStatus();
        int afterRemoved = Stat.getStat(stat, uglyFace, mahBoi, b);
        Assert.assertTrue(original == afterRemoved);
    }

    @Test
    public void triAttackTest() {
        boolean alwaysSame = true;
        Map<StatusCondition, Boolean> statusMap = new EnumMap<>(StatusCondition.class);
        statusMap.put(StatusCondition.NO_STATUS, false);
        statusMap.put(StatusCondition.PARALYZED, false);
        statusMap.put(StatusCondition.BURNED, false);
        statusMap.put(StatusCondition.FROZEN, false);

        for (int i = 0; i < 1000; i++) {
            TestPokemon attacking = new TestPokemon(PokemonNamesies.SHUCKLE);
            TestPokemon defending = new TestPokemon(PokemonNamesies.SHUCKLE);

            TestBattle b = TestBattle.create(attacking, defending);

            b.fight(AttackNamesies.TRI_ATTACK, AttackNamesies.TRI_ATTACK);

            StatusCondition attackingCondition = attacking.getStatus().getType();
            StatusCondition defendingCondition = defending.getStatus().getType();

            Assert.assertTrue(statusMap.containsKey(attackingCondition));
            Assert.assertTrue(statusMap.containsKey(defendingCondition));

            statusMap.put(attackingCondition, true);
            statusMap.put(defendingCondition, true);

            if (attackingCondition != defendingCondition) {
                alwaysSame = false;
            }
        }

        // TODO: Uncomment this once the begin and end applies shit is done
//        Assert.assertFalse(alwaysSame);
        Assert.assertTrue(statusMap.get(StatusCondition.NO_STATUS));
//        Assert.assertTrue(statusMap.get(StatusCondition.PARALYZED));
//        Assert.assertTrue(statusMap.get(StatusCondition.BURNED));
//        Assert.assertTrue(statusMap.get(StatusCondition.FROZEN));
    }
}
