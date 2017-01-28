package test;

import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import util.StringUtils;

public class PowerChangeTest {
    @Test
    public void filterTest() {
        // Super-effective attack should be reduced by 25%
        powerModifierTest(PokemonNamesies.SQUIRTLE, AttackNamesies.VINE_WHIP, .75, PokemonManipulator.giveDefendingAbility(AbilityNamesies.FILTER));
        powerModifierTest(PokemonNamesies.CHANDELURE, AttackNamesies.SURF, .75, PokemonManipulator.giveDefendingAbility(AbilityNamesies.PRISM_ARMOR));
        powerModifierTest(PokemonNamesies.DRIFBLIM, AttackNamesies.THUNDER_SHOCK, .75, PokemonManipulator.giveDefendingAbility(AbilityNamesies.SOLID_ROCK));

        // Neutral and not very effective moves should not be modified
        powerModifierTest(PokemonNamesies.RAICHU, AttackNamesies.VINE_WHIP, 1, PokemonManipulator.giveDefendingAbility(AbilityNamesies.FILTER));
        powerModifierTest(PokemonNamesies.BUDEW, AttackNamesies.THUNDER_SHOCK, 1, PokemonManipulator.giveDefendingAbility(AbilityNamesies.SOLID_ROCK));

        // Should not change modifier when the attacker has mold breaker
        powerModifierTest(
                PokemonNamesies.SQUIRTLE,
                AttackNamesies.VINE_WHIP,
                1,
                (battle, attacking, defending) -> {
                    attacking.setAbility(AbilityNamesies.MOLD_BREAKER);
                    defending.setAbility(AbilityNamesies.FILTER);
                }
        );

        // Prism Armor is unaffected by mold breaker
        powerModifierTest(
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
    public void typeEffectiveTest() {
        // Tinted Lens doubles the power when not very effective
        powerModifierTest(PokemonNamesies.BUDEW, AttackNamesies.THUNDER_SHOCK, 2, PokemonManipulator.giveAttackingAbility(AbilityNamesies.TINTED_LENS));
        powerModifierTest(PokemonNamesies.SQUIRTLE, AttackNamesies.VINE_WHIP, 1, PokemonManipulator.giveAttackingAbility(AbilityNamesies.TINTED_LENS));
        powerModifierTest(PokemonNamesies.RAICHU, AttackNamesies.EMBER, 1, PokemonManipulator.giveAttackingAbility(AbilityNamesies.TINTED_LENS));

        // Expert Belt increases the power of super effective moves by 20%
        powerModifierTest(PokemonNamesies.SQUIRTLE, AttackNamesies.VINE_WHIP, 1.2, PokemonManipulator.giveAttackingItem(ItemNamesies.EXPERT_BELT));
        powerModifierTest(PokemonNamesies.SQUIRTLE, AttackNamesies.DARK_PULSE, 1, PokemonManipulator.giveAttackingItem(ItemNamesies.EXPERT_BELT));
        powerModifierTest(PokemonNamesies.SQUIRTLE, AttackNamesies.SURF, 1, PokemonManipulator.giveAttackingItem(ItemNamesies.EXPERT_BELT));

        // Tanga berry reduces super-effective bug moves
        powerModifierTest(PokemonNamesies.KADABRA, AttackNamesies.X_SCISSOR, .5, PokemonManipulator.giveDefendingItem(ItemNamesies.TANGA_BERRY));
        powerModifierTest(PokemonNamesies.KADABRA, AttackNamesies.CRUNCH, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.TANGA_BERRY));
        powerModifierTest(PokemonNamesies.KADABRA, AttackNamesies.SURF, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.TANGA_BERRY));
        powerModifierTest(PokemonNamesies.KADABRA, AttackNamesies.PSYBEAM, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.TANGA_BERRY));

