package test;

import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import util.StringUtils;

public class StatChangeTest {
    @Test
    public void test() {
        statModifierTest(1.5, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.HUSTLE));
        statModifierTest(.8, Stat.ACCURACY, new TestInfo().attacking(AbilityNamesies.HUSTLE));
        statModifierTest(1, Stat.SP_ATTACK, new TestInfo().attacking(AbilityNamesies.HUSTLE));

        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(EffectNamesies.LIGHT_SCREEN));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(EffectNamesies.LIGHT_SCREEN));

        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.LILEEP).defending(EffectNamesies.SANDSTORM));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.MAWILE).defending(EffectNamesies.SANDSTORM));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.SANDYGAST).defending(EffectNamesies.SANDSTORM));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.GEODUDE).defending(EffectNamesies.SANDSTORM));

        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.LANTURN).defending(ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.CHINCHOU).defending(ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.CLAMPERL).defending(ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.CLAMPERL).defending(ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.HUNTAIL).defending(ItemNamesies.DEEP_SEA_SCALE));

        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.DRAGONAIR).defending(ItemNamesies.EVIOLITE));
        statModifierTest(1.5, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.CHANSEY).defending(ItemNamesies.EVIOLITE));
        statModifierTest(1, Stat.SPEED, new TestInfo().defending(PokemonNamesies.CHANSEY).defending(ItemNamesies.EVIOLITE));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.HUNTAIL).defending(ItemNamesies.EVIOLITE));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.RAICHU).defending(ItemNamesies.EVIOLITE));

        statModifierTest(2, Stat.SPEED, new TestInfo().attacking(AbilityNamesies.CHLOROPHYLL).attacking(EffectNamesies.SUNNY));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().attacking(AbilityNamesies.CHLOROPHYLL).attacking(EffectNamesies.SUNNY));

        statModifierTest(1.5, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(EffectNamesies.SUNNY));
        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(AbilityNamesies.FLOWER_GIFT).attacking(EffectNamesies.SUNNY));
        statModifierTest(1, 1.5, Stat.SP_DEFENSE, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(EffectNamesies.SUNNY));
        statModifierTest(1, Stat.SPEED, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(EffectNamesies.SUNNY));

        statModifierTest(2, Stat.DEFENSE, new TestInfo().with(AttackNamesies.TACKLE).defending(AbilityNamesies.FUR_COAT));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().with(AttackNamesies.SURF).defending(AbilityNamesies.FUR_COAT));
    }

    private void statModifierTest(double expectedChange, Stat stat, TestInfo testInfo) {
        statModifierTest(expectedChange, 1, stat, testInfo);
    }

    private void statModifierTest(double expectedChange, double otherExpectedChange, Stat stat, TestInfo testInfo) {
        TestPokemon attacking = new TestPokemon(testInfo.attackingName);
        TestPokemon defending = new TestPokemon(testInfo.defendingName);

        TestPokemon statPokemon = stat.user() ? attacking : defending;
        TestPokemon otherPokemon = stat.user() ? defending : attacking;

        TestBattle battle = TestBattle.create(attacking, defending);
        attacking.setupMove(testInfo.attackName, battle, defending);

        int beforeStat = Stat.getStat(stat, statPokemon, otherPokemon, battle);
        int otherBeforeStat = Stat.getStat(stat, otherPokemon, statPokemon, battle);

        testInfo.manipulator.manipulate(battle, attacking, defending);

        int afterStat = Stat.getStat(stat, statPokemon, otherPokemon, battle);
        int otherAfterStat = Stat.getStat(stat, otherPokemon, statPokemon, battle);

        Assert.assertTrue(
                StringUtils.spaceSeparated(beforeStat, afterStat, expectedChange, testInfo.toString()),
                (int)(beforeStat*expectedChange) == afterStat
        );

        Assert.assertTrue((int)(otherBeforeStat*otherExpectedChange) == otherAfterStat);
    }
}
