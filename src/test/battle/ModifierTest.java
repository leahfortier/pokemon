package test.battle;

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
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import test.BaseTest;
import test.TestPokemon;
import test.TestUtils;
import type.PokeType;
import type.Type;
import util.string.StringUtils;

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

        statModifierTest(2, Stat.SPEED, new TestInfo().attacking(AbilityNamesies.CHLOROPHYLL).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().attacking(AbilityNamesies.CHLOROPHYLL).attacking(WeatherNamesies.SUNNY));

        statModifierTest(1.5, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1, 1.5, Stat.SP_DEFENSE, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));
        statModifierTest(1, Stat.SPEED, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(WeatherNamesies.SUNNY));

        statModifierTest(2, Stat.DEFENSE, new TestInfo().with(AttackNamesies.TACKLE).defending(AbilityNamesies.FUR_COAT));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().with(AttackNamesies.SURF).defending(AbilityNamesies.FUR_COAT));
    }

    private void statModifierTest(double expectedChange, Stat stat, TestInfo testInfo) {
        statModifierTest(expectedChange, 1, stat, testInfo);
    }

    private void statModifierTest(double expectedChange, double otherExpectedChange, Stat stat, TestInfo testInfo) {
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        TestPokemon statPokemon = stat.user() ? attacking : defending;
        TestPokemon otherPokemon = stat.user() ? defending : attacking;

        int beforeStat = Stat.getStat(stat, statPokemon, otherPokemon, battle);
        int otherBeforeStat = Stat.getStat(stat, otherPokemon, statPokemon, battle);

        testInfo.manipulate(battle);

        int afterStat = Stat.getStat(stat, statPokemon, otherPokemon, battle);
        int otherAfterStat = Stat.getStat(stat, otherPokemon, statPokemon, battle);

        Assert.assertEquals(
                StringUtils.spaceSeparated(beforeStat, afterStat, expectedChange, testInfo),
                (int)(beforeStat*expectedChange),
                afterStat
        );

        Assert.assertEquals((int)(otherBeforeStat*otherExpectedChange), otherAfterStat);
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

        // Normalize boosts non-normal-type moves that have been changed to normal type
        powerChangeTest(1, AttackNamesies.SWIFT, new TestInfo().attacking(AbilityNamesies.NORMALIZE));
        powerChangeTest(1.2, AttackNamesies.THUNDERBOLT, new TestInfo().attacking(AbilityNamesies.NORMALIZE));
        powerChangeTest(1.2, AttackNamesies.VINE_WHIP, new TestInfo().attacking(AbilityNamesies.NORMALIZE));

        // Multiscale halves power when at full health
        powerChangeTest(.5, AttackNamesies.VINE_WHIP, new TestInfo().defending(AbilityNamesies.MULTISCALE));
        powerChangeTest(1, AttackNamesies.VINE_WHIP, new TestInfo().defending(AbilityNamesies.MULTISCALE).with((battle, attacking, defending) -> {
            battle.attackingFight(AttackNamesies.FALSE_SWIPE);
            defending.assertNotFullHealth();
        }));
    }

    @Test
    public void terrainTest() {
        // Terrain effects boost/lower the power of certain types of moves
        powerChangeTest(1.5, AttackNamesies.THUNDER, new TestInfo().attacking(TerrainNamesies.ELECTRIC_TERRAIN));
        powerChangeTest(1.5, AttackNamesies.PSYCHIC, new TestInfo().attacking(TerrainNamesies.PSYCHIC_TERRAIN));
        powerChangeTest(1.5, AttackNamesies.SOLAR_BEAM, new TestInfo().attacking(TerrainNamesies.GRASSY_TERRAIN));
        powerChangeTest(.5, AttackNamesies.OUTRAGE, new TestInfo().attacking(TerrainNamesies.MISTY_TERRAIN));

        // Different move type -- no change
        powerChangeTest(1, AttackNamesies.DAZZLING_GLEAM, new TestInfo().attacking(TerrainNamesies.MISTY_TERRAIN));

        // Float with Flying -- no change
        powerChangeTest(1, AttackNamesies.PSYBEAM, new TestInfo().attacking(PokemonNamesies.PIDGEOT, TerrainNamesies.PSYCHIC_TERRAIN));

        // Float with Levitate -- no change
        powerChangeTest(1, AttackNamesies.THUNDER, new TestInfo().attacking(AbilityNamesies.LEVITATE).attacking(TerrainNamesies.ELECTRIC_TERRAIN));

        // Float with telekinesis -- no change
        powerChangeTest(1, AttackNamesies.VINE_WHIP, new TestInfo().attacking(PokemonEffectNamesies.TELEKINESIS).attacking(TerrainNamesies.GRASSY_TERRAIN));
    }

    // No modifier without manipulation, expectedModifier with it
    private void powerChangeTest(double expectedModifier, AttackNamesies attackNamesies, TestInfo testInfo) {
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Check modifiers manually
        double beforeModifier = battle.getDamageModifier(attacking, defending);
        TestUtils.assertEquals(1, beforeModifier);

        testInfo.manipulate(battle);
        attacking.setupMove(attackNamesies, battle);
        double afterModifier = battle.getDamageModifier(attacking, defending);

        TestUtils.assertEquals(
                StringUtils.spaceSeparated(attackNamesies, testInfo),
                expectedModifier,
                afterModifier
        );

        // Make sure modifiers actually happen in battle
        powerChangeTest(expectedModifier, false, attackNamesies, testInfo);
        powerChangeTest(expectedModifier, true, attackNamesies, testInfo);
    }

    private void powerChangeTest(double expectedModifier, boolean manipulate, AttackNamesies attackNamesies, TestInfo testInfo) {
        TestBattle battle = testInfo.createBattle();
        if (manipulate) {
            testInfo.manipulate(battle);
        }

        battle.getAttacking().setExpectedDamageModifier(manipulate ? expectedModifier : 1);
        battle.attackingFight(attackNamesies);
    }

    @Test
    public void powerModifierTest() {
        // Acrobatics has double power when not holding an item
        powerModifierTest(2, AttackNamesies.ACROBATICS, new TestInfo());
        powerModifierTest(1, AttackNamesies.ACROBATICS, new TestInfo().attacking(ItemNamesies.POTION));

        // Body Slam -- doubles when the opponent uses Minimize
        powerModifierTest(1, AttackNamesies.BODY_SLAM, new TestInfo());
        powerModifierTest(2, AttackNamesies.BODY_SLAM, new TestInfo().defendingFight(AttackNamesies.MINIMIZE));
    }

    // Differs from the powerChangeTest in that it only checks once
    // Immediately applies manipulations in the testInfo and confirms the power modifier
    private void powerModifierTest(double expectedModifier, AttackNamesies attackNamesies, TestInfo testInfo) {
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        testInfo.manipulate(battle);

        // Manual check
        attacking.setupMove(attackNamesies, battle);
        TestUtils.assertEquals(expectedModifier, battle.getDamageModifier(attacking, defending));

        // Battle check
        attacking.setExpectedDamageModifier(expectedModifier);
        battle.attackingFight(attackNamesies);
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
        stageChangeTest(0, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.TANGLED_FEET, PokemonEffectNamesies.CONFUSION));

        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SAND_VEIL, WeatherNamesies.SANDSTORM));
        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SNOW_CLOAK, WeatherNamesies.HAILING));
        stageChangeTest(0, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.SNOW_CLOAK, WeatherNamesies.HAILING));
        stageChangeTest(0, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SNOW_CLOAK, WeatherNamesies.SANDSTORM));
        stageChangeTest(0, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SAND_VEIL, WeatherNamesies.SUNNY));

        stageChangeTest(-2, Stat.EVASION, new TestInfo().attacking(StandardBattleEffectNamesies.GRAVITY));

        stageChangeTest(-2, Stat.DEFENSE, new TestInfo().attackingFight(AttackNamesies.SCREECH));
        stageChangeTest(-4, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.SIMPLE).attackingFight(AttackNamesies.SCREECH));

        stageChangeTest(2, Stat.ATTACK, new TestInfo().attackingFight(AttackNamesies.SWORDS_DANCE));
        stageChangeTest(4, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.SIMPLE).attackingFight(AttackNamesies.SWORDS_DANCE));

        stageChangeTest(1, Stat.ATTACK, new TestInfo().attackingFight(AttackNamesies.GROWTH));
        stageChangeTest(1, Stat.SP_ATTACK, new TestInfo().attackingFight(AttackNamesies.GROWTH));
        stageChangeTest(2, Stat.ATTACK, new TestInfo().attackingFight(AttackNamesies.SUNNY_DAY).attackingFight(AttackNamesies.GROWTH));
        stageChangeTest(2, Stat.SP_ATTACK, new TestInfo().attackingFight(AttackNamesies.SUNNY_DAY).attackingFight(AttackNamesies.GROWTH));

        stageChangeTest(
                3,
                Stat.SP_DEFENSE,
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
                0,
                Stat.SP_DEFENSE,
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
                2,
                Stat.DEFENSE,
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
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        TestPokemon statPokemon = stat.user() ? attacking : defending;
        TestPokemon otherPokemon = stat.user() ? defending : attacking;

        int beforeStage = Stat.getStage(stat, statPokemon, otherPokemon, battle);
        Assert.assertEquals(0, beforeStage);

        testInfo.manipulate(battle);
        int afterStage = Stat.getStage(stat, statPokemon, otherPokemon, battle);

        Assert.assertEquals(
                StringUtils.spaceSeparated(afterStage, expectedStage, stat, testInfo),
                expectedStage,
                afterStage
        );
    }

    @Test
    public void absorbTypeTest() {
        stageChangeTest(1, Stat.SP_ATTACK, new TestInfo()
                .attacking(AbilityNamesies.LIGHTNINGROD)
                .defendingFight(AttackNamesies.THUNDER_PUNCH));

        stageChangeTest(0, Stat.SP_ATTACK, new TestInfo()
                .defending(AbilityNamesies.LIGHTNINGROD)
                .defendingFight(AttackNamesies.THUNDER_PUNCH));

        stageChangeTest(0, Stat.SP_ATTACK, new TestInfo()
                .attacking(AbilityNamesies.LIGHTNINGROD)
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
        Assert.assertFalse(defending.isType(battle, Type.DARK));
        defending.setCastSource((ChangeTypeSource)(b, caster, victim) -> new PokeType(Type.DARK));
        Assert.assertFalse(defending.isType(battle, Type.DARK));
        Effect.cast(PokemonEffectNamesies.CHANGE_TYPE, battle, defending, defending, CastSource.CAST_SOURCE, false);
        Assert.assertTrue(defending.isType(battle, Type.DARK));
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
}
