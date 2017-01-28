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
        powerModifierTest(.75, new TestInfo().defending(PokemonNamesies.SQUIRTLE, AbilityNamesies.FILTER).with(AttackNamesies.VINE_WHIP));
        powerModifierTest(.75, new TestInfo().defending(PokemonNamesies.CHANDELURE, AbilityNamesies.PRISM_ARMOR).with(AttackNamesies.SURF));
        powerModifierTest(.75, new TestInfo().defending(PokemonNamesies.DRIFBLIM, AbilityNamesies.SOLID_ROCK).with(AttackNamesies.THUNDERBOLT));

        // Neutral and not very effective moves should not be modified
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.RAICHU, AbilityNamesies.FILTER).with(AttackNamesies.VINE_WHIP));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.BUDEW, AbilityNamesies.SOLID_ROCK).with(AttackNamesies.THUNDER));

        // Should not change modifier when the attacker has mold breaker
        powerModifierTest(1, new TestInfo()
                .defending(PokemonNamesies.SQUIRTLE, AbilityNamesies.FILTER)
                .with(AttackNamesies.VINE_WHIP)
                .attacking(AbilityNamesies.MOLD_BREAKER)
        );

        // Prism Armor is unaffected by mold breaker
        powerModifierTest(.75, new TestInfo()
                .defending(PokemonNamesies.CHANDELURE, AbilityNamesies.PRISM_ARMOR)
                .with(AttackNamesies.SURF)
                .attacking(AbilityNamesies.MOLD_BREAKER)
        );
    }

    @Test
    public void typeEffectiveTest() {
        // Tinted Lens doubles the power when not very effective
        powerModifierTest(2, new TestInfo().defending(PokemonNamesies.BUDEW).attacking(AbilityNamesies.TINTED_LENS).with(AttackNamesies.THUNDER_SHOCK));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.SQUIRTLE).attacking(AbilityNamesies.TINTED_LENS).with(AttackNamesies.VINE_WHIP));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.RAICHU).attacking(AbilityNamesies.TINTED_LENS).with(AttackNamesies.EMBER));

        // Expert Belt increases the power of super effective moves by 20%
        powerModifierTest(1.2, new TestInfo().defending(PokemonNamesies.SQUIRTLE).attacking(ItemNamesies.EXPERT_BELT).with(AttackNamesies.VINE_WHIP));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.SQUIRTLE).attacking(ItemNamesies.EXPERT_BELT).with(AttackNamesies.DARK_PULSE));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.SQUIRTLE).attacking(ItemNamesies.EXPERT_BELT).with(AttackNamesies.SURF));

        // Tanga berry reduces super-effective bug moves
        powerModifierTest(.5, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY).with(AttackNamesies.X_SCISSOR));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY).with(AttackNamesies.CRUNCH));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY).with(AttackNamesies.SURF));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY).with(AttackNamesies.PSYBEAM));

        // Yache berry reduces super-effective ice moves
        powerModifierTest(.5, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY).with(AttackNamesies.ICE_BEAM));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY).with(AttackNamesies.OUTRAGE));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY).with(AttackNamesies.TACKLE));
        powerModifierTest(1, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY).with(AttackNamesies.SURF));
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
        powerModifierTest(1.5, new TestInfo().with(AttackNamesies.SURF).attacking(EffectNamesies.RAINING));
        powerModifierTest(.5, new TestInfo().with(AttackNamesies.FLAMETHROWER).attacking(EffectNamesies.RAINING));
        powerModifierTest(1, new TestInfo().with(AttackNamesies.THUNDERBOLT).attacking(EffectNamesies.RAINING));

        powerModifierTest(1.5, new TestInfo().with(AttackNamesies.FLAMETHROWER).attacking(EffectNamesies.SUNNY));
        powerModifierTest(.5, new TestInfo().with(AttackNamesies.SURF).attacking(EffectNamesies.SUNNY));
        powerModifierTest(1, new TestInfo().with(AttackNamesies.THUNDERBOLT).attacking(EffectNamesies.SUNNY));
    }

    @Test
    public void powerModifierTest() {
        // Adamant Orb boosts dragon and steel type moves but only for Dialga
        powerModifierTest(1.2, new TestInfo().attacking(PokemonNamesies.DIALGA, ItemNamesies.ADAMANT_ORB).with(AttackNamesies.OUTRAGE));
        powerModifierTest(1, new TestInfo().attacking(PokemonNamesies.DIALGA, ItemNamesies.ADAMANT_ORB).with(AttackNamesies.TACKLE));
        powerModifierTest(1, new TestInfo().attacking(PokemonNamesies.DRAGONAIR, ItemNamesies.ADAMANT_ORB).with(AttackNamesies.OUTRAGE));

        // Charcoal
        powerModifierTest(1.2, new TestInfo().attacking(PokemonNamesies.CHARMANDER, ItemNamesies.CHARCOAL).with(AttackNamesies.FLAMETHROWER));
        powerModifierTest(1.2, new TestInfo().attacking(PokemonNamesies.BUDEW, ItemNamesies.CHARCOAL).with(AttackNamesies.FLAMETHROWER));
        powerModifierTest(1, new TestInfo().attacking(PokemonNamesies.CHARIZARD, ItemNamesies.CHARCOAL).with(AttackNamesies.AIR_SLASH));

        // Life orb
        powerModifierTest(5324.0/4096.0, new TestInfo().attacking(ItemNamesies.LIFE_ORB).with(AttackNamesies.AIR_SLASH));
        powerModifierTest(5324.0/4096.0, new TestInfo().attacking(ItemNamesies.LIFE_ORB).with(AttackNamesies.OUTRAGE));

        // Muscle band boosts Physical moves
        powerModifierTest(1.1, new TestInfo().attacking(ItemNamesies.MUSCLE_BAND).with(AttackNamesies.VINE_WHIP));
        powerModifierTest(1, new TestInfo().attacking(ItemNamesies.MUSCLE_BAND).with(AttackNamesies.ENERGY_BALL));
    }

    @Test
    public void terrainTest() {
        // Terrain effects boost/lower the power of certain types of moves
        powerModifierTest(1.5, new TestInfo().with(AttackNamesies.THUNDER).attacking(EffectNamesies.ELECTRIC_TERRAIN));
        powerModifierTest(1.5, new TestInfo().with(AttackNamesies.PSYCHIC).attacking(EffectNamesies.PSYCHIC_TERRAIN));
        powerModifierTest(1.5, new TestInfo().with(AttackNamesies.SOLAR_BEAM).attacking(EffectNamesies.GRASSY_TERRAIN));
        powerModifierTest(.5, new TestInfo().with(AttackNamesies.OUTRAGE).attacking(EffectNamesies.MISTY_TERRAIN));

        // Different move type -- no change
        powerModifierTest(1, new TestInfo().with(AttackNamesies.DAZZLING_GLEAM).attacking(EffectNamesies.MISTY_TERRAIN));

        // Float with Flying -- no change
        powerModifierTest(1, new TestInfo().with(AttackNamesies.PSYBEAM).attacking(PokemonNamesies.PIDGEOT, EffectNamesies.PSYCHIC_TERRAIN));

        // Float with Levitate -- no change
        powerModifierTest(1, new TestInfo().with(AttackNamesies.THUNDER).attacking(AbilityNamesies.LEVITATE).attacking(EffectNamesies.ELECTRIC_TERRAIN));

        // Float with telekinesis -- no change
        powerModifierTest(1, new TestInfo().with(AttackNamesies.VINE_WHIP).attacking(EffectNamesies.TELEKINESIS).attacking(EffectNamesies.GRASSY_TERRAIN));
    }

    private void powerModifierTest(double expectedChange, TestInfo testInfo) {
        TestPokemon attacking = new TestPokemon(testInfo.attackingName);
        TestPokemon defending = new TestPokemon(testInfo.defendingName);

        TestBattle battle = TestBattle.create(attacking, defending);

        attacking.setupMove(testInfo.attackName, battle, defending);
        double beforeModifier = battle.getDamageModifier(attacking, defending);

        testInfo.manipulator.manipulate(battle, attacking, defending);
        double afterModifier = battle.getDamageModifier(attacking, defending);

        Assert.assertTrue(
                StringUtils.spaceSeparated(beforeModifier, afterModifier, expectedChange, testInfo.toString()),
                expectedChange*beforeModifier == afterModifier
        );
    }
}
