package test.battle;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.CastSource;
import battle.effect.attack.ChangeTypeSource;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import test.BaseTest;
import test.TestPokemon;
import test.TestUtils;
import type.PokeType;
import type.Type;
import util.StringUtils;

public class ModifierTest extends BaseTest {
    @Test
    public void statChangeTest() {
        statModifierTest(1.5, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.HUSTLE));
        statModifierTest(.8, Stat.ACCURACY, new TestInfo().attacking(AbilityNamesies.HUSTLE));
        statModifierTest(1, Stat.SP_ATTACK, new TestInfo().attacking(AbilityNamesies.HUSTLE));

        statModifierTest(2, Stat.SP_DEFENSE, new TestInfo().defending(EffectNamesies.LIGHT_SCREEN));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(EffectNamesies.LIGHT_SCREEN));

        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.LILEEP).defending(EffectNamesies.SANDSTORM));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.MAWILE).defending(EffectNamesies.SANDSTORM));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().defending(PokemonNamesies.SANDYGAST).defending(EffectNamesies.SANDSTORM));
        statModifierTest(1, Stat.DEFENSE, new TestInfo().defending(PokemonNamesies.GEODUDE).defending(EffectNamesies.SANDSTORM));

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

        statModifierTest(2, Stat.SPEED, new TestInfo().attacking(AbilityNamesies.CHLOROPHYLL).attacking(EffectNamesies.SUNNY));
        statModifierTest(1, Stat.SP_DEFENSE, new TestInfo().attacking(AbilityNamesies.CHLOROPHYLL).attacking(EffectNamesies.SUNNY));

        statModifierTest(1.5, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(EffectNamesies.SUNNY));
        statModifierTest(1.5, Stat.SP_DEFENSE, new TestInfo().defending(AbilityNamesies.FLOWER_GIFT).attacking(EffectNamesies.SUNNY));
        statModifierTest(1, 1.5, Stat.SP_DEFENSE, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(EffectNamesies.SUNNY));
        statModifierTest(1, Stat.SPEED, new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attacking(EffectNamesies.SUNNY));

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

        TestUtils.assertEquals(
                StringUtils.spaceSeparated(beforeStat, afterStat, expectedChange, testInfo),
                (int)(beforeStat*expectedChange),
                afterStat
        );

        TestUtils.assertEquals((int)(otherBeforeStat*otherExpectedChange), otherAfterStat);
    }

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
        powerModifierTest(1.2, new TestInfo().attacking(PokemonNamesies.DIALGA, ItemNamesies.ADAMANT_ORB).with(AttackNamesies.IRON_HEAD));
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

        // Galvanize boosts normal-type moves that have been changed electric type
        powerModifierTest(1.2, new TestInfo().attacking(AbilityNamesies.GALVANIZE).with(AttackNamesies.SWIFT));
        powerModifierTest(1, new TestInfo().attacking(AbilityNamesies.GALVANIZE).with(AttackNamesies.THUNDERBOLT));
        powerModifierTest(1, new TestInfo().attacking(AbilityNamesies.GALVANIZE).with(AttackNamesies.VINE_WHIP));

        // Normalize boosts non-normal-type moves that have been changed to normal type
        powerModifierTest(1, new TestInfo().attacking(AbilityNamesies.NORMALIZE).with(AttackNamesies.SWIFT));
        powerModifierTest(1.2, new TestInfo().attacking(AbilityNamesies.NORMALIZE).with(AttackNamesies.THUNDERBOLT));
        powerModifierTest(1.2, new TestInfo().attacking(AbilityNamesies.NORMALIZE).with(AttackNamesies.VINE_WHIP));
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
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        double beforeModifier = battle.getDamageModifier(attacking, defending);

        testInfo.manipulate(battle);
        double afterModifier = battle.getDamageModifier(attacking, defending);

        TestUtils.assertEquals(
                StringUtils.spaceSeparated(beforeModifier, afterModifier, expectedChange, testInfo),
                expectedChange*beforeModifier,
                afterModifier
        );
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

        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.TANGLED_FEET, EffectNamesies.CONFUSION));
        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.TANGLED_FEET).attackingFight(AttackNamesies.CONFUSE_RAY));
        stageChangeTest(0, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.TANGLED_FEET, EffectNamesies.CONFUSION));

        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SAND_VEIL, EffectNamesies.SANDSTORM));
        stageChangeTest(1, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SNOW_CLOAK, EffectNamesies.HAILING));
        stageChangeTest(0, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.SNOW_CLOAK, EffectNamesies.HAILING));
        stageChangeTest(0, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SNOW_CLOAK, EffectNamesies.SANDSTORM));
        stageChangeTest(0, Stat.EVASION, new TestInfo().defending(AbilityNamesies.SAND_VEIL, EffectNamesies.SUNNY));

        stageChangeTest(-2, Stat.EVASION, new TestInfo().attacking(EffectNamesies.GRAVITY));

        stageChangeTest(-2, Stat.DEFENSE, new TestInfo().attackingFight(AttackNamesies.SCREECH));
        stageChangeTest(-4, Stat.DEFENSE, new TestInfo().defending(AbilityNamesies.SIMPLE).attackingFight(AttackNamesies.SCREECH));

        stageChangeTest(2, Stat.ATTACK, new TestInfo().attackingFight(AttackNamesies.SWORDS_DANCE));
        stageChangeTest(4, Stat.ATTACK, new TestInfo().attacking(AbilityNamesies.SIMPLE).attackingFight(AttackNamesies.SWORDS_DANCE));

        stageChangeTest(1, Stat.ATTACK, new TestInfo().attackingFight(AttackNamesies.GROWTH));
        stageChangeTest(2, Stat.ATTACK, new TestInfo().attackingFight(AttackNamesies.SUNNY_DAY).attackingFight(AttackNamesies.GROWTH));

        stageChangeTest(
                3,
                Stat.SP_DEFENSE,
                new TestInfo().with((battle, attacking, defending) -> {
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    Assert.assertTrue(defending.hasEffect(EffectNamesies.STOCKPILE));
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
                    Assert.assertFalse(defending.hasEffect(EffectNamesies.STOCKPILE));
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
                    Assert.assertTrue(defending.hasEffect(EffectNamesies.STOCKPILE));
                    Assert.assertFalse(defending.fullHealth());
                    battle.defendingFight(AttackNamesies.SWALLOW);
                    Assert.assertTrue(defending.fullHealth());
                    Assert.assertFalse(defending.hasEffect(EffectNamesies.STOCKPILE));
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    Assert.assertTrue(defending.hasEffect(EffectNamesies.STOCKPILE));
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
    public void flashFireTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SQUIRTLE, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.FLASH_FIRE);
        TestPokemon defending = battle.getDefending();

        attacking.setupMove(AttackNamesies.EMBER, battle);
        double unactivatedFire = battle.getDamageModifier(attacking, defending);
        TestUtils.assertEquals(1, unactivatedFire);

        battle.defendingFight(AttackNamesies.EMBER);
        attacking.setupMove(AttackNamesies.SURF, battle);
        double activatedNonFire = battle.getDamageModifier(attacking, defending);
        TestUtils.assertEquals(1, activatedNonFire);

        attacking.setupMove(AttackNamesies.EMBER, battle);
        double activatedFire = battle.getDamageModifier(attacking, defending);
        TestUtils.assertEquals(1.5, activatedFire);
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
        EffectNamesies.CHANGE_TYPE.getEffect().cast(battle, defending, defending, CastSource.CAST_SOURCE, false);
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
        Assert.assertFalse(attacking.fullHealth());
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