        // Yache berry reduces super-effective ice moves
        powerModifierTest(PokemonNamesies.DRAGONITE, AttackNamesies.ICE_BEAM, .5, PokemonManipulator.giveDefendingItem(ItemNamesies.YACHE_BERRY));
        powerModifierTest(PokemonNamesies.DRAGONITE, AttackNamesies.OUTRAGE, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.YACHE_BERRY));
        powerModifierTest(PokemonNamesies.DRAGONITE, AttackNamesies.TACKLE, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.YACHE_BERRY));
        powerModifierTest(PokemonNamesies.DRAGONITE, AttackNamesies.SURF, 1, PokemonManipulator.giveDefendingItem(ItemNamesies.YACHE_BERRY));
    }

    /*
    TODO: Other weather things that should be tested unrelated to PowerChange
        - Raise Sp.Defense of certain types in Sandstorm
        - Don't buffet those bros
        - Damp Rock and those other bros
        - No freezing when sunny
     */
    @Test
    public void weatherTest() {
        // Weather effects boost/lower the power of certain types of moves
        powerModifierTest(AttackNamesies.SURF, 1.5, PokemonManipulator.giveBattleEffect(EffectNamesies.RAINING));
        powerModifierTest(AttackNamesies.FLAMETHROWER, .5, PokemonManipulator.giveBattleEffect(EffectNamesies.RAINING));
        powerModifierTest(AttackNamesies.THUNDERBOLT, 1, PokemonManipulator.giveBattleEffect(EffectNamesies.RAINING));

        powerModifierTest(AttackNamesies.FLAMETHROWER, 1.5, PokemonManipulator.giveBattleEffect(EffectNamesies.SUNNY));
        powerModifierTest(AttackNamesies.SURF, .5, PokemonManipulator.giveBattleEffect(EffectNamesies.SUNNY));
        powerModifierTest(AttackNamesies.THUNDERBOLT, 1, PokemonManipulator.giveBattleEffect(EffectNamesies.SUNNY));
    }

    @Test
    public void powerModifierTest() {
        // Adamant Orb boosts dragon and steel type moves but only for Dialga
        powerModifierTest(PokemonNamesies.DIALGA, PokemonNamesies.BIDOOF, AttackNamesies.OUTRAGE, 1.2, PokemonManipulator.giveAttackingItem(ItemNamesies.ADAMANT_ORB));
        powerModifierTest(PokemonNamesies.DIALGA, PokemonNamesies.BIDOOF, AttackNamesies.TACKLE, 1, PokemonManipulator.giveAttackingItem(ItemNamesies.ADAMANT_ORB));
        powerModifierTest(PokemonNamesies.DRAGONAIR, PokemonNamesies.BIDOOF, AttackNamesies.OUTRAGE, 1, PokemonManipulator.giveAttackingItem(ItemNamesies.ADAMANT_ORB));

        // Charcoal
        powerModifierTest(PokemonNamesies.CHARMANDER, PokemonNamesies.BIDOOF, AttackNamesies.FLAMETHROWER, 1.2, PokemonManipulator.giveAttackingItem(ItemNamesies.CHARCOAL));
        powerModifierTest(PokemonNamesies.BUDEW, PokemonNamesies.BIDOOF, AttackNamesies.FLAMETHROWER, 1.2, PokemonManipulator.giveAttackingItem(ItemNamesies.CHARCOAL));
        powerModifierTest(PokemonNamesies.CHARIZARD, PokemonNamesies.BIDOOF, AttackNamesies.AIR_SLASH, 1, PokemonManipulator.giveAttackingItem(ItemNamesies.CHARCOAL));

        // Life orb
        powerModifierTest(AttackNamesies.AIR_SLASH, 5324.0/4096.0, PokemonManipulator.giveAttackingItem(ItemNamesies.LIFE_ORB));
        powerModifierTest(AttackNamesies.OUTRAGE, 5324.0/4096.0, PokemonManipulator.giveAttackingItem(ItemNamesies.LIFE_ORB));

        // Muscle band boosts Physical moves
        powerModifierTest(AttackNamesies.VINE_WHIP, 1.1, PokemonManipulator.giveAttackingItem(ItemNamesies.MUSCLE_BAND));
        powerModifierTest(AttackNamesies.ENERGY_BALL, 1, PokemonManipulator.giveAttackingItem(ItemNamesies.MUSCLE_BAND));
    }

    @Test
    public void terrainTest() {
        // Terrain effects boost/lower the power of certain types of moves
        powerModifierTest(AttackNamesies.THUNDER, 1.5, PokemonManipulator.giveBattleEffect(EffectNamesies.ELECTRIC_TERRAIN));
        powerModifierTest(AttackNamesies.PSYCHIC, 1.5, PokemonManipulator.giveBattleEffect(EffectNamesies.PSYCHIC_TERRAIN));
        powerModifierTest(AttackNamesies.SOLAR_BEAM, 1.5, PokemonManipulator.giveBattleEffect(EffectNamesies.GRASSY_TERRAIN));
        powerModifierTest(AttackNamesies.OUTRAGE, .5, PokemonManipulator.giveBattleEffect(EffectNamesies.MISTY_TERRAIN));

        // Different move type -- no change
        powerModifierTest(AttackNamesies.DAZZLING_GLEAM, 1, PokemonManipulator.giveBattleEffect(EffectNamesies.MISTY_TERRAIN));

        // Float with Flying -- no change
        powerModifierTest(
                PokemonNamesies.PIDGEOT,
                PokemonNamesies.ABRA,
                AttackNamesies.PSYBEAM,
                1,
                PokemonManipulator.giveBattleEffect(EffectNamesies.PSYCHIC_TERRAIN)
        );

        // Float with Levitate -- no change
        powerModifierTest(
                AttackNamesies.THUNDER,
                1,
                PokemonManipulator.combine(
                        PokemonManipulator.giveBattleEffect(EffectNamesies.ELECTRIC_TERRAIN),
                        PokemonManipulator.giveAttackingAbility(AbilityNamesies.LEVITATE)
                )
        );

        // Float with telekinesis -- no change
        powerModifierTest(
                AttackNamesies.VINE_WHIP,
                1,
                PokemonManipulator.combine(
                        PokemonManipulator.giveBattleEffect(EffectNamesies.GRASSY_TERRAIN),
                        PokemonManipulator.giveAttackingEffect(EffectNamesies.TELEKINESIS)
                )
        );
    }

    private void powerModifierTest(AttackNamesies attackName, double expectedChange, PokemonManipulator manipulator) {
        powerModifierTest(PokemonNamesies.CHARMANDER, attackName, expectedChange, manipulator);
    }

    private void powerModifierTest(PokemonNamesies defendingName, AttackNamesies attackName, double expectedChange, PokemonManipulator manipulator) {
        powerModifierTest(PokemonNamesies.BULBASAUR, defendingName, attackName, expectedChange, manipulator);
    }

    private void powerModifierTest(PokemonNamesies attackingName, PokemonNamesies defendingName, AttackNamesies attackName, double expectedChange, PokemonManipulator manipulator) {
        TestPokemon attacking = new TestPokemon(attackingName);
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
}
