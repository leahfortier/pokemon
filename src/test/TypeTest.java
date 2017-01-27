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
    public void typeAdvantageChartTest() {
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
    public void typeAdvantageTest() {
        Assert.assertTrue(TypeAdvantage.ELECTRIC.doesNotEffect(Type.GROUND));
        Assert.assertTrue(TypeAdvantage.WATER.isSuperEffective(Type.FIRE));
        Assert.assertTrue(TypeAdvantage.NORMAL.isNotVeryEffective(Type.ROCK));
    }

    @Test
    public void changeEffectivenessTest() {
        // Foresight
        changeEffectivenessTest(PokemonNamesies.GASTLY, AttackNamesies.TACKLE, PokemonManipulator.giveDefendingEffect(EffectNamesies.FORESIGHT));
        changeEffectivenessTest(PokemonNamesies.GASTLY, AttackNamesies.KARATE_CHOP, PokemonManipulator.giveDefendingEffect(EffectNamesies.FORESIGHT));

        // Miracle Eye
        changeEffectivenessTest(PokemonNamesies.UMBREON, AttackNamesies.PSYCHIC, PokemonManipulator.giveDefendingEffect(EffectNamesies.MIRACLE_EYE));

        // Ring Target
        changeEffectivenessTest(PokemonNamesies.PIDGEY, AttackNamesies.EARTHQUAKE, PokemonManipulator.giveDefendingItem(ItemNamesies.RING_TARGET));
        changeEffectivenessTest(PokemonNamesies.SANDSHREW, AttackNamesies.THUNDER_PUNCH, PokemonManipulator.giveDefendingItem(ItemNamesies.RING_TARGET));

        // Scrappy
        changeEffectivenessTest(PokemonNamesies.GASTLY, AttackNamesies.TACKLE, PokemonManipulator.giveAttackingAbility(AbilityNamesies.SCRAPPY));
        changeEffectivenessTest(PokemonNamesies.GASTLY, AttackNamesies.KARATE_CHOP, PokemonManipulator.giveAttackingAbility(AbilityNamesies.SCRAPPY));
    }

    private void changeEffectivenessTest(PokemonNamesies defendingPokemon, AttackNamesies attack, PokemonManipulator manipulator) {
        ActivePokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR);
        ActivePokemon defending = new TestPokemon(defendingPokemon);

        Battle battle = TestBattle.create(attacking, defending);

        // Make sure attack is unsuccessful without the effect
        attacking.setMove(new Move(attack));
        Assert.assertTrue(TypeAdvantage.doesNotEffect(attacking, defending, battle));

        // Cast the effect and make sure the move hits
        manipulator.manipulate(battle, attacking, defending);
        Assert.assertFalse(TypeAdvantage.doesNotEffect(attacking, defending, battle));
    }

    @Test
    public void filterTest() {
        // Super-effective attack should be reduced by 25%
        damageModifierTest(PokemonNamesies.SQUIRTLE, AttackNamesies.VINE_WHIP, .75, PokemonManipulator.giveDefendingAbility(AbilityNamesies.FILTER));
        damageModifierTest(PokemonNamesies.CHANDELURE, AttackNamesies.SURF, .75, PokemonManipulator.giveDefendingAbility(AbilityNamesies.PRISM_ARMOR));
        damageModifierTest(PokemonNamesies.DRIFBLIM, AttackNamesies.THUNDER_SHOCK, .75, PokemonManipulator.giveDefendingAbility(AbilityNamesies.SOLID_ROCK));

        // Neutral and not very effective moves should not be modified
        damageModifierTest(PokemonNamesies.RAICHU, AttackNamesies.VINE_WHIP, 1, PokemonManipulator.giveDefendingAbility(AbilityNamesies.FILTER));
        damageModifierTest(PokemonNamesies.BUDEW, AttackNamesies.THUNDER_SHOCK, 1, PokemonManipulator.giveDefendingAbility(AbilityNamesies.SOLID_ROCK));

        // Should not change modifier when the attacker has mold breaker
        damageModifierTest(
                PokemonNamesies.SQUIRTLE,
                AttackNamesies.VINE_WHIP,
                1,
                (battle, attacking, defending) -> {
                    attacking.setAbility(AbilityNamesies.MOLD_BREAKER);
                    defending.setAbility(AbilityNamesies.FILTER);
                }
        );

        // Prism Armor is unaffected by mold breaker
        damageModifierTest(
                PokemonNamesies.CHANDELURE,
                AttackNamesies.SURF,
                .75,
                (battle, attacking, defending) -> {
                    attacking.setAbility(AbilityNamesies.MOLD_BREAKER);
                    defending.setAbility(AbilityNamesies.PRISM_ARMOR);
                }
        );
    }

    @Test
    public void damageModifierTest() {
        // Tinted Lens doubles the power when not very effective
        damageModifierTest(PokemonNamesies.BUDEW, AttackNamesies.THUNDER_SHOCK, 2, PokemonManipulator.giveAttackingAbility(AbilityNamesies.TINTED_LENS));
        damageModifierTest(PokemonNamesies.SQUIRTLE, AttackNamesies.VINE_WHIP, 1, PokemonManipulator.giveAttackingAbility(AbilityNamesies.TINTED_LENS));
        damageModifierTest(PokemonNamesies.RAICHU, AttackNamesies.EMBER, 1, PokemonManipulator.giveAttackingAbility(AbilityNamesies.TINTED_LENS));

        // Expert Belt increases the power of super effective movws by 20%
        damageModifierTest(PokemonNamesies.SQUIRTLE, AttackNamesies.VINE_WHIP, 1.2, PokemonManipulator.giveAttackingItem(ItemNamesies.EXPERT_BELT));
        damageModifierTest(PokemonNamesies.SQUIRTLE, AttackNamesies.DARK_PULSE, 1, PokemonManipulator.giveAttackingItem(ItemNamesies.EXPERT_BELT));
        damageModifierTest(PokemonNamesies.SQUIRTLE, AttackNamesies.SURF, 1, PokemonManipulator.giveAttackingItem(ItemNamesies.EXPERT_BELT));

        // Tanga berry reduces super-effective bug moves
        damageModifierTest(PokemonNamesies.KADABRA, AttackNamesies.X_SCISSOR, .5, PokemonManipulator.giveDefendingItem(ItemNamesies.TANGA_BERRY));
        damageModifierTest(PokemonNamesies.KADABRA, AttackNamesies.CRUNCH, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.TANGA_BERRY));
        damageModifierTest(PokemonNamesies.KADABRA, AttackNamesies.SURF, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.TANGA_BERRY));
        damageModifierTest(PokemonNamesies.KADABRA, AttackNamesies.PSYBEAM, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.TANGA_BERRY));

        // Yache berry reduces super-effective ice moves
        damageModifierTest(PokemonNamesies.DRAGONITE, AttackNamesies.ICE_BEAM, .5, PokemonManipulator.giveDefendingItem(ItemNamesies.YACHE_BERRY));
        damageModifierTest(PokemonNamesies.DRAGONITE, AttackNamesies.OUTRAGE, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.YACHE_BERRY));
        damageModifierTest(PokemonNamesies.DRAGONITE, AttackNamesies.TACKLE, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.YACHE_BERRY));
        damageModifierTest(PokemonNamesies.DRAGONITE, AttackNamesies.SURF, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.YACHE_BERRY));
    }

    private void damageModifierTest(PokemonNamesies defendingName, AttackNamesies attackName, double expectedChange, PokemonManipulator manipulator) {
        TestPokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR);
        TestPokemon defending = new TestPokemon(defendingName);

        TestBattle battle = TestBattle.create(attacking, defending);

        attacking.setupMove(attackName, battle, defending);
        double beforeModifier = battle.getDamageModifier(attacking, defending);

        manipulator.manipulate(battle, attacking, defending);
        double afterModifier = battle.getDamageModifier(attacking, defending);

        Assert.assertTrue(
                StringUtils.spaceSeparated(defendingName, attackName, beforeModifier, afterModifier, expectedChange),
                expectedChange*beforeModifier == afterModifier
        );
    }

    @Test
    public void freezeDryTest() {
        AttackNamesies freezeDry = AttackNamesies.FREEZE_DRY;

        // Freeze-Dry is super effective against water types
        advantageChecker(2, freezeDry, PokemonNamesies.SQUIRTLE);

        // Should have neutral effectiveness against a Water/Ice type, instead of .25
        advantageChecker(1, freezeDry, PokemonNamesies.LAPRAS);

        // Super effective against a non-water type Pokemon that has been soaked
        advantageChecker(
                2,
                freezeDry,
                (battle, attacking, defending) -> attacking.callNewMove(battle, defending, new Move(AttackNamesies.SOAK)),
                PokemonNamesies.PIDGEY
        );
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
