package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.source.CastSource;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import test.general.BaseTest;
import test.pokemon.TestPokemon;

// Sort of for tests related to general battle stuff and you can't figure out where to put it
// Can also be used for testing test mechanics
public class MechanicsTest extends BaseTest {
    @Test
    public void testInfoCopyTest() {
        // If these default values change then this test might need an adjustment
        assertTestInfoSpecies(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER, new TestInfo());

        // Changing information other than species will not affect the copy
        testInfoCopyTest(false, new TestInfo());
        testInfoCopyTest(false, new TestInfo().attacking(AbilityNamesies.OVERGROW));
        testInfoCopyTest(false, new TestInfo().defending(AbilityNamesies.BLAZE));

        // Even if the default Pokemon are explicitly set, it will override the setting in the copy
        testInfoCopyTest(true, new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER));
        testInfoCopyTest(true, new TestInfo().attacking(PokemonNamesies.BULBASAUR));
        testInfoCopyTest(true, new TestInfo().defending(PokemonNamesies.CHARMANDER));
    }

    private void testInfoCopyTest(boolean changed, TestInfo testInfo) {
        // Find out what Pokemon species are in the original TestInfo
        // Confirm that they are different that the ones used in the test to check differences (otherwise test makes no sense)
        TestBattle battle = testInfo.createBattle();
        PokemonNamesies attacking = battle.getAttacking().namesies();
        PokemonNamesies defending = battle.getDefending().namesies();
        Assert.assertNotEquals(PokemonNamesies.SQUIRTLE, attacking);
        Assert.assertNotEquals(PokemonNamesies.PIKACHU, defending);
        assertTestInfoSpecies(attacking, defending, testInfo);

        // Create a copy of testInfo, replacing Pokes with Squirtle and Pikachu ONLY IF NOT CHANGED
        TestInfo copy = testInfo.copy(PokemonNamesies.SQUIRTLE, PokemonNamesies.PIKACHU);
        if (changed) {
            assertTestInfoSpecies(attacking, defending, copy);
        } else {
            assertTestInfoSpecies(PokemonNamesies.SQUIRTLE, PokemonNamesies.PIKACHU, copy);
        }
    }

    private void assertTestInfoSpecies(PokemonNamesies attacking, PokemonNamesies defending, TestInfo testInfo) {
        TestBattle battle = testInfo.createBattle();
        battle.getAttacking().assertSpecies(attacking);
        battle.getDefending().assertSpecies(defending);
    }

    @Test
    public void sourceTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        attacking.withAbility(AbilityNamesies.OVERGROW);
        attacking.giveItem(ItemNamesies.ORAN_BERRY);
        attacking.setupMove(AttackNamesies.SWITCHEROO, battle);
        attacking.setCastSource(ItemNamesies.NO_ITEM.getItem());

        for (CastSource source : CastSource.values()) {
            String sourceName = source.getSourceName(attacking);
            if (source.hasSourceName()) {
                // Important since these values are hard-coded in another method
                Assert.assertTrue(source == CastSource.ABILITY || source == CastSource.HELD_ITEM);
                Assert.assertNotNull(sourceName);
            } else {
                Assert.assertNull(sourceName);
            }
        }

        Assert.assertEquals(AbilityNamesies.OVERGROW.getName(), CastSource.ABILITY.getSourceName(attacking));
        Assert.assertEquals(ItemNamesies.ORAN_BERRY.getName(), CastSource.HELD_ITEM.getSourceName(attacking));

        Assert.assertSame(attacking.getAbility(), CastSource.ABILITY.getSource(attacking));
        Assert.assertSame(attacking.getHeldItem(), CastSource.HELD_ITEM.getSource(attacking));
        Assert.assertSame(attacking.getAttack(), CastSource.ATTACK.getSource(attacking));
        Assert.assertSame(attacking.getCastSource(), CastSource.CAST_SOURCE.getSource(attacking));
    }

    @Test
    public void otherTrainerPokemonTest() {
        otherTrainerPokemonTest(
                PokemonNamesies.SQUIRTLE, null,
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER).addAttacking(PokemonNamesies.SQUIRTLE)
        );

        otherTrainerPokemonTest(
                null, PokemonNamesies.SQUIRTLE,
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .asTrainerBattle()
                        .addDefending(PokemonNamesies.SQUIRTLE)
        );

        otherTrainerPokemonTest(
                PokemonNamesies.SQUIRTLE, PokemonNamesies.PIKACHU,
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .asTrainerBattle()
                        .addAttacking(PokemonNamesies.SQUIRTLE)
                        .addDefending(PokemonNamesies.PIKACHU)
        );

        // It's totally valid to add multiple Pokemon, however then this method should fail because no concrete 'other'
        // In general, two is ideal because then easy to control which Pokemon will be randomly swapped to
        otherTrainerPokemonTest(
                PokemonNamesies.SQUIRTLE, null,
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .asTrainerBattle()
                        .addAttacking(PokemonNamesies.SQUIRTLE)
                        .addDefending(PokemonNamesies.PIKACHU)
                        .addDefending(PokemonNamesies.EEVEE)
        );

        otherTrainerPokemonTest(
                null, PokemonNamesies.EEVEE,
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .asTrainerBattle()
                        .addAttacking(PokemonNamesies.SQUIRTLE)
                        .addAttacking(PokemonNamesies.PIKACHU)
                        .addDefending(PokemonNamesies.EEVEE)
        );

        otherTrainerPokemonTest(
                null, null,
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .addAttacking(PokemonNamesies.SQUIRTLE)
                        .addAttacking(PokemonNamesies.PIKACHU)
        );
    }

    // Expected can be null to predict failure
    private void otherTrainerPokemonTest(PokemonNamesies expectedAttacking, PokemonNamesies expectedDefending, TestInfo testInfo) {
        TestBattle battle = testInfo.createBattle();
        testInfo.manipulate(battle);

        // If success, then assert species
        // If failure, then expected should be null (to indicate expected to fail)
        try {
            TestPokemon otherAttacking = battle.getOtherAttacking();
            otherAttacking.assertSpecies(expectedAttacking);
        } catch (AssertionError error) {
            Assert.assertNull(expectedAttacking);
        }

        try {
            TestPokemon otherDefending = battle.getOtherDefending();
            otherDefending.assertSpecies(expectedDefending);
        } catch (AssertionError error) {
            Assert.assertNull(expectedDefending);
        }
    }
}
