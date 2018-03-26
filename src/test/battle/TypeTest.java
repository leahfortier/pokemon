package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import test.BaseTest;
import test.TestPokemon;
import test.TestUtils;
import type.Type;
import type.TypeAdvantage;
import util.string.StringUtils;

public class TypeTest extends BaseTest {
    private static final double typeAdvantage[][] = {
            {1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, .5,  0,  1,  1, .5,  1, 1}, // Normal
            {1, .5, .5,  1,  2,  2,  1,  1,  1,  1,  1,  2, .5,  1, .5,  1,  2,  1, 1}, // Fire
            {1,  2, .5,  1, .5,  1,  1,  1,  2,  1,  1,  1,  2,  1, .5,  1,  1,  1, 1}, // Water
            {1,  1,  2, .5, .5,  1,  1,  1,  0,  2,  1,  1,  1,  1, .5,  1,  1,  1, 1}, // Electric
            {1, .5,  2,  1, .5,  1,  1, .5,  2, .5,  1, .5,  2,  1, .5,  1, .5,  1, 1}, // Grass
            {1, .5, .5,  1,  2, .5,  1,  1,  2,  2,  1,  1,  1,  1,  2,  1, .5,  1, 1}, // Ice
            {2,  1,  1,  1,  1,  2,  1, .5,  1, .5, .5, .5,  2,  0,  1,  2,  2, .5, 1}, // Fighting
            {1,  1,  1,  1,  2,  1,  1, .5, .5,  1,  1,  1, .5, .5,  1,  1,  0,  2, 1}, // Poison
            {1,  2,  1,  2, .5,  1,  1,  2,  1,  0,  1, .5,  2,  1,  1,  1,  2,  1, 1}, // Ground
            {1,  1,  1, .5,  2,  1,  2,  1,  1,  1,  1,  2, .5,  1,  1,  1, .5,  1, 1}, // Flying
            {1,  1,  1,  1,  1,  1,  2,  2,  1,  1, .5,  1,  1,  1,  1,  0, .5,  1, 1}, // Psychic
            {1, .5,  1,  1,  2,  1, .5, .5,  1, .5,  2,  1,  1, .5,  1,  2, .5, .5, 1}, // Bug
            {1,  2,  1,  1,  1,  2, .5,  1, .5,  2,  1,  2,  1,  1,  1,  1, .5,  1, 1}, // Rock
            {0,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  1,  1,  2,  1, .5,  1,  1, 1}, // Ghost
            {1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  1, .5,  0, 1}, // Dragon
            {1,  1,  1,  1,  1,  1, .5,  1,  1,  1,  2,  1,  1,  2,  1, .5,  1, .5, 1}, // Dark
            {1, .5, .5, .5,  1,  2,  1,  1,  1,  1,  1,  1,  2,  1,  1,  1, .5,  2, 1}, // Steel
            {1, .5,  1,  1,  1,  1,  2, .5,  1,  1,  1,  1,  1,  1,  2,  2, .5,  1, 1}, // Fairy
            {1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 1}  // No Type
    };

    @Test
    public void typeAdvantageChartTest() {
        for (Type attacking : Type.values()) {
            TypeAdvantage advantage = attacking.getAdvantage();
            for (int i = 0; i < typeAdvantage[attacking.getIndex()].length; i++) {
                Type defending = Type.values()[i];
                double chartAdv = typeAdvantage[attacking.getIndex()][i];
                double classAdv = advantage.getAdvantage(defending);

                Assert.assertTrue(
                        StringUtils.spaceSeparated(attacking, defending, chartAdv, classAdv),
                        chartAdv == classAdv
                );
            }
        }
    }

    @Test
    public void typeAdvantageTest() {
        Assert.assertTrue(TypeAdvantage.ELECTRIC.doesNotEffect(Type.GROUND));
        Assert.assertTrue(TypeAdvantage.WATER.isSuperEffective(Type.FIRE));
        Assert.assertTrue(TypeAdvantage.NORMAL.isNotVeryEffective(Type.ROCK));
    }

