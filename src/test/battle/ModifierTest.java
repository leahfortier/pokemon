package test.battle;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
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
        statModifierTest(1.5, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.HUSTLE));
        statModifierTest(.8, Stat.ACCURACY, new TestInfo().attacking(AbilityNamesies.HUSTLE));
        statModifierTest(1, Stat.SP_ATTACK, new TestInfo().attacking(AbilityNamesies.HUSTLE));

        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(TeamEffectNamesies.LIGHT_SCREEN));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(TeamEffectNamesies.LIGHT_SCREEN));

        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.LILEEP).defending(WeatherNamesies.SANDSTORM));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.MAWILE).defending(WeatherNamesies.SANDSTORM));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.SANDYGAST).defending(WeatherNamesies.SANDSTORM));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.GEODUDE).defending(WeatherNamesies.SANDSTORM));

        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.LANTURN, ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.CHINCHOU, ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.CLAMPERL, ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.CLAMPERL, ItemNamesies.DEEP_SEA_SCALE));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.HUNTAIL, ItemNamesies.DEEP_SEA_SCALE));

        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.DRAGONAIR, ItemNamesies.EVIOLITE));
        statModifierTest(1.5, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.CHANSEY, ItemNamesies.EVIOLITE));
        statModifierTest(1, Stat.SPEED, User.DEFENDING, new TestInfo().defending(PokemonNamesies.CHANSEY, ItemNamesies.EVIOLITE));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.HUNTAIL, ItemNamesies.EVIOLITE));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.RAICHU, ItemNamesies.EVIOLITE));

        statModifierTest(2, Stat.SPEED, User.ATTACKING, new TestInfo().attacking(AbilityNamesies.CHLOROPHYLL).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().attacking(AbilityNamesies.CHLOROPHYLL).attacking(WeatherNamesies.SUNNY));

        statModifierTest(1.5, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1.5, Stat.SP_DEFENSE, User.ATTACKING, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1, Stat.SP_DEFENSE, User.DEFENDING, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1, Stat.SPEED, User.ATTACKING, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));

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
        powerChangeTest(1, AttackNamesies.CRUNCH, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY));
        powerChangeTest(1, AttackNamesies.SURF, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY));
        powerChangeTest(1, AttackNamesies.PSYBEAM, new TestInfo().defending(PokemonNamesies.KADABRA, ItemNamesies.TANGA_BERRY));

        // Yache berry reduces super-effective ice moves
        powerChangeTest(.5, AttackNamesies.ICE_BEAM, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY));
        powerChangeTest(1, AttackNamesies.OUTRAGE, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY));
        powerChangeTest(1, AttackNamesies.TACKLE, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY));
        powerChangeTest(1, AttackNamesies.SURF, new TestInfo().defending(PokemonNamesies.DRAGONITE, ItemNamesies.YACHE_BERRY));
        powerChangeTest(1, AttackNamesies.ICE_BEAM, new TestInfo().defending(PokemonNamesies.EEVEE, ItemNamesies.YACHE_BERRY));
        powerChangeTest(1, AttackNamesies.ICE_BEAM, new TestInfo().defending(PokemonNamesies.CHARMANDER, ItemNamesies.YACHE_BERRY));
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

        // Charcoal
        powerChangeTest(1.2, AttackNamesies.FLAMETHROWER, new TestInfo().attacking(PokemonNamesies.CHARMANDER, ItemNamesies.CHARCOAL));
        powerChangeTest(1.2, AttackNamesies.FLAMETHROWER, new TestInfo().attacking(PokemonNamesies.BUDEW, ItemNamesies.CHARCOAL));
        powerChangeTest(1, AttackNamesies.AIR_SLASH, new TestInfo().attacking(PokemonNamesies.CHARIZARD, ItemNamesies.CHARCOAL));

        // Life orb
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

        // Body Slam -- doubles when the opponent uses Minimize
        powerChangeTest(1, AttackNamesies.BODY_SLAM, new TestInfo());
        powerChangeTest(2, AttackNamesies.BODY_SLAM, new TestInfo().defendingFight(AttackNamesies.MINIMIZE));

        // Tar Shot doubles effectiveness of Fire moves
        powerChangeTest(2, AttackNamesies.EMBER, new TestInfo().attackingFight(AttackNamesies.TAR_SHOT));
        powerChangeTest(1, AttackNamesies.TACKLE, new TestInfo().attackingFight(AttackNamesies.TAR_SHOT));
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

    // No modifier without manipulation, expectedModifier with it
    private void powerChangeTest(double expectedModifier, AttackNamesies attackNamesies, TestInfo testInfo) {
        testInfo.powerChangeTest(expectedModifier, attackNamesies);
    }

    private void powerChangeTest(double withoutModifier, double expectedModifier, AttackNamesies attackNamesies, TestInfo testInfo) {
        testInfo.powerChangeTest(withoutModifier, expectedModifier, attackNamesies);
    }

    @Test
    public void stageChangeTest() {
        stageChangeTest(1, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.NO_ABILITY).defendingFight(AttackNamesies.DEFENSE_CURL));
        stageChangeTest(2, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.SIMPLE).defendingFight(AttackNamesies.DEFENSE_CURL));
        stageChangeTest(-1, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.CONTRARY).defendingFight(AttackNamesies.DEFENSE_CURL));

        stageChangeTest(-1, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.NO_ABILITY).attackingFight(AttackNamesies.TAIL_WHIP));
        stageChangeTest(-2, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.SIMPLE).attackingFight(AttackNamesies.TAIL_WHIP));
        stageChangeTest(1, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.CONTRARY).attackingFight(AttackNamesies.TAIL_WHIP));

        // Contrary even works against self-inflicted negative stat changes
        stageChangeTest(-1, Stat.SPEED, new TestInfo().attacking(AbilityNamesies.NO_ABILITY).attackingFight(AttackNamesies.HAMMER_ARM));
        stageChangeTest(-2, Stat.SPEED, new TestInfo().attacking(AbilityNamesies.SIMPLE).attackingFight(AttackNamesies.HAMMER_ARM));
        stageChangeTest(1, Stat.SPEED, new TestInfo().attacking(AbilityNamesies.CONTRARY).attackingFight(AttackNamesies.HAMMER_ARM));

        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.TANGLED_FEET, PokemonEffectNamesies.CONFUSION));
        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.TANGLED_FEET).attackingFight(AttackNamesies.CONFUSE_RAY));

        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SAND_VEIL, WeatherNamesies.SANDSTORM));
        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SNOW_CLOAK, WeatherNamesies.HAILING));
        stageChangeTest(new TestStages(), new TestInfo().defending(AbilityNamesies.SNOW_CLOAK, WeatherNamesies.SANDSTORM));
        stageChangeTest(new TestStages(), new TestInfo().defending(AbilityNamesies.SAND_VEIL, WeatherNamesies.SUNNY));

        stageChangeTest(-2, Stat.EVASION, new TestInfo().attacking(StandardBattleEffectNamesies.GRAVITY));

        stageChangeTest(-2, Stat.DEFENSE, new TestInfo().attackingFight(AttackNamesies.SCREECH));
        stageChangeTest(-4, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.SIMPLE).attackingFight(AttackNamesies.SCREECH));

        stageChangeTest(2, Stat.ATTACK, new TestInfo().attackingFight(AttackNamesies.SWORDS_DANCE));
        stageChangeTest(4, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.SIMPLE).attackingFight(AttackNamesies.SWORDS_DANCE));

        stageChangeTest(
                new TestStages().set(1, Stat.ATTACK, Stat.SP_ATTACK),
                new TestInfo().attackingFight(AttackNamesies.GROWTH)
        );
        stageChangeTest(
                new TestStages().set(2, Stat.ATTACK, Stat.SP_ATTACK),
                new TestInfo().attackingFight(AttackNamesies.SUNNY_DAY).attackingFight(AttackNamesies.GROWTH)
        );

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

        stageChangeTest(
                new TestStages().set(2, Stat.DEFENSE, Stat.SP_DEFENSE),
                new TestInfo().with((battle, attacking, defending) -> {
                    battle.fight(AttackNamesies.SWIFT, AttackNamesies.STOCKPILE);
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
    }

    private void checkPriority(int expected, TestBattle battle, AttackNamesies attack) {
        TestPokemon attacking = battle.getAttacking();

        attacking.setMove(new Move(attack));
        Assert.assertEquals(expected, battle.getAttackPriority(attacking));

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
        checkCritStage(2, new TestInfo().with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT)));
        checkCritStage(2, new TestInfo()
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT, true, true))
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT, true, false))
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
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT, true, false))
        );
        checkCritStage(2, new TestInfo()
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT, true, true))
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
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT, true, true))
                .with(lansatBerry)
        );
        checkCritStage(2, new TestInfo()
                .with(lansatBerry)
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT, true, false))
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
