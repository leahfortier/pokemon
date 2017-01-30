package test;

import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import util.StringUtils;

public class ModifierTest {
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
        TestPokemon attacking = new TestPokemon(testInfo.attackingName);
        TestPokemon defending = new TestPokemon(testInfo.defendingName);

        TestPokemon statPokemon = stat.user() ? attacking : defending;
        TestPokemon otherPokemon = stat.user() ? defending : attacking;

        TestBattle battle = TestBattle.create(attacking, defending);
        attacking.setupMove(testInfo.attackName, battle, defending);

        int beforeStat = Stat.getStat(stat, statPokemon, otherPokemon, battle);
        int otherBeforeStat = Stat.getStat(stat, otherPokemon, statPokemon, battle);

        testInfo.manipulator.manipulate(battle, attacking, defending);

        int afterStat = Stat.getStat(stat, statPokemon, otherPokemon, battle);
        int otherAfterStat = Stat.getStat(stat, otherPokemon, statPokemon, battle);

        Assert.assertTrue(
                StringUtils.spaceSeparated(beforeStat, afterStat, expectedChange, testInfo.toString()),
                (int)(beforeStat*expectedChange) == afterStat
        );

        Assert.assertTrue((int)(otherBeforeStat*otherExpectedChange) == otherAfterStat);
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

    @Test
    public void stageChangeTest() {
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

        stageChangeTest(
                3,
                Stat.SP_DEFENSE,
                new TestInfo().with((battle, attacking, defending) -> {
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
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
                    Assert.assertTrue(!defending.fullHealth());
                    battle.defendingFight(AttackNamesies.SWALLOW);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                    battle.defendingFight(AttackNamesies.STOCKPILE);
                })
        );
    }

    private void stageChangeTest(int expectedStage, Stat stat, TestInfo testInfo) {
        TestPokemon attacking = new TestPokemon(testInfo.attackingName);
        TestPokemon defending = new TestPokemon(testInfo.defendingName);

        TestBattle battle = TestBattle.create(attacking, defending);

        TestPokemon statPokemon = stat.user() ? attacking : defending;
        TestPokemon otherPokemon = stat.user() ? defending : attacking;

        attacking.setupMove(testInfo.attackName, battle, defending);
        int beforeStage = Stat.getStage(stat, statPokemon, otherPokemon, battle);
        Assert.assertTrue(beforeStage == 0);

        testInfo.manipulator.manipulate(battle, attacking, defending);
        int afterStage = Stat.getStage(stat, statPokemon, otherPokemon, battle);

        Assert.assertTrue(
                StringUtils.spaceSeparated(afterStage, expectedStage, stat, testInfo.toString()),
                afterStage == expectedStage
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
        TestPokemon attacking = new TestPokemon(PokemonNamesies.SQUIRTLE).withAbility(AbilityNamesies.FLASH_FIRE);
        TestPokemon defending = new TestPokemon(PokemonNamesies.CHARMANDER);

        TestBattle battle = TestBattle.create(attacking, defending);

        attacking.setupMove(AttackNamesies.EMBER, battle, defending);
        double unactivatedFire = battle.getDamageModifier(attacking, defending);
        Assert.assertTrue(unactivatedFire + "", unactivatedFire == 1);

        battle.defendingFight(AttackNamesies.EMBER);
        attacking.setupMove(AttackNamesies.SURF, battle, defending);
        double activatedNonFire = battle.getDamageModifier(attacking, defending);
        Assert.assertTrue(activatedNonFire + "", activatedNonFire == 1);

        attacking.setupMove(AttackNamesies.EMBER, battle, defending);
        double activatedFire = battle.getDamageModifier(attacking, defending);
        Assert.assertTrue(activatedFire + "", activatedFire == 1.5);
    }
}