    @Test
    public void changeEffectivenessTest() {
        // Foresight
        PokemonManipulator foresight = PokemonManipulator.giveDefendingEffect(PokemonEffectNamesies.FORESIGHT);
        changeEffectivenessTest(PokemonNamesies.GASTLY, AttackNamesies.TACKLE, foresight);
        changeEffectivenessTest(PokemonNamesies.GASTLY, AttackNamesies.KARATE_CHOP, foresight);

        // Miracle Eye
        PokemonManipulator miracleEye = PokemonManipulator.giveDefendingEffect(PokemonEffectNamesies.MIRACLE_EYE);
        changeEffectivenessTest(PokemonNamesies.UMBREON, AttackNamesies.PSYCHIC, miracleEye);

        // Ring Target
        PokemonManipulator ringTarget = PokemonManipulator.giveDefendingItem(ItemNamesies.RING_TARGET);
        changeEffectivenessTest(PokemonNamesies.PIDGEY, AttackNamesies.EARTHQUAKE, ringTarget);
        changeEffectivenessTest(PokemonNamesies.SANDSHREW, AttackNamesies.THUNDER_PUNCH, ringTarget);

        // Scrappy
        PokemonManipulator scrappy = PokemonManipulator.giveAttackingAbility(AbilityNamesies.SCRAPPY);
        changeEffectivenessTest(PokemonNamesies.GASTLY, AttackNamesies.TACKLE, scrappy);
        changeEffectivenessTest(PokemonNamesies.GASTLY, AttackNamesies.KARATE_CHOP, scrappy);
    }

    private void changeEffectivenessTest(PokemonNamesies defendingPokemon, AttackNamesies attack, PokemonManipulator manipulator) {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, defendingPokemon);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Make sure attack is unsuccessful without the effect
        attacking.setupMove(attack, battle);
        Assert.assertTrue(TypeAdvantage.doesNotEffect(attacking, defending, battle));

