package test.battle;

import battle.ActivePokemon;
import battle.Battle;
import battle.DamageCalculator;
import org.junit.Assert;
import test.general.TestUtils;
import test.pokemon.TestPokemon;
import util.string.StringUtils;

public class TestDamageCalculator extends DamageCalculator {
    @Override
    public double getDamageModifier(Battle b, ActivePokemon attacking, ActivePokemon defending) {
        double modifier = super.getDamageModifier(b, attacking, defending);

        Assert.assertTrue(attacking.getAttack().getName(), modifier > 0);
        Double expectedDamageModifier = ((TestPokemon)attacking).getExpectedDamageModifier();
        if (expectedDamageModifier != null) {
            TestUtils.assertEquals(
                    StringUtils.spaceSeparated(attacking.getAttack(), attacking.getCount()),
                    expectedDamageModifier, modifier
            );
        }

        return modifier;
    }

    @Override
    protected boolean checkRandomCrit(Battle b, ActivePokemon me) {
        // Tests can never critical hit by chance (can still crit with AlwaysCritEffects and such still though)
        return false;
    }
}
