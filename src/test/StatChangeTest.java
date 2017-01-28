package test;

import org.junit.Assert;
import org.junit.Test;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import util.StringUtils;

public class StatChangeTest {

    @Test
    public void test() {
        statModifierTest(1.5, Stat.ATTACK, AbilityNamesies.HUSTLE);
        statModifierTest(.8, Stat.ACCURACY, AbilityNamesies.HUSTLE);
        statModifierTest(1, Stat.SP_ATTACK, AbilityNamesies.HUSTLE);
    }

    private void statModifierTest(double expectedChange, Stat stat, AbilityNamesies abilityNamesies) {
        statModifierTest(expectedChange, stat, new TestInfo().attacking(abilityNamesies));
    }

    private void statModifierTest(
            double expectedChange,
            Stat stat,
            TestInfo testInfo) {
        TestPokemon attacking = new TestPokemon(testInfo.attackingName);
        TestPokemon defending = new TestPokemon(testInfo.defendingName);

        TestBattle battle = TestBattle.create(attacking, defending);
        attacking.setupMove(testInfo.attackName, battle, defending);

        int beforeStat = Stat.getStat(stat, attacking, defending, battle);
        testInfo.manipulator.manipulate(battle, attacking, defending);
        int afterStat = Stat.getStat(stat, attacking, defending, battle);

        Assert.assertTrue(
                StringUtils.spaceSeparated(beforeStat, afterStat, expectedChange, testInfo.toString()),
                (int)(beforeStat*expectedChange) == afterStat
        );
    }
}