        // Cast the effect and make sure the move hits
        manipulator.manipulate(battle);
        Assert.assertFalse(TypeAdvantage.doesNotEffect(attacking, defending, battle));
    }

    @Test
    public void freezeDryTest() {
        AttackNamesies freezeDry = AttackNamesies.FREEZE_DRY;

        // Freeze-Dry is super effective against water types
        advantageChecker(2, freezeDry, PokemonNamesies.SQUIRTLE);

        // Should have neutral effectiveness against a Water/Ice type, instead of .25
        advantageChecker(1, freezeDry, PokemonNamesies.LAPRAS);

        // Super effective against a non-water type Pokemon that has been soaked
        advantageChecker(2, freezeDry, PokemonManipulator.attackingAttack(AttackNamesies.SOAK), PokemonNamesies.PIDGEY);
    }

    @Test
    public void flyingPressTest() {
        PokemonNamesies[] superDuperEffective = {
                PokemonNamesies.ABOMASNOW,
                PokemonNamesies.DEERLING,
                PokemonNamesies.BRELOOM,
                PokemonNamesies.NUZLEAF,
                PokemonNamesies.CRABOMINABLE,
                PokemonNamesies.SCRAGGY,
                PokemonNamesies.WEAVILE
        };

        PokemonNamesies[] superEffective = {
                // Single typed strong
                PokemonNamesies.LICKITUNG,
                PokemonNamesies.TANGELA,
                PokemonNamesies.GLACEON,
                PokemonNamesies.MACHAMP,
                PokemonNamesies.UMBREON,

                // One strong, one neutral
                PokemonNamesies.LITLEO,
                PokemonNamesies.DIGGERSBY,
                PokemonNamesies.DRAMPA,
                PokemonNamesies.TORTERRA,
                PokemonNamesies.LILEEP,
                PokemonNamesies.PARAS,
                PokemonNamesies.FERROSEED,
                PokemonNamesies.AMAURA,
                PokemonNamesies.KYUREM,
                PokemonNamesies.HERACROSS,
                PokemonNamesies.LUCARIO,
                PokemonNamesies.HAKAMO_O,
                PokemonNamesies.PAWNIARD,
                PokemonNamesies.DEINO
        };

        PokemonNamesies[] neutralEffective = {
                // Single typed neutral
                PokemonNamesies.SQUIRTLE,
                PokemonNamesies.CHARMANDER,
                PokemonNamesies.SANDSHREW,
                PokemonNamesies.BONSLY,
                PokemonNamesies.CATERPIE,
                PokemonNamesies.KLINK,
                PokemonNamesies.DRAGONAIR,

                // One weak, one strong
                PokemonNamesies.PIDGEOT,
                PokemonNamesies.GIRAFARIG,
                PokemonNamesies.JIGGLYPUFF,
                PokemonNamesies.HOPPIP,
                PokemonNamesies.EXEGGCUTE,
                PokemonNamesies.COTTONEE,
                PokemonNamesies.JYNX
        };

        PokemonNamesies[] notVeryEffective = {
                // Single typed weak
                PokemonNamesies.PIKACHU,
                PokemonNamesies.ARBOK,
                PokemonNamesies.TORNADUS,
                PokemonNamesies.ALAKAZAM,
                PokemonNamesies.CLEFABLE,

                // One weak, one neutral
                PokemonNamesies.TENTACOOL,
                PokemonNamesies.MANTINE,
                PokemonNamesies.SLOWBRO,
                PokemonNamesies.MARILL,
                PokemonNamesies.CHARIZARD,
                PokemonNamesies.VICTINI,
                PokemonNamesies.CARBINK
        };

        PokemonNamesies[] superNotVeryEffective = {
                PokemonNamesies.ZAPDOS,
                PokemonNamesies.DEDENNE,
                PokemonNamesies.ZUBAT,
                PokemonNamesies.NATU,
                PokemonNamesies.TOGEKISS,
                PokemonNamesies.MR_MIME
        };

        PokemonNamesies[] noEffect = {
                PokemonNamesies.BANETTE,
                PokemonNamesies.PHANTUMP,
                PokemonNamesies.FROSLASS,
                PokemonNamesies.SABLEYE,
                PokemonNamesies.GASTLY,
                PokemonNamesies.MIMIKYU
        };

        // Flying Press is simultaneously a Fighting/Flying attack
        AttackNamesies flyingPress = AttackNamesies.FLYING_PRESS;
        advantageChecker(4, flyingPress, superDuperEffective);
        advantageChecker(2, flyingPress, superEffective);
        advantageChecker(1, flyingPress, neutralEffective);
        advantageChecker(.5, flyingPress, notVeryEffective);
        advantageChecker(.25, flyingPress, superNotVeryEffective);
        advantageChecker(0, flyingPress, noEffect);

        // Electrify should make the attack Electric/Flying
        PokemonManipulator electrify = PokemonManipulator.defendingAttack(AttackNamesies.ELECTRIFY);
        advantageChecker(4, flyingPress, electrify, PokemonNamesies.HAWLUCHA, PokemonNamesies.BUTTERFREE);
        advantageChecker(2, flyingPress, electrify, PokemonNamesies.CATERPIE, PokemonNamesies.LARVESTA);
        advantageChecker(1, flyingPress, electrify, PokemonNamesies.ALAKAZAM, PokemonNamesies.AERODACTYL);
        advantageChecker(.5, flyingPress, electrify, PokemonNamesies.ZAPDOS, PokemonNamesies.MAWILE, PokemonNamesies.KLINK);
        advantageChecker(.25, flyingPress, electrify, PokemonNamesies.PIKACHU, PokemonNamesies.SHIELDON);
        advantageChecker(.125, flyingPress, electrify, PokemonNamesies.ZEKROM, PokemonNamesies.MAGNEZONE);
        advantageChecker(0, flyingPress, electrify, PokemonNamesies.SANDSHREW, PokemonNamesies.GLIGAR);

        // Normalize should make the attack Normal/Flying
        PokemonManipulator normalize = PokemonManipulator.giveAttackingAbility(AbilityNamesies.NORMALIZE);
        advantageChecker(4, flyingPress, normalize, PokemonNamesies.BRELOOM, PokemonNamesies.PARASECT);
        advantageChecker(2, flyingPress, normalize, PokemonNamesies.MACHAMP, PokemonNamesies.BULBASAUR);
        advantageChecker(1, flyingPress, normalize, PokemonNamesies.PIDGEY, PokemonNamesies.JOLTIK);
        advantageChecker(.5, flyingPress, normalize, PokemonNamesies.PIKACHU, PokemonNamesies.LILEEP, PokemonNamesies.LANTURN);
        advantageChecker(.25, flyingPress, normalize, PokemonNamesies.KLINK, PokemonNamesies.BOLDORE);
        advantageChecker(.125, flyingPress, normalize, PokemonNamesies.MAGNEMITE);
        advantageChecker(.0625, flyingPress, normalize, PokemonNamesies.SHIELDON);
        advantageChecker(0, flyingPress, normalize, PokemonNamesies.GASTLY, PokemonNamesies.BANETTE);
    }

    private void advantageChecker(double expectedAdvantage, AttackNamesies attack, PokemonNamesies... defendingPokemon) {
        advantageChecker(expectedAdvantage, attack, PokemonManipulator.empty(), defendingPokemon);
    }

    private void advantageChecker(double expectedAdvantage, AttackNamesies attack, PokemonManipulator manipulator, PokemonNamesies... defendingPokemon) {
        advantageChecker(null, expectedAdvantage, attack, manipulator, defendingPokemon);
    }

    private void advantageChecker(Double beforeExpected, double afterExpected, AttackNamesies attack, PokemonManipulator manipulator, PokemonNamesies... defendingPokemon) {
        advantageChecker(beforeExpected, afterExpected, PokemonNamesies.BULBASAUR, attack, manipulator, defendingPokemon);
    }

    private void advantageChecker(Double beforeExpected, double afterExpected, PokemonNamesies attackingPokemon, AttackNamesies attack, PokemonManipulator manipulator, PokemonNamesies... defendingPokemon) {
        for (PokemonNamesies pokemonNamesies : defendingPokemon) {
            TestBattle battle = TestBattle.create(attackingPokemon, pokemonNamesies);
            TestPokemon attacking = battle.getAttacking();
            TestPokemon defending = battle.getDefending();

            if (beforeExpected != null) {
                attacking.setupMove(attack, battle);
                double beforeActual = TypeAdvantage.getAdvantage(attacking, defending, battle);
                TestUtils.assertEquals(
                        StringUtils.spaceSeparated(attack, pokemonNamesies, beforeExpected, beforeActual),
                        beforeExpected, beforeActual
                );
            }

            manipulator.manipulate(battle);
            attacking.setupMove(attack, battle);

            double afterActual = TypeAdvantage.getAdvantage(attacking, defending, battle);
            TestUtils.assertEquals(
                    StringUtils.spaceSeparated(attack, pokemonNamesies, afterExpected, afterActual),
                    afterExpected, afterActual
            );
        }
    }

    @Test
    public void changeAttackTypeTest() {
        PokemonManipulator electrify = PokemonManipulator.defendingAttack(AttackNamesies.ELECTRIFY);
        advantageChecker(2.0, 1, AttackNamesies.SURF, electrify, PokemonNamesies.GROWLITHE);
        advantageChecker(.5, 2, AttackNamesies.SURF, electrify, PokemonNamesies.SQUIRTLE);
        advantageChecker(2.0, 2, AttackNamesies.FREEZE_DRY, electrify, PokemonNamesies.SQUIRTLE);
        advantageChecker(0.0, 1, AttackNamesies.TACKLE, electrify, PokemonNamesies.GASTLY);

        // Electrify affects Ground-type Pokemon
        advantageChecker(1.0, 0, PokemonNamesies.SANDSHREW, AttackNamesies.TACKLE, electrify, PokemonNamesies.SANDSHREW);

        // Electrify only works for the current turn
        advantageChecker(2.0, 2, AttackNamesies.SURF, (battle, attacking, defending) -> battle.defendingFight(AttackNamesies.ELECTRIFY), PokemonNamesies.GROWLITHE);

        // Plasma Fists only affects Normal-type moves
        PokemonManipulator plasmaFists = PokemonManipulator.defendingAttack(AttackNamesies.PLASMA_FISTS);
        advantageChecker(2.0, 2, AttackNamesies.SURF, plasmaFists, PokemonNamesies.GROWLITHE);
        advantageChecker(.5, .5, AttackNamesies.SURF, plasmaFists, PokemonNamesies.SQUIRTLE);
        advantageChecker(2.0, 2, AttackNamesies.FREEZE_DRY, plasmaFists, PokemonNamesies.SQUIRTLE);
        advantageChecker(0.0, 1, AttackNamesies.TACKLE, plasmaFists, PokemonNamesies.GASTLY);
        advantageChecker(1.0, 0, AttackNamesies.TACKLE, plasmaFists, PokemonNamesies.SANDSHREW);
        advantageChecker(.5, 0, AttackNamesies.SWIFT, plasmaFists, PokemonNamesies.GEODUDE);

        // Plasma Fists doesn't affect Sandshrew so the effect shouldn't go through
        advantageChecker(1.0, 1, PokemonNamesies.SANDSHREW, AttackNamesies.TACKLE, plasmaFists, PokemonNamesies.SANDSHREW);

        // Plasma Fists also only works for the current turn
        advantageChecker(0.0, 0, AttackNamesies.TACKLE, (battle, attacking, defending) -> battle.defendingFight(AttackNamesies.PLASMA_FISTS), PokemonNamesies.GASTLY);

        // Galvanize changes normal type moves to electric type
        PokemonManipulator galvanize = PokemonManipulator.giveAttackingAbility(AbilityNamesies.GALVANIZE);
        advantageChecker(2.0, 2, AttackNamesies.SURF, galvanize, PokemonNamesies.GROWLITHE);
        advantageChecker(.5, .5, AttackNamesies.SURF, galvanize, PokemonNamesies.SQUIRTLE);
        advantageChecker(2.0, 2, AttackNamesies.FREEZE_DRY, galvanize, PokemonNamesies.SQUIRTLE);
        advantageChecker(0.0, 1, AttackNamesies.TACKLE, galvanize, PokemonNamesies.GASTLY);
        advantageChecker(1.0, 0, AttackNamesies.TACKLE, galvanize, PokemonNamesies.SANDSHREW);
        advantageChecker(.5, 0, AttackNamesies.SWIFT, galvanize, PokemonNamesies.GEODUDE);

        // TODO: Electrify + Normalize
        // TODO: Normalize + Thunder Wave
        // Normalize changes all moves to Normal-type
        PokemonManipulator normalize = PokemonManipulator.giveAttackingAbility(AbilityNamesies.NORMALIZE);
        advantageChecker(2.0, 1, AttackNamesies.SURF, normalize, PokemonNamesies.GROWLITHE);
        advantageChecker(.5, 1, AttackNamesies.SURF, normalize, PokemonNamesies.SQUIRTLE);
        advantageChecker(2.0, 2, AttackNamesies.FREEZE_DRY, normalize, PokemonNamesies.SQUIRTLE);
        advantageChecker(1.0, 1, AttackNamesies.WATER_GUN, normalize, PokemonNamesies.GLACEON);
        advantageChecker(0.0, 0, AttackNamesies.TACKLE, normalize, PokemonNamesies.GASTLY);
        advantageChecker(0.0, 0, AttackNamesies.KARATE_CHOP, normalize, PokemonNamesies.GASTLY);
        advantageChecker(.5, .5, AttackNamesies.SWIFT, normalize, PokemonNamesies.GEODUDE);

        // Liquid voice changes all sound-based moves to Water-type
        PokemonManipulator liquidVoice = PokemonManipulator.giveAttackingAbility(AbilityNamesies.LIQUID_VOICE);
        advantageChecker(2.0, 2, AttackNamesies.SURF, liquidVoice, PokemonNamesies.GROWLITHE);
        advantageChecker(.5, .5, AttackNamesies.SWIFT, liquidVoice, PokemonNamesies.GEODUDE);
        advantageChecker(2.0, 2, AttackNamesies.FREEZE_DRY, liquidVoice, PokemonNamesies.SQUIRTLE);
        advantageChecker(0.0, 0, AttackNamesies.TACKLE, liquidVoice, PokemonNamesies.GASTLY);
        advantageChecker(0.0, 0, AttackNamesies.KARATE_CHOP, liquidVoice, PokemonNamesies.GASTLY);
        advantageChecker(0.0, 1, AttackNamesies.BOOMBURST, liquidVoice, PokemonNamesies.GASTLY);
        advantageChecker(2.0, 1, AttackNamesies.BUG_BUZZ, liquidVoice, PokemonNamesies.ALAKAZAM);
        advantageChecker(.5, 2, AttackNamesies.BUG_BUZZ, liquidVoice, PokemonNamesies.GROWLITHE);
    }

    @Test
    public void effectiveTest() {
        // "Self-target" damage dealing, damage dealing, and fucking Thunder Wave -- all ineffective against Ground-type
        checkEffective(false, AttackNamesies.CHARGE_BEAM, new TestInfo().defending(PokemonNamesies.SANDSHREW));
        checkEffective(false, AttackNamesies.THUNDER_SHOCK, new TestInfo().defending(PokemonNamesies.SANDSHREW));
        checkEffective(false, AttackNamesies.THUNDER_WAVE, new TestInfo().defending(PokemonNamesies.SANDSHREW));

        // Status moves (self-target or not excluding Thunder Wave) and field moves are fine
        checkEffective(true, AttackNamesies.EERIE_IMPULSE, new TestInfo().attacking(PokemonNamesies.SANDSHREW)
                                                                         .defending(PokemonNamesies.SANDSHREW));
        checkEffective(true, AttackNamesies.MAGNET_RISE, new TestInfo().attacking(PokemonNamesies.SANDSHREW)
                                                                       .defending(PokemonNamesies.SANDSHREW));
        checkEffective(true, AttackNamesies.ELECTRIC_TERRAIN, new TestInfo().attacking(PokemonNamesies.SANDSHREW)
                                                                            .defending(PokemonNamesies.SANDSHREW));

        checkEffective(true, AttackNamesies.THUNDER_WAVE, new TestInfo().attacking(PokemonNamesies.SANDSHREW));
        checkEffective(false, AttackNamesies.THUNDER_WAVE, new TestInfo().attacking(PokemonNamesies.SANDSHREW)
                                                                         .defending(AbilityNamesies.MAGIC_BOUNCE));
        checkEffective(true, AttackNamesies.CHARGE_BEAM, new TestInfo().attacking(PokemonNamesies.SANDSHREW)
                                                                       .defending(AbilityNamesies.MAGIC_BOUNCE));

        // Lightningrod absorbs Electric moves
        checkEffective(false, AttackNamesies.THUNDER_WAVE, new TestInfo().defending(AbilityNamesies.LIGHTNINGROD));
    }

    private void checkEffective(boolean effective, AttackNamesies attackNamesies, TestInfo testInfo) {
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking();

        testInfo.manipulate(battle);
        attacking.apply(effective, attackNamesies, battle);
    }
}
