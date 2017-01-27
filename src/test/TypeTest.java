package test;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import type.Type;
import type.TypeAdvantage;
import util.StringUtils;

public class TypeTest {
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
            {1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 1}}; // No Type

    @Test
    public void typeAdvantageTest() {
        for (Type attacking : Type.values()) {
            TypeAdvantage advantage = attacking.getAdvantage();
            for (int i = 0; i < typeAdvantage[attacking.getIndex()].length; i++) {
                Type defending = Type.values()[i];
                double chartAdv = typeAdvantage[attacking.getIndex()][i];
                double classAdv = advantage.getAdvantage(defending);

                Assert.assertTrue(
                        StringUtils.spaceSeparated(attacking, defending, chartAdv, classAdv),
                        chartAdv  == classAdv
                );
            }
        }
    }

    @Test
    public void changeEffectivenessTest() {
        // Foresight
        changeEffectivenessTest(
                PokemonNamesies.GASTLY,
                AttackNamesies.TACKLE,
                PokemonManipulator.giveDefendingEffect(EffectNamesies.FORESIGHT)
        );

        // Miracle Eye
        changeEffectivenessTest(
                PokemonNamesies.UMBREON,
                AttackNamesies.PSYCHIC,
                PokemonManipulator.giveDefendingEffect(EffectNamesies.MIRACLE_EYE)
        );

        // Ring Target
        changeEffectivenessTest(
                PokemonNamesies.PIDGEY,
                AttackNamesies.EARTHQUAKE,
                PokemonManipulator.giveDefendingItem(ItemNamesies.RING_TARGET)
        );

        // Scrappy
        changeEffectivenessTest(
                PokemonNamesies.GASTLY,
                AttackNamesies.TACKLE,
                PokemonManipulator.giveAttackingAbility(AbilityNamesies.SCRAPPY)
        );
    }

    private void changeEffectivenessTest(PokemonNamesies defendingPokemon, AttackNamesies attack, PokemonManipulator manipulator) {
        ActivePokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR);
        ActivePokemon defending = new TestPokemon(defendingPokemon);

        Battle battle = TestBattle.create(attacking, defending);

        // Make sure attack is unsuccessful without the effect
        attacking.setMove(new Move(attack));
        Assert.assertTrue(TypeAdvantage.getAdvantage(attacking, defending, battle) == 0);

        // Cast the effect and make sure the move hits
        manipulator.manipulate(battle, attacking, defending);
        Assert.assertTrue(TypeAdvantage.getAdvantage(attacking, defending, battle) > 0);
    }

    @Test
    public void freezeDryTest() {
        AttackNamesies freezeDry = AttackNamesies.FREEZE_DRY;

        // Freeze-Dry is super effective against water types
        advantageChecker(2, freezeDry, PokemonNamesies.SQUIRTLE);

        // Should have neutral effectiveness against a Water/Ice type, instead of .25
        advantageChecker(1, freezeDry, PokemonNamesies.LAPRAS);

        // Super effective against a non-water type Pokemon that has been soaked
        PokemonManipulator soak = (battle, attacking, defending) ->
                attacking.callNewMove(battle, defending, new Move(AttackNamesies.SOAK));
        advantageChecker(2, freezeDry, soak, PokemonNamesies.PIDGEY);
    }

    @Test
    public void flyingPressTest() {
        ActivePokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR);
        attacking.setMove(new Move(AttackNamesies.FLYING_PRESS));

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
                PokemonNamesies.CARBINK,
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

        // Electrify should make the attack Electic/Flying
        PokemonManipulator electrify = PokemonManipulator.giveAttackingEffect(EffectNamesies.ELECTRIFIED);
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
        ActivePokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR);
        for (PokemonNamesies pokemonNamesies : defendingPokemon) {
            ActivePokemon defending = new TestPokemon(pokemonNamesies);
            Battle battle = TestBattle.create(attacking, defending);

            attacking.setMove(new Move(attack));
            manipulator.manipulate(battle, attacking, defending);

            double actualAdvantage = TypeAdvantage.getAdvantage(attacking, defending, battle);
            Assert.assertTrue(
                    StringUtils.spaceSeparated(attack, pokemonNamesies, expectedAdvantage, actualAdvantage),
                    actualAdvantage == expectedAdvantage
            );
        }
    }
}
