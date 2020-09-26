package test.battle;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.effect.Effect;
import battle.effect.battle.StandardBattleEffectNamesies;
import battle.effect.battle.terrain.TerrainNamesies;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.source.ChangeTypeSource;
import battle.effect.team.TeamEffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import pokemon.stat.User;
import test.battle.manipulator.PokemonManipulator;
import test.battle.manipulator.TestAction;
import test.battle.manipulator.TestInfo;
import test.general.BaseTest;
import test.general.TestUtils;
import test.pokemon.TestPokemon;
import type.PokeType;
import type.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

public class ModifierTest extends BaseTest {
    @Test
    public void statChangeTest() {
        // Hustle boosts Attack, but decreases Accuracy
        statModifierTest(1.5, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.HUSTLE));
        statModifierTest(.8, Stat.ACCURACY, new TestInfo().attacking(AbilityNamesies.HUSTLE));
        statModifierTest(1, Stat.SP_ATTACK, new TestInfo().attacking(AbilityNamesies.HUSTLE));

        // Light Screen doubles Sp. Defense
        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(TeamEffectNamesies.LIGHT_SCREEN));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(TeamEffectNamesies.LIGHT_SCREEN));

        // Sandstorm raises Sp. Defense of Rock-type Pokemon (blocked by Air Lock)
        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.LILEEP).defending(WeatherNamesies.SANDSTORM));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.LILEEP).defending(WeatherNamesies.SANDSTORM).attacking(AbilityNamesies.AIR_LOCK));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.MAWILE).defending(WeatherNamesies.SANDSTORM));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.SANDYGAST).defending(WeatherNamesies.SANDSTORM));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.GEODUDE).defending(WeatherNamesies.SANDSTORM));

        // Deep Sea Scale doubles Sp. Defense for Clamperl, Chinchou, and Lanturn (because I say so)
        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.LANTURN, ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.CHINCHOU, ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.CLAMPERL, ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.CLAMPERL, ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.HUNTAIL, ItemNamesies.DEEP_SEA_SCALE));

        // Eviolite boost Defense and Sp. Defense for unevolved Pokemon
        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.DRAGONAIR, ItemNamesies.EVIOLITE));
        statModifierTest(1.5, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.CHANSEY, ItemNamesies.EVIOLITE));
        statModifierTest(1, Stat.SPEED, User.DEFENDING, new TestInfo().defending(PokemonNamesies.CHANSEY, ItemNamesies.EVIOLITE));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.HUNTAIL, ItemNamesies.EVIOLITE));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.RAICHU, ItemNamesies.EVIOLITE));

        // Chlorophyll doubles Speed in Sunny weather
        statModifierTest(2, Stat.SPEED, User.ATTACKING, new TestInfo().attacking(AbilityNamesies.CHLOROPHYLL).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().attacking(AbilityNamesies.CHLOROPHYLL).attacking(WeatherNamesies.SUNNY));

        // Flower Gift boost Attack and Sp. Defense when Sunny
        statModifierTest(1.5, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1.5, Stat.SP_DEFENSE, User.ATTACKING, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1, Stat.SP_DEFENSE, User.DEFENDING, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1, Stat.SPEED, User.ATTACKING, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));

        // Fur Cost doubles Defense
        statModifierTest(2, Stat.DEFENSE, new TestInfo().with(AttackNamesies.TACKLE).defending(AbilityNamesies.FUR_COAT));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().with(AttackNamesies.SURF).defending(AbilityNamesies.FUR_COAT));
    }

    private void statModifierTest(double expectedChange, Stat stat, TestInfo testInfo) {
        testInfo.statModifierTest(expectedChange, stat);
    }

    private void statModifierTest(double expectedChange, Stat stat, User user, TestInfo testInfo) {
        testInfo.statModifierTest(expectedChange, stat, user);
    }

    @Test
    public void filterTest() {
        // Super-effective attack should be reduced by 25%
        powerChangeTest(.75, AttackNamesies.VINE_WHIP, new TestInfo().defending(PokemonNamesies.SQUIRTLE, AbilityNamesies.FILTER));
        powerChangeTest(.75, AttackNamesies.SURF, new TestInfo().defending(PokemonNamesies.CHANDELURE, AbilityNamesies.PRISM_ARMOR));
        powerChangeTest(.75, AttackNamesies.THUNDERBOLT, new TestInfo().defending(PokemonNamesies.DRIFBLIM, AbilityNamesies.SOLID_ROCK));

        // Neutral and not very effective moves should not be modified
        powerChangeTest(1, AttackNamesies.VINE_WHIP, new TestInfo().defending(PokemonNamesies.RAICHU, AbilityNamesies.FILTER));
        powerChangeTest(1, AttackNamesies.THUNDER, new TestInfo().defending(PokemonNamesies.BUDEW, AbilityNamesies.SOLID_ROCK));

        // Should not change modifier when the attacker has mold breaker
        powerChangeTest(1, AttackNamesies.VINE_WHIP, new TestInfo()
                .defending(PokemonNamesies.SQUIRTLE, AbilityNamesies.FILTER)
                .attacking(AbilityNamesies.MOLD_BREAKER)
        );

        // Prism Armor is unaffected by mold breaker
        powerChangeTest(.75, AttackNamesies.SURF, new TestInfo()
                .defending(PokemonNamesies.CHANDELURE, AbilityNamesies.PRISM_ARMOR)
                .attacking(AbilityNamesies.MOLD_BREAKER)
        );
    }

    @Test
    public void typeEffectiveTest() {
        // Tinted Lens doubles the power when not very effective
        powerChangeTest(2, AttackNamesies.THUNDER_SHOCK, new TestInfo().defending(PokemonNamesies.BUDEW).attacking(AbilityNamesies.TINTED_LENS));
        powerChangeTest(2, AttackNamesies.VINE_WHIP, new TestInfo().defending(PokemonNamesies.BUTTERFREE).attacking(AbilityNamesies.TINTED_LENS));
        powerChangeTest(1, AttackNamesies.VINE_WHIP, new TestInfo().defending(PokemonNamesies.SQUIRTLE).attacking(AbilityNamesies.TINTED_LENS));
        powerChangeTest(1, AttackNamesies.EMBER, new TestInfo().defending(PokemonNamesies.RAICHU).attacking(AbilityNamesies.TINTED_LENS));

        // Expert Belt increases the power of super effective moves by 20%
        powerChangeTest(1.2, AttackNamesies.VINE_WHIP, new TestInfo().defending(PokemonNamesies.SQUIRTLE).attacking(ItemNamesies.EXPERT_BELT));
        powerChangeTest(1, AttackNamesies.DARK_PULSE, new TestInfo().defending(PokemonNamesies.SQUIRTLE).attacking(ItemNamesies.EXPERT_BELT));
        powerChangeTest(1, AttackNamesies.SURF, new TestInfo().defending(PokemonNamesies.SQUIRTLE).attacking(ItemNamesies.EXPERT_BELT));

        // Tanga berry reduces super-effective bug moves
        powerChangeTest(.5, AttackNamesies.X_SCISSOR, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY));
        powerChangeTest(.25, AttackNamesies.X_SCISSOR, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY).defending(AbilityNamesies.RIPEN));
        powerChangeTest(1, AttackNamesies.CRUNCH, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY));
        powerChangeTest(1, AttackNamesies.SURF, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY));
        powerChangeTest(1, AttackNamesies.PSYBEAM, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY));

        // Yache berry reduces super-effective ice moves
        powerChangeTest(.5, AttackNamesies.ICE_BEAM, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY));
        powerChangeTest(.25, AttackNamesies.ICE_BEAM, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY).defending(AbilityNamesies.RIPEN));
        powerChangeTest(1, AttackNamesies.OUTRAGE, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY));
        powerChangeTest(1, AttackNamesies.TACKLE, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY));
        powerChangeTest(1, AttackNamesies.SURF, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY));
        powerChangeTest(1, AttackNamesies.ICE_BEAM, new TestInfo().defending(PokemonNamesies.EEVEE, ItemNamesies.YACHE_BERRY));
        powerChangeTest(1, AttackNamesies.ICE_BEAM, new TestInfo().defending(PokemonNamesies.CHARMANDER, ItemNamesies.YACHE_BERRY));
    }

    @Test
    public void weatherTest() {
        // Weather effects boost/lower the power of certain types of moves
        powerChangeTest(1.5, AttackNamesies.SURF, new TestInfo().attacking(WeatherNamesies.RAINING));
        powerChangeTest(.5, AttackNamesies.FLAMETHROWER, new TestInfo().attacking(WeatherNamesies.RAINING));
        powerChangeTest(1, AttackNamesies.THUNDERBOLT, new TestInfo().attacking(WeatherNamesies.RAINING));

        powerChangeTest(1.5, AttackNamesies.FLAMETHROWER, new TestInfo().attacking(WeatherNamesies.SUNNY));
        powerChangeTest(.5, AttackNamesies.SURF, new TestInfo().attacking(WeatherNamesies.SUNNY));
        powerChangeTest(1, AttackNamesies.THUNDERBOLT, new TestInfo().attacking(WeatherNamesies.SUNNY));
    }

    @Test
    public void powerChangeTest() {
        // Adamant Orb boosts dragon and steel type moves but only for Dialga
        powerChangeTest(1.2, AttackNamesies.OUTRAGE, new TestInfo().attacking(PokemonNamesies.DIALGA, ItemNamesies.ADAMANT_ORB));
        powerChangeTest(1.2, AttackNamesies.IRON_HEAD, new TestInfo().attacking(PokemonNamesies.DIALGA, ItemNamesies.ADAMANT_ORB));
        powerChangeTest(1, AttackNamesies.TACKLE, new TestInfo().attacking(PokemonNamesies.DIALGA, ItemNamesies.ADAMANT_ORB));
        powerChangeTest(1, AttackNamesies.OUTRAGE, new TestInfo().attacking(PokemonNamesies.DRAGONAIR, ItemNamesies.ADAMANT_ORB));

        // Charcoal boosts Fire-type moves
        powerChangeTest(1.2, AttackNamesies.FLAMETHROWER, new TestInfo().attacking(PokemonNamesies.CHARMANDER, ItemNamesies.CHARCOAL));
        powerChangeTest(1.2, AttackNamesies.FLAMETHROWER, new TestInfo().attacking(PokemonNamesies.BUDEW, ItemNamesies.CHARCOAL));
        powerChangeTest(1, AttackNamesies.AIR_SLASH, new TestInfo().attacking(PokemonNamesies.CHARIZARD, ItemNamesies.CHARCOAL));

        // Life orb just boosts it all
        powerChangeTest(5324.0/4096.0, AttackNamesies.AIR_SLASH, new TestInfo().attacking(ItemNamesies.LIFE_ORB));
        powerChangeTest(5324.0/4096.0, AttackNamesies.OUTRAGE, new TestInfo().attacking(ItemNamesies.LIFE_ORB));

        // Muscle band boosts Physical moves
        powerChangeTest(1.1, AttackNamesies.VINE_WHIP, new TestInfo().attacking(ItemNamesies.MUSCLE_BAND));
        powerChangeTest(1, AttackNamesies.ENERGY_BALL, new TestInfo().attacking(ItemNamesies.MUSCLE_BAND));

        // Galvanize boosts normal-type moves that have been changed electric type
        powerChangeTest(1.2, AttackNamesies.SWIFT, new TestInfo().attacking(AbilityNamesies.GALVANIZE));
        powerChangeTest(1, AttackNamesies.THUNDERBOLT, new TestInfo().attacking(AbilityNamesies.GALVANIZE));
        powerChangeTest(1, AttackNamesies.VINE_WHIP, new TestInfo().attacking(AbilityNamesies.GALVANIZE));

        // Normalize boosts normal-type moves (regardless of if it was originally normal)
        powerChangeTest(1.2, AttackNamesies.SWIFT, new TestInfo().attacking(AbilityNamesies.NORMALIZE));
        powerChangeTest(1.2, AttackNamesies.THUNDERBOLT, new TestInfo().attacking(AbilityNamesies.NORMALIZE));
        powerChangeTest(1.2, AttackNamesies.VINE_WHIP, new TestInfo().attacking(AbilityNamesies.NORMALIZE));

        // Multiscale halves power when at full health
        powerChangeTest(.5, AttackNamesies.VINE_WHIP, new TestInfo().defending(AbilityNamesies.MULTISCALE));
        powerChangeTest(1, AttackNamesies.VINE_WHIP, new TestInfo().defending(AbilityNamesies.MULTISCALE).with((battle, attacking, defending) -> {
            battle.attackingFight(AttackNamesies.FALSE_SWIPE);
            defending.assertNotFullHealth();
        }));

        // Facade doubles in power when the user has a status condition
        powerChangeTest(1, AttackNamesies.FACADE, new TestInfo());
        powerChangeTest(2, AttackNamesies.FACADE, new TestInfo().defendingFight(AttackNamesies.WILL_O_WISP));
        powerChangeTest(2, AttackNamesies.FACADE, new TestInfo().defendingFight(AttackNamesies.THUNDER_WAVE));
        powerChangeTest(2, AttackNamesies.FACADE, new TestInfo().defendingFight(AttackNamesies.TOXIC).attacking(PokemonNamesies.EEVEE));
        powerChangeTest(2, AttackNamesies.FACADE, new TestInfo().defendingFight(AttackNamesies.POISON_POWDER).attacking(PokemonNamesies.EEVEE));
        powerChangeTest(1, AttackNamesies.FACADE, new TestInfo().defendingFight(AttackNamesies.CONFUSE_RAY));

        // Acrobatics has double power when not holding an item
        powerChangeTest(2, 2, AttackNamesies.ACROBATICS, new TestInfo());
        powerChangeTest(2, 1, AttackNamesies.ACROBATICS, new TestInfo().attacking(ItemNamesies.POTION));

        // Body Slam doubles power (and always hits) if opponent has used Minimize
        powerChangeTest(1, AttackNamesies.BODY_SLAM, new TestInfo());
        powerChangeTest(2, AttackNamesies.BODY_SLAM, new TestInfo().defendingFight(AttackNamesies.MINIMIZE).attackingBypass(true));

        // Tar Shot doubles effectiveness of Fire moves
        powerChangeTest(2, AttackNamesies.EMBER, new TestInfo().attackingFight(AttackNamesies.TAR_SHOT));
        powerChangeTest(1, AttackNamesies.TACKLE, new TestInfo().attackingFight(AttackNamesies.TAR_SHOT));

        // Ice Scales reduces the power of special attacks
        powerChangeTest(1, AttackNamesies.TACKLE, new TestInfo().defending(AbilityNamesies.ICE_SCALES));
        powerChangeTest(.5, AttackNamesies.SWIFT, new TestInfo().defending(AbilityNamesies.ICE_SCALES));
        powerChangeTest(1, AttackNamesies.SWIFT, new TestInfo().attacking(AbilityNamesies.ICE_SCALES));

        // Power Spot just always boost by 30%
        powerChangeTest(1.3, AttackNamesies.TACKLE, new TestInfo().attacking(AbilityNamesies.POWER_SPOT));
        powerChangeTest(1.3, AttackNamesies.SWIFT, new TestInfo().attacking(AbilityNamesies.POWER_SPOT));
        powerChangeTest(1, AttackNamesies.TACKLE, new TestInfo().defending(AbilityNamesies.POWER_SPOT));

        // Punk Rock boosts sound moves by 30%, and reduces by 50% for incoming sound moves
        powerChangeTest(1.3, AttackNamesies.OVERDRIVE, new TestInfo().attacking(AbilityNamesies.PUNK_ROCK));
        powerChangeTest(.5, AttackNamesies.OVERDRIVE, new TestInfo().defending(AbilityNamesies.PUNK_ROCK));
        powerChangeTest(1, AttackNamesies.SWIFT, new TestInfo().attacking(AbilityNamesies.PUNK_ROCK));

        // Steely Spirit boosts Steel-type moves by 50%
        powerChangeTest(1.5, AttackNamesies.FLASH_CANNON, new TestInfo().attacking(AbilityNamesies.STEELY_SPIRIT));
        powerChangeTest(1, AttackNamesies.SWIFT, new TestInfo().attacking(AbilityNamesies.STEELY_SPIRIT));
    }

    @Test
    public void terrainTest() {
        // Terrain effects boost/lower the power of certain types of moves
        powerChangeTest(1.3, AttackNamesies.THUNDER, new TestInfo().attacking(TerrainNamesies.ELECTRIC_TERRAIN));
        powerChangeTest(1.3, AttackNamesies.PSYCHIC, new TestInfo().attacking(TerrainNamesies.PSYCHIC_TERRAIN));
        powerChangeTest(1.3, AttackNamesies.SOLAR_BEAM, new TestInfo().attacking(TerrainNamesies.GRASSY_TERRAIN));
        powerChangeTest(.5, AttackNamesies.OUTRAGE, new TestInfo().attacking(TerrainNamesies.MISTY_TERRAIN));

        // Does not matter if the recipient is levitating
        powerChangeTest(1.3, AttackNamesies.THUNDER, new TestInfo().attacking(TerrainNamesies.ELECTRIC_TERRAIN).defending(PokemonNamesies.DRAGONITE));

        // Different move type -- no change
        powerChangeTest(1, AttackNamesies.DAZZLING_GLEAM, new TestInfo().attacking(TerrainNamesies.MISTY_TERRAIN));

        // Float with Flying type -- no change
        powerChangeTest(1, AttackNamesies.PSYBEAM, new TestInfo().attacking(PokemonNamesies.PIDGEOT, TerrainNamesies.PSYCHIC_TERRAIN));

        // Float with Levitate -- no change
        powerChangeTest(1, AttackNamesies.THUNDER, new TestInfo().attacking(AbilityNamesies.LEVITATE).attacking(TerrainNamesies.ELECTRIC_TERRAIN));

        // Float with telekinesis -- no change
        powerChangeTest(1, AttackNamesies.VINE_WHIP, new TestInfo().attacking(PokemonEffectNamesies.TELEKINESIS).attacking(TerrainNamesies.GRASSY_TERRAIN));

        // Misty Terrain checks the recipient of the attack for groundedness
        // Flying-type Dragonite should still have a reduced power
        // But if the recipient is floating with flying type, then the attack has neutral damage
        powerChangeTest(.5, AttackNamesies.OUTRAGE, new TestInfo().attacking(PokemonNamesies.DRAGONITE, TerrainNamesies.MISTY_TERRAIN));
        powerChangeTest(1, AttackNamesies.OUTRAGE, new TestInfo().attacking(TerrainNamesies.MISTY_TERRAIN).defending(PokemonNamesies.DRAGONITE));
    }

    @Test
    public void terrainBoostTest() {
        // Expanding Force receives an additional 50% boost on top of the 30% psychic move boost
        // The attacking Pokemon must be grounded (in the terrain) to receive the boost
        terrainBoostTest(1.3*1.5, 1, 1.3*1.5, AttackNamesies.EXPANDING_FORCE, TerrainNamesies.PSYCHIC_TERRAIN);

        // Rising Voltage's power is doubled on top of the 30% electric move boost
        // The defending Pokemon must be grounded (in the terrain) to receive the boost
        terrainBoostTest(1.3*2, 2, 1.3, AttackNamesies.RISING_VOLTAGE, TerrainNamesies.ELECTRIC_TERRAIN);

        // Misty Explosion's power is increased by 50% on Misty Terrain
        // Note: No Fairy-type terrain boost on Misty Terrain, decreases Dragon-type power instead
        // The attacking Pokemon must be grounded (in the terrain) to receive the boost
        terrainBoostTest(1.5, 1, 1.5, AttackNamesies.MISTY_EXPLOSION, TerrainNamesies.MISTY_TERRAIN);
    }

    private void terrainBoostTest(double bothGroundedModifier,
                                  double attackingLevitatingModifier,
                                  double defendingLevitatingModifier,
                                  AttackNamesies boostAttack,
                                  TerrainNamesies boostTerrain) {
        // Should only get boost for specified terrain
        for (TerrainNamesies terrainNamesies : TerrainNamesies.values()) {
            double modifier = terrainNamesies == boostTerrain ? bothGroundedModifier : 1;
            powerChangeTest(modifier, boostAttack, new TestInfo().attacking(terrainNamesies));
        }

        // Attacking Pokemon is flying type and should not be affected by terrain
        powerChangeTest(attackingLevitatingModifier, boostAttack, new TestInfo().attacking(PokemonNamesies.DRAGONITE, boostTerrain));

        // Defending Pokemon is flying type and should not be affected by terrain
        powerChangeTest(defendingLevitatingModifier, boostAttack, new TestInfo().attacking(boostTerrain).defending(PokemonNamesies.DRAGONITE));

        // Both Pokemon are flying type and should not be affected by terrain
        powerChangeTest(1, boostAttack, new TestInfo().attacking(PokemonNamesies.DRAGONITE, boostTerrain).defending(PokemonNamesies.DRAGONITE));
    }

    // No modifier without manipulation, expectedModifier with it
    private void powerChangeTest(double expectedModifier, AttackNamesies attackNamesies, TestInfo testInfo) {
        testInfo.powerChangeTest(expectedModifier, attackNamesies);
    }

    private void powerChangeTest(double withoutModifier, double expectedModifier, AttackNamesies attackNamesies, TestInfo testInfo) {
        testInfo.powerChangeTest(withoutModifier, expectedModifier, attackNamesies);
    }

    @Test
    public void simpleContraryTest() {
        // Simple/Contrary applies for changes made by the user or by the opponent
        simpleContraryTest(1, Stat.DEFENSE, User.DEFENDING, new TestAction().defendingFight(AttackNamesies.DEFENSE_CURL));
        simpleContraryTest(-1, Stat.DEFENSE, User.DEFENDING, new TestAction().attackingFight(AttackNamesies.TAIL_WHIP));
        simpleContraryTest(2, Stat.ATTACK, User.ATTACKING, new TestAction().attackingFight(AttackNamesies.SWORDS_DANCE));
        simpleContraryTest(-2, Stat.SP_ATTACK, User.ATTACKING, new TestAction().defendingFight(AttackNamesies.EERIE_IMPULSE));
        simpleContraryTest(3, Stat.DEFENSE, User.DEFENDING, new TestAction().defendingFight(AttackNamesies.COTTON_GUARD));

        // Simple/Contrary even works against self-inflicted negative stat changes
        simpleContraryTest(-1, Stat.SPEED, User.ATTACKING, new TestAction().attackingFight(AttackNamesies.HAMMER_ARM));
        simpleContraryTest(-2, Stat.SP_ATTACK, User.ATTACKING, new TestAction().attackingFight(AttackNamesies.LEAF_STORM));
    }

    private void simpleContraryTest(int expectedStage, Stat stat, User abilityHolder, PokemonManipulator manipulator) {
        Assert.assertNotEquals(User.BOTH, abilityHolder);

        // Simple doubles stat changes, Contrary reverses stat changes
        simpleContraryTest(expectedStage, stat, abilityHolder, AbilityNamesies.NO_ABILITY, manipulator);
        simpleContraryTest(2*expectedStage, stat, abilityHolder, AbilityNamesies.SIMPLE, manipulator);
        simpleContraryTest(-expectedStage, stat, abilityHolder, AbilityNamesies.CONTRARY, manipulator);

        // Simple and Contrary only reflect the receiver, not the user
        User opposite = abilityHolder.isAttacking() ? User.DEFENDING : User.ATTACKING;
        simpleContraryTest(expectedStage, stat, opposite, AbilityNamesies.SIMPLE, manipulator);
        simpleContraryTest(expectedStage, stat, opposite, AbilityNamesies.CONTRARY, manipulator);
    }

    private void simpleContraryTest(int expectedStage, Stat stat, User abilityHolder, AbilityNamesies ability, PokemonManipulator manipulator) {
        TestInfo testInfo = new TestInfo();
        if (abilityHolder.isAttacking()) {
            testInfo.attacking(ability);
        } else {
            testInfo.defending(ability);
        }
        stageChangeTest(expectedStage, stat, testInfo.with(manipulator));
    }

    @Test
    public void stageChangeTest() {
        // Tangled Feet raises Evasion stage when confused
        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.TANGLED_FEET, PokemonEffectNamesies.CONFUSION));
        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.TANGLED_FEET).attackingFight(AttackNamesies.CONFUSE_RAY));

        // Sand Veil raises Evasion in Sandstorm, Snow Cloak raises Evasion in Hail
        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SAND_VEIL, WeatherNamesies.SANDSTORM));
        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SNOW_CLOAK, WeatherNamesies.HAILING));
        stageChangeTest(new TestStages(), new TestInfo().defending(AbilityNamesies.SNOW_CLOAK, WeatherNamesies.SANDSTORM));
        stageChangeTest(new TestStages(), new TestInfo().defending(AbilityNamesies.SAND_VEIL, WeatherNamesies.SUNNY));

        // Gravity sharply decreases Evasion
        stageChangeTest(-2, Stat.EVASION, new TestInfo().attacking(StandardBattleEffectNamesies.GRAVITY));

        // Growth raises Attack and Sp. Attack by 1 stage (each by 2 when Sunny)
        stageChangeTest(
                new TestStages().set(1, Stat.ATTACK, Stat.SP_ATTACK),
                new TestInfo().attackingFight(AttackNamesies.GROWTH)
        );
        stageChangeTest(
                new TestStages().set(2, Stat.ATTACK, Stat.SP_ATTACK),
                new TestInfo().attackingFight(AttackNamesies.SUNNY_DAY).attackingFight(AttackNamesies.GROWTH)
        );

        // Each stack of Stockpile raises Defense and Sp. Defense by 1 stage with max of 3 stacks
        stageChangeTest(
                new TestStages().set(3, Stat.DEFENSE, Stat.SP_DEFENSE),
                new TestInfo().with((battle, attacking, defending) -> {
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    defending.assertHasEffect(PokemonEffectNamesies.STOCKPILE);
                })
        );

        // After using Spit Up (or Swallow) though the effect will be removed and stages will revert
        stageChangeTest(
                new TestStages(),
                new TestInfo().with((battle, attacking, defending) -> {
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.fight(AttackNamesies.ENDURE, AttackNamesies.SPIT_UP);
                    defending.assertNoEffect(PokemonEffectNamesies.STOCKPILE);
                })
        );

        // Once removed though, you can just add it back again (in this case 2 stacks)
        stageChangeTest(
                new TestStages().set(2, Stat.DEFENSE, Stat.SP_DEFENSE),
                new TestInfo().with((battle, attacking, defending) -> {
                    battle.fight(AttackNamesies.FALSE_SWIPE, AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    defending.assertHasEffect(PokemonEffectNamesies.STOCKPILE);
                    defending.assertNotFullHealth();
                    battle.defendingFight(AttackNamesies.SWALLOW);
                    defending.assertFullHealth();
                    defending.assertNoEffect(PokemonEffectNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    defending.assertHasEffect(PokemonEffectNamesies.STOCKPILE);
                })
        );
    }

    private void stageChangeTest(int expectedStage, Stat stat, TestInfo testInfo) {
        stageChangeTest(new TestStages().set(expectedStage, stat), testInfo);
    }

    private void stageChangeTest(TestStages expectedStages, TestInfo testInfo) {
        testInfo.stageChangeTest(expectedStages);
    }

    @Test
    public void ignoreStageTest() {
        // Double team increases defending evasion by 1 stage
        // Foresight effect though will ignore this stage
        ignoreStageTest(
                new IgnoreStages(new TestStages().set(1, Stat.EVASION), Stat.EVASION),
                new TestInfo().defendingFight(AttackNamesies.DOUBLE_TEAM),
                (battle, attacking, defending) -> battle.attackingFight(AttackNamesies.FORESIGHT)
        );

        // Sweet Scent decreases defending evasion by 2 stages
        // Foresight effect only cancels positive evasion so it should be the same
        ignoreStageTest(
                new IgnoreStages(new TestStages().set(-2, Stat.EVASION)),
                new TestInfo().attackingFight(AttackNamesies.SWEET_SCENT),
                (battle, attacking, defending) -> battle.attackingFight(AttackNamesies.FORESIGHT)
        );

        // Foresight only should ignore evasion
        ignoreStageTest(
                (battle, attacking, defending) -> battle.attackingFight(AttackNamesies.FORESIGHT),
                Stat.EVASION
        );

        // Unaware on the attacking Pokemon ignores defending stages
        ignoreStageTest(
                (battle, attacking, defending) -> attacking.withAbility(AbilityNamesies.UNAWARE),
                Stat.EVASION, Stat.DEFENSE, Stat.SP_DEFENSE
        );

        // Unaware on the defending Pokemon ignores attacking stages
        ignoreStageTest(
                (battle, attacking, defending) -> defending.withAbility(AbilityNamesies.UNAWARE),
                Stat.ACCURACY, Stat.ATTACK, Stat.SP_ATTACK
        );

        // Keen Eye on the attacking Pokemon ignores evasion
        ignoreStageTest((battle, attacking, defending) -> attacking.withAbility(AbilityNamesies.KEEN_EYE), Stat.EVASION);

        // Keen Eye on the defending Pokemon does nothing
        ignoreStageTest((battle, attacking, defending) -> defending.withAbility(AbilityNamesies.KEEN_EYE));

        // Chip Away ignores defending stages
        ignoreStageTest(
                (battle, attacking, defending) -> attacking.setupMove(AttackNamesies.CHIP_AWAY, battle),
                Stat.EVASION, Stat.DEFENSE, Stat.SP_DEFENSE
        );
    }

    private void ignoreStageTest(PokemonManipulator ignoreMe, Stat... ignoreStats) {
        // Sets up every single stat change (all positive) but different values on the attacking and defending
        PokemonManipulator setupManipulator = (battle, attacking, defending) -> {
            battle.attackingFight(AttackNamesies.QUIVER_DANCE);
            battle.attackingFight(AttackNamesies.QUIVER_DANCE);
            battle.attackingFight(AttackNamesies.QUIVER_DANCE);
            battle.attackingFight(AttackNamesies.DRAGON_DANCE);
            battle.attackingFight(AttackNamesies.DRAGON_DANCE);
            battle.attackingFight(AttackNamesies.COIL);
            battle.attackingFight(AttackNamesies.COIL);
            battle.attackingFight(AttackNamesies.MINIMIZE);

            battle.defendingFight(AttackNamesies.QUIVER_DANCE);
            battle.defendingFight(AttackNamesies.DOUBLE_TEAM);
            battle.defendingFight(AttackNamesies.HONE_CLAWS);
            battle.defendingFight(AttackNamesies.HARDEN);

            attacking.assertStages(new TestStages().set(2, Stat.ACCURACY, Stat.DEFENSE, Stat.EVASION)
                                                   .set(3, Stat.SP_ATTACK, Stat.SP_DEFENSE)
                                                   .set(4, Stat.ATTACK)
                                                   .set(5, Stat.SPEED));
            defending.assertStages(new TestStages().set(1, Stat.ATTACK, Stat.DEFENSE, Stat.SP_ATTACK,
                                                        Stat.SP_DEFENSE, Stat.SPEED, Stat.ACCURACY, Stat.EVASION));
        };

        TestStages setupStages = new TestStages().set(1, Stat.DEFENSE, Stat.SP_DEFENSE, Stat.EVASION)
                                                 .set(2, Stat.ACCURACY)
                                                 .set(3, Stat.SP_ATTACK)
                                                 .set(4, Stat.ATTACK)
                                                 .set(5, Stat.SPEED);

        ignoreStageTest(new IgnoreStages(setupStages, ignoreStats), new TestInfo().with(setupManipulator), ignoreMe);
    }

    private void ignoreStageTest(IgnoreStages ignoreStages, TestInfo testInfo, PokemonManipulator ignoreMe) {
        stageChangeTest(ignoreStages.withoutIgnore, testInfo);
        stageChangeTest(ignoreStages.ignoreStages, testInfo.with(ignoreMe));
    }

    // The stages here will not all be for the same Pokemon
    // Attack, Sp. Attack, Accuracy, and Speed will look at the attacking Pokemon stages
    // Defense, Sp. Defense, and Evasion will look at the defending Pokemon stages
    private static class IgnoreStages {
        TestStages withoutIgnore;
        TestStages ignoreStages;

        public IgnoreStages(TestStages withoutIgnore, Stat... shouldIgnore) {
            this.withoutIgnore = withoutIgnore;
            this.ignoreStages = new TestStages();

            EnumSet<Stat> ignoreStats = EnumSet.noneOf(Stat.class);
            Collections.addAll(ignoreStats, shouldIgnore);
            for (Stat stat : Stat.BATTLE_STATS) {
                // Ignore stats are set to zero be default, all others are set to the same as withoutIgnore
                if (!ignoreStats.contains(stat)) {
                    this.ignoreStages.set(withoutIgnore.get(stat), stat);
                }
            }
        }
    }

    @Test
    public void criticalHitStageTest() {
        // Negative defense is unaffected by crits
        criticalHitStageTest(
                new CritStageMap().stage(-1, Stat.DEFENSE),
                new TestInfo().attackingFight(AttackNamesies.TAIL_WHIP)
        );

        // Positive defense is negated by crits
        criticalHitStageTest(
                new CritStageMap().stage(1, 0, Stat.DEFENSE),
                new TestInfo().defendingFight(AttackNamesies.HARDEN)
        );

        // Negative special defense is unaffected by crits
        criticalHitStageTest(
                new CritStageMap().stage(-2, Stat.SP_DEFENSE),
                new TestInfo().attackingFight(AttackNamesies.FAKE_TEARS)
        );

        // Positive special defense is negated by crits
        criticalHitStageTest(
                new CritStageMap().stage(1, 0, Stat.SP_DEFENSE),
                new TestInfo().defendingFight(AttackNamesies.AROMATIC_MIST)
        );

        // Positive attack is unaffected by crits
        criticalHitStageTest(
                new CritStageMap().stage(2, Stat.ATTACK),
                new TestInfo().attackingFight(AttackNamesies.SWORDS_DANCE)
        );

        // Negative attack is negated by crits
        criticalHitStageTest(
                new CritStageMap().stage(-1, 0, Stat.ATTACK),
                new TestInfo().defendingFight(AttackNamesies.GROWL)
        );

        // Positive special attack is unaffected by crits
        criticalHitStageTest(
                new CritStageMap().stage(2, Stat.SP_ATTACK),
                new TestInfo().attackingFight(AttackNamesies.NASTY_PLOT)
        );

        // Negative special attack is negated by crits
        criticalHitStageTest(
                new CritStageMap().stage(-1, 0, Stat.SP_ATTACK, Stat.ATTACK),
                new TestInfo().defendingFight(AttackNamesies.TEARFUL_LOOK)
        );

        // Negates positive defense even by effects
        criticalHitStageTest(
                new CritStageMap().stage(1, 0, Stat.DEFENSE, Stat.SP_DEFENSE),
                new TestInfo().defendingFight(AttackNamesies.STOCKPILE)
        );

        // Negation for positive defense stacks with stages and effects
        criticalHitStageTest(
                new CritStageMap().stage(2, 0, Stat.DEFENSE).stage(1, 0, Stat.SP_DEFENSE),
                new TestInfo().defendingFight(AttackNamesies.STOCKPILE).defendingFight(AttackNamesies.HARDEN)
        );

        // Only positive changes are negated (so even if total is neutral, the negative stage persists)
        criticalHitStageTest(
                new CritStageMap().stage(0, -1, Stat.DEFENSE).stage(1, 0, Stat.SP_DEFENSE),
                new TestInfo().defendingFight(AttackNamesies.STOCKPILE).attackingFight(AttackNamesies.TAIL_WHIP)
        );

        // Critical hits ignore barrier effects like Reflect
        criticalHitStageTest(
                new CritStageMap().modifier(2, 1, Stat.DEFENSE),
                new TestInfo().defendingFight(AttackNamesies.REFLECT)
        );
    }

    private void criticalHitStageTest(CritStageMap stageMap, TestInfo testInfo) {
        criticalHitStageTest(AttackNamesies.TACKLE, stageMap.withoutCrit, testInfo);
        criticalHitStageTest(AttackNamesies.SWIFT, stageMap.withoutCrit, testInfo);
        criticalHitStageTest(AttackNamesies.STORM_THROW, stageMap.withCrit, testInfo);
        criticalHitStageTest(AttackNamesies.FROST_BREATH, stageMap.withCrit, testInfo);

        // Lucky Chant prevents crits
        criticalHitStageTest(AttackNamesies.FROST_BREATH, stageMap.withoutCrit, testInfo.defendingFight(AttackNamesies.LUCKY_CHANT));
    }

    private void criticalHitStageTest(AttackNamesies attackNamesies, ModifierStages modifierStages, TestInfo testInfo) {
        // Set up the attack and calculate the damage
        // Does not execute the move but will set if it's a critical hit or not
        testInfo.with(attackNamesies);
        testInfo.with(Battle::calculateDamage);

        testInfo.stageChangeTest(modifierStages.stages);

        // Check each battle stat and make sure the stage and modifier is as expected
        for (Stat stat : Stat.BATTLE_STATS) {
            int stage = modifierStages.stages.get(stat);
            double modifier = modifierStages.getModifier(stat);

            // Test isn't actually relevant for these stats so just make sure these weren't set and ignore
            if (stat == Stat.SPEED || stat == Stat.ACCURACY || stat == Stat.EVASION) {
                Assert.assertEquals(stat.name(), 0, stage);
                TestUtils.assertEquals(stat.name(), 1, modifier);
                continue;
            }

            testInfo.statModifierTest(modifier, stat);
        }
    }

    private static class ModifierStages {
        private TestStages stages;
        private double[] modifiers;

        public ModifierStages() {
            this.stages = new TestStages();
            this.modifiers = new double[Stat.NUM_BATTLE_STATS];
            Arrays.fill(modifiers, 1);
        }

        // Sets the expected stage for each stat
        public void stage(int stage, Stat... stats) {
            this.stages.set(stage, stats);
        }

        // Sets the expected modifier for each stat
        // Note: Does not include stage adjustments
        public void modifier(double modifier, Stat... stats) {
            for (Stat stat : stats) {
                TestUtils.assertEquals(stat.getName(), 1, modifiers[stat.index()]);
                modifiers[stat.index()] = modifier;
            }
        }

        // Returns the set modifier multiplied by the stage modifier (since that's how the calculation actually works)
        public double getModifier(Stat stat) {
            return modifiers[stat.index()]*stat.getStageStatModifier(stages.get(stat));
        }
    }

    // Refers to stat stages (not crit stages) when attacker lands a critical hit
    private static class CritStageMap {
        private ModifierStages withoutCrit;
        private ModifierStages withCrit;

        public CritStageMap() {
            this.withoutCrit = new ModifierStages();
            this.withCrit = new ModifierStages();
        }

        public CritStageMap stage(int always, Stat... stats) {
            return this.stage(always, always, stats);
        }

        public CritStageMap stage(int withoutCrit, int withCrit, Stat... stats) {
            this.withoutCrit.stage(withoutCrit, stats);
            this.withCrit.stage(withCrit, stats);
            return this;
        }

        public CritStageMap modifier(int always, Stat... stats) {
            return this.modifier(always, always, stats);
        }

        public CritStageMap modifier(int withoutCrit, int withCrit, Stat... stats) {
            this.withoutCrit.modifier(withoutCrit, stats);
            this.withCrit.modifier(withCrit, stats);
            return this;
        }
    }

    @Test
    public void absorbTypeTest() {
        stageChangeTest(1, Stat.SP_ATTACK, new TestInfo()
                .attacking(AbilityNamesies.LIGHTNING_ROD)
                .defendingFight(AttackNamesies.THUNDER_PUNCH));

        stageChangeTest(0, Stat.SP_ATTACK, new TestInfo()
                .defending(AbilityNamesies.LIGHTNING_ROD)
                .defendingFight(AttackNamesies.THUNDER_PUNCH));

        stageChangeTest(0, Stat.SP_ATTACK, new TestInfo()
                .attacking(AbilityNamesies.LIGHTNING_ROD)
                .defendingFight(AttackNamesies.TACKLE));
    }

    @Test
    public void priorityChangeTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // No effects -- just default priorities
        checkPriority(0, battle, AttackNamesies.TACKLE);
        checkPriority(1, battle, AttackNamesies.QUICK_ATTACK);
        checkPriority(1, battle, AttackNamesies.BABY_DOLL_EYES);
        checkPriority(0, battle, AttackNamesies.NASTY_PLOT);
        checkPriority(0, battle, AttackNamesies.PECK);
        checkPriority(0, battle, AttackNamesies.RECOVER);
        checkPriority(0, battle, AttackNamesies.ABSORB);
        checkPriority(0, battle, AttackNamesies.GRASSY_GLIDE);

        // Prankster increases priority of status moves
        attacking.withAbility(AbilityNamesies.PRANKSTER);
        checkPriority(0, battle, AttackNamesies.TACKLE);
        checkPriority(1, battle, AttackNamesies.QUICK_ATTACK);
        checkPriority(2, battle, AttackNamesies.BABY_DOLL_EYES);
        checkPriority(1, battle, AttackNamesies.NASTY_PLOT);
        checkPriority(1, battle, AttackNamesies.THUNDER_WAVE);
        checkPriority(1, battle, AttackNamesies.NATURE_POWER);

        // Unless the opponent is dark type
        defending.assertNotType(battle, Type.DARK);
        defending.setCastSource((ChangeTypeSource)(b, caster, victim) -> new PokeType(Type.DARK));
        defending.assertNotType(battle, Type.DARK);
        Effect.cast(PokemonEffectNamesies.CHANGE_TYPE, battle, defending, defending, CastSource.CAST_SOURCE, false);
        defending.assertType(battle, Type.DARK);
        checkPriority(0, battle, AttackNamesies.NASTY_PLOT);
        checkPriority(0, battle, AttackNamesies.THUNDER_WAVE);
        checkPriority(1, battle, AttackNamesies.BABY_DOLL_EYES);

        // Gale Wings increases the priority of Flying type moves
        attacking.withAbility(AbilityNamesies.GALE_WINGS);
        checkPriority(0, battle, AttackNamesies.TACKLE);
        checkPriority(1, battle, AttackNamesies.QUICK_ATTACK);
        checkPriority(0, battle, AttackNamesies.NASTY_PLOT);
        checkPriority(1, battle, AttackNamesies.PECK);
        checkPriority(0, battle, AttackNamesies.JUDGEMENT);

        // Even though this will be a Flying-type move, it is not decided at priority check time
        attacking.giveItem(ItemNamesies.SKY_PLATE);
        checkPriority(0, battle, AttackNamesies.JUDGEMENT);

        // Gale Wings only works when the user has full health
        battle.attackingFight(AttackNamesies.BELLY_DRUM);
        attacking.assertNotFullHealth();
        checkPriority(0, battle, AttackNamesies.PECK);

        // Triage increases the priority of healing moves by 3
        attacking.withAbility(AbilityNamesies.TRIAGE);
        checkPriority(0, battle, AttackNamesies.TACKLE);
        checkPriority(1, battle, AttackNamesies.QUICK_ATTACK);
        checkPriority(0, battle, AttackNamesies.NASTY_PLOT);
        checkPriority(3, battle, AttackNamesies.RECOVER);
        checkPriority(3, battle, AttackNamesies.ABSORB);
        checkPriority(3, battle, AttackNamesies.LUNAR_DANCE);
        checkPriority(3, battle, AttackNamesies.PURIFY);
        checkPriority(3, battle, AttackNamesies.WISH);

        // Does not count as healing moves:
        checkPriority(0, battle, AttackNamesies.AQUA_RING);
        checkPriority(0, battle, AttackNamesies.GRASSY_TERRAIN);
        checkPriority(0, battle, AttackNamesies.INGRAIN);
        checkPriority(0, battle, AttackNamesies.LEECH_SEED);
        checkPriority(0, battle, AttackNamesies.PAIN_SPLIT);
        checkPriority(0, battle, AttackNamesies.PRESENT);

        // Grassy Glide has increased priority with grassy terrain
        checkPriority(0, battle, AttackNamesies.GRASSY_GLIDE);
        battle.attackingFight(AttackNamesies.GRASSY_TERRAIN);
        checkPriority(1, battle, AttackNamesies.GRASSY_GLIDE);
    }

    private void checkPriority(int expected, TestBattle battle, AttackNamesies attack) {
        TestPokemon attacking = battle.getAttacking();

        attacking.setupMove(attack, battle);
        Assert.assertEquals(expected, attacking.getAttackPriority());

        boolean playerFirst = battle.speedPriority();
        if (expected > 0) {
            Assert.assertTrue(playerFirst);
        } else if (expected < 0) {
            Assert.assertFalse(playerFirst);
        }
    }

    @Test
    public void critStageTest() {
        checkCritStage(0, new TestInfo().with(AttackNamesies.TACKLE));

        // +1 crit stage when using -- but no effect when used previously
        checkCritStage(1, new TestInfo().with(AttackNamesies.RAZOR_LEAF));
        checkCritStage(0, new TestInfo().attackingFight(AttackNamesies.RAZOR_LEAF).with(AttackNamesies.TACKLE));

        // +2 crit stage when used, but not when using (I guess it's a status move so it technically doesn't have a stage but whatever)
        checkCritStage(2, new TestInfo().attackingFight(AttackNamesies.FOCUS_ENERGY).with(AttackNamesies.TACKLE));
        checkCritStage(0, new TestInfo().with(AttackNamesies.FOCUS_ENERGY));

        // +2 after using Dire Hit (can only use once -- should fail if used again)
        checkCritStage(2, new TestInfo().useItem(ItemNamesies.DIRE_HIT));
        checkCritStage(2, new TestInfo().useItem(ItemNamesies.DIRE_HIT, true, true)
                                        .useItem(ItemNamesies.DIRE_HIT, true, false)
        );

        // +2 from Lansat Berry when health is below 1/4
        checkCritStage(0, new TestInfo().attacking(ItemNamesies.LANSAT_BERRY));
        checkCritStage(0, new TestInfo()
                .attacking(ItemNamesies.LANSAT_BERRY)
                .with((battle, attacking, defending) -> {
                    // Not enough
                    battle.attackingFight(AttackNamesies.BELLY_DRUM);
                    attacking.assertHealthRatio(.5);
                })
        );
        PokemonManipulator lansatBerry = (battle, attacking, defending) -> {
            attacking.withItem(ItemNamesies.LANSAT_BERRY);
            attacking.assertNotConsumedItem();

            // If the Pokemon already has an increased crit ratio, Lansat Berry cannot further increase and should not be consumed
            boolean hasCrits = attacking.hasEffect(PokemonEffectNamesies.RAISE_CRITS);

            battle.falseSwipePalooza(false);
            if (hasCrits) {
                attacking.assertNotConsumedItem();
                attacking.assertHoldingItem(ItemNamesies.LANSAT_BERRY);
            } else {
                attacking.assertConsumedBerry();
            }

            attacking.hasEffect(PokemonEffectNamesies.RAISE_CRITS);
        };
        checkCritStage(2, new TestInfo().with(lansatBerry));

        // Razor Claw and Scope Lens increase by 1
        checkCritStage(1, new TestInfo().attacking(ItemNamesies.RAZOR_CLAW));
        checkCritStage(1, new TestInfo().attacking(ItemNamesies.SCOPE_LENS));

        // Lucky Punch increases by 2 but only for Chansey (Night Slash is also +1 when using)
        checkCritStage(1, new TestInfo().attacking(PokemonNamesies.CHANSEY).with(AttackNamesies.NIGHT_SLASH));
        checkCritStage(3, new TestInfo().attacking(PokemonNamesies.CHANSEY, ItemNamesies.LUCKY_PUNCH).with(AttackNamesies.NIGHT_SLASH));
        checkCritStage(1, new TestInfo().attacking(PokemonNamesies.FARFETCHD, ItemNamesies.LUCKY_PUNCH).with(AttackNamesies.NIGHT_SLASH));

        // Stick increases by 2 but only for Farfetch'd
        checkCritStage(2, new TestInfo().attacking(PokemonNamesies.FARFETCHD, ItemNamesies.STICK));
        checkCritStage(0, new TestInfo().attacking(PokemonNamesies.CHANSEY, ItemNamesies.STICK));

        // Super Luck increases by 1 (unaffected by Mold Breaker)
        checkCritStage(1, new TestInfo().attacking(AbilityNamesies.SUPER_LUCK));
        checkCritStage(1, new TestInfo().attacking(AbilityNamesies.SUPER_LUCK).defending(AbilityNamesies.MOLD_BREAKER));

        // Focus Energy/Dire Hit/Lansat Berry do not stack with each other
        checkCritStage(2, new TestInfo()
                .attackingFight(AttackNamesies.FOCUS_ENERGY)
                .useItem(ItemNamesies.DIRE_HIT, true, false)
        );
        checkCritStage(2, new TestInfo()
                .useItem(ItemNamesies.DIRE_HIT, true, true)
                .attackingFight(AttackNamesies.FOCUS_ENERGY)
        );
        checkCritStage(2, new TestInfo()
                .attackingFight(AttackNamesies.FOCUS_ENERGY)
                .with(lansatBerry)
        );
        checkCritStage(2, new TestInfo()
                .with(lansatBerry)
                .attackingFight(AttackNamesies.FOCUS_ENERGY)
        );
        checkCritStage(2, new TestInfo()
                .useItem(ItemNamesies.DIRE_HIT, true, true)
                .with(lansatBerry)
        );
        checkCritStage(2, new TestInfo()
                .with(lansatBerry)
                .useItem(ItemNamesies.DIRE_HIT, true, false)
        );

        // All other effects stack together though -- attack, ability, held item, additional effects (focus energy etc)
        checkCritStage(4, new TestInfo()
                .attacking(PokemonNamesies.CHANSEY, ItemNamesies.LUCKY_PUNCH)
                .attacking(AbilityNamesies.SUPER_LUCK)
                .with(AttackNamesies.NIGHT_SLASH)
        );
        checkCritStage(6, new TestInfo()
                .attacking(PokemonNamesies.FARFETCHD, ItemNamesies.STICK)
                .attacking(AbilityNamesies.SUPER_LUCK)
                .attackingFight(AttackNamesies.FOCUS_ENERGY)
                .with(AttackNamesies.RAZOR_LEAF)
        );
    }

    private void checkCritStage(int expectedStage, TestInfo testInfo) {
        testInfo.checkCritStage(expectedStage);
    }
}
