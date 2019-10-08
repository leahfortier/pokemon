package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import test.general.BaseTest;
import test.pokemon.TestPokemon;
import test.general.TestUtils;
import trainer.EnemyTrainer;
import type.Type;
import type.TypeAdvantage;

public class AbilityTest extends BaseTest {
    @Test
    public void descriptionTest() {
        for (AbilityNamesies abilityNamesies : AbilityNamesies.values()) {
            if (abilityNamesies == AbilityNamesies.NO_ABILITY) {
                continue;
            }

            // All descriptions should end with a period
            Ability ability = abilityNamesies.getNewAbility();
            String description = ability.getDescription();
            Assert.assertTrue(ability.getName() + " " + description, description.endsWith("."));
        }
    }

    @Test
    public void levitateTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.LEVITATE);

        // Ground moves should not hit a levitating Pokemon
        attacking.setupMove(AttackNamesies.EARTHQUAKE, battle);
        Assert.assertTrue(TypeAdvantage.doesNotEffect(attacking, defending, battle));

        // Even if holding a Ring Target
        defending.giveItem(ItemNamesies.RING_TARGET);
        Assert.assertTrue(TypeAdvantage.doesNotEffect(attacking, defending, battle));

        // Unless the user has mold breaker
        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        Assert.assertFalse(TypeAdvantage.doesNotEffect(attacking, defending, battle));
        defending.removeItem();
        Assert.assertFalse(TypeAdvantage.doesNotEffect(attacking, defending, battle));

        // Or Gravity is in effect
        attacking.withAbility(AbilityNamesies.NO_ABILITY);
        Assert.assertTrue(TypeAdvantage.doesNotEffect(attacking, defending, battle));
        battle.defendingFight(AttackNamesies.GRAVITY);
        Assert.assertFalse(TypeAdvantage.doesNotEffect(attacking, defending, battle));
    }

    @Test
    public void wonderGuardTest() {
        TestInfo emptyInfo = new TestInfo();

        // Status move should work
        wonderGuardTest(AttackNamesies.DRAGON_DANCE, emptyInfo, (battle, attacking, defending) -> {
            attacking.assertStages(new TestStages().set(1, Stat.ATTACK, Stat.SPEED));
            defending.assertNoStages();
            defending.assertFullHealth();
        });
        wonderGuardTest(AttackNamesies.THUNDER_WAVE, emptyInfo, (battle, attacking, defending) -> {
            defending.assertHasStatus(StatusNamesies.PARALYZED);
            defending.assertFullHealth();
            defending.assertNoStages();
        });

        PokemonManipulator murder = (battle, attacking, defending) -> Assert.assertTrue(defending.isActuallyDead());

        // Super-effective moves and moves without type work
        wonderGuardTest(AttackNamesies.SHADOW_BALL, emptyInfo, murder);
        wonderGuardTest(AttackNamesies.STRUGGLE, emptyInfo, murder);

        PokemonManipulator allClear = (battle, attacking, defending) -> {
            attacking.assertFullHealth();
            defending.assertFullHealth();
            attacking.assertNoStages();
            defending.assertNoStages();
            attacking.assertNoStatus();
            defending.assertNoStatus();
        };

        // Attacking non-super effective moves should not work
        wonderGuardTest(AttackNamesies.SURF, emptyInfo, allClear);
        wonderGuardTest(AttackNamesies.VINE_WHIP, emptyInfo, allClear);
        wonderGuardTest(AttackNamesies.TACKLE, emptyInfo, allClear);
        wonderGuardTest(AttackNamesies.FLASH_CANNON, emptyInfo, allClear);

        // Unless the attacker breaks the mold
        wonderGuardTest(AttackNamesies.SUNSTEEL_STRIKE, emptyInfo, murder);

        TestInfo moldBreaker = new TestInfo().attacking(AbilityNamesies.MOLD_BREAKER);
        wonderGuardTest(AttackNamesies.SURF, moldBreaker, murder);
        wonderGuardTest(AttackNamesies.VINE_WHIP, moldBreaker, murder);

        // Tackle will still fail since Shedinja is Ghost-type
        wonderGuardTest(AttackNamesies.TACKLE, moldBreaker, allClear);
    }

    private void wonderGuardTest(AttackNamesies attackNamesies, TestInfo testInfo, PokemonManipulator testSuccess) {
        testInfo.defending(PokemonNamesies.SHEDINJA, AbilityNamesies.WONDER_GUARD);

        TestBattle battle = testInfo.createBattle();
        testInfo.manipulate(battle);

        battle.attackingFight(attackNamesies);
        testSuccess.manipulate(battle);
    }

    @Test
    public void absorbTypeTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.LANTURN);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.VOLT_ABSORB);

        battle.attackingFight(AttackNamesies.CONSTRICT);
        defending.assertNotFullHealth();

        // Thunderbolt should heal
        attacking.apply(false, AttackNamesies.THUNDERBOLT, battle);
        defending.assertFullHealth();

        battle.attackingFight(AttackNamesies.CONSTRICT);
        defending.assertNotFullHealth();

        battle.attackingFight(AttackNamesies.WATER_GUN);
        defending.assertNotFullHealth();
    }

    @Test
    public void dampTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        attacking.apply(true, AttackNamesies.SELF_DESTRUCT, battle);
        battle.emptyHeal();
        defending.apply(true, AttackNamesies.EXPLOSION, battle);

        battle.emptyHeal();
        attacking.withAbility(AbilityNamesies.DAMP);
        attacking.apply(false, AttackNamesies.SELF_DESTRUCT, battle);
        defending.apply(false, AttackNamesies.EXPLOSION, battle);
    }

    @Test
    public void testColorChange() {
        TestBattle battle = TestBattle.create(PokemonNamesies.HAPPINY, PokemonNamesies.KECLEON);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.COLOR_CHANGE);

        Assert.assertTrue(defending.hasAbility(AbilityNamesies.COLOR_CHANGE));
        defending.assertType(battle, Type.NORMAL);

        battle.fight(AttackNamesies.WATER_GUN, AttackNamesies.ENDURE);
        defending.assertType(battle, Type.WATER);
        defending.assertNotType(battle, Type.NORMAL);

        battle.emptyHeal();
        battle.fight(AttackNamesies.EMBER, AttackNamesies.ENDURE);
        defending.assertType(battle, Type.FIRE);
        defending.assertNotType(battle, Type.WATER);

        // Status moves should not change type
        battle.attackingFight(AttackNamesies.GROWL);
        defending.assertType(battle, Type.FIRE);
        defending.assertNotType(battle, Type.NORMAL);

        // Status moves should not change type
        battle.attackingFight(AttackNamesies.THUNDER_WAVE);
        defending.assertType(battle, Type.FIRE);
        defending.assertNotType(battle, Type.ELECTRIC);

        battle.attackingFight(AttackNamesies.TACKLE);
        defending.assertType(battle, Type.NORMAL);
        defending.assertNotType(battle, Type.FIRE);
    }

    @Test
    public void priorityPreventionTest() {
        // Queenly Majesty should block priority moves that are not self-target
        checkPriorityPrevention(0, false, AttackNamesies.TACKLE);
        checkPriorityPrevention(0, false, AttackNamesies.NASTY_PLOT);
        checkPriorityPrevention(0, false, AttackNamesies.STRING_SHOT);
        checkPriorityPrevention(1, true, AttackNamesies.QUICK_ATTACK);
        checkPriorityPrevention(1, true, AttackNamesies.BABY_DOLL_EYES);
        checkPriorityPrevention(1, true, AttackNamesies.BIDE);
        checkPriorityPrevention(4, false, AttackNamesies.PROTECT);

        // Should block moves that have their priority increases via Prankster (+1 for status moves)
        // Should not block though for the self-target moves (like Nasty Plot and Protect)
        PokemonManipulator prankster = PokemonManipulator.giveAttackingAbility(AbilityNamesies.PRANKSTER);
        checkPriorityPrevention(0, 0, false, AttackNamesies.TACKLE, prankster);
        checkPriorityPrevention(0, 1, false, AttackNamesies.NASTY_PLOT, prankster);
        checkPriorityPrevention(4, 5, false, AttackNamesies.PROTECT, prankster);
        checkPriorityPrevention(0, 1, true, AttackNamesies.STRING_SHOT, prankster);
        checkPriorityPrevention(1, 1, true, AttackNamesies.QUICK_ATTACK, prankster);

        // Mold breaker doesn't give a fuck (does not block moves in this case)
        PokemonManipulator moldBreaker = PokemonManipulator.giveAttackingAbility(AbilityNamesies.MOLD_BREAKER);
        checkPriorityPrevention(0, 0, false, AttackNamesies.TACKLE, moldBreaker);
        checkPriorityPrevention(0, 0, false, AttackNamesies.NASTY_PLOT, moldBreaker);
        checkPriorityPrevention(0, 0, false, AttackNamesies.STRING_SHOT, moldBreaker);
        checkPriorityPrevention(4, 4, false, AttackNamesies.PROTECT, moldBreaker);
        checkPriorityPrevention(1, 1, false, AttackNamesies.BABY_DOLL_EYES, moldBreaker);
        checkPriorityPrevention(1, 1, false, AttackNamesies.QUICK_ATTACK, moldBreaker);
    }

    private void checkPriorityPrevention(int expectedPriority, boolean prevent, AttackNamesies attack) {
        checkPriorityPrevention(expectedPriority, expectedPriority, prevent, attack, PokemonManipulator.empty());
    }

    private void checkPriorityPrevention(int beforePriority, int afterPriority, boolean prevent, AttackNamesies attack, PokemonManipulator manipulator) {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        defending.withAbility(AbilityNamesies.QUEENLY_MAJESTY);

        attacking.setupMove(attack, battle);
        Assert.assertEquals(beforePriority, battle.getAttackPriority(attacking));
        attacking.apply(beforePriority <= 0 || attacking.getAttack().isSelfTargetStatusMove(), attack, battle);

        battle.emptyHeal();
        manipulator.manipulate(battle);

        attacking.setupMove(attack, battle);
        Assert.assertEquals(afterPriority, battle.getAttackPriority(attacking));
        attacking.apply(!prevent, attack, battle);
        Assert.assertEquals(afterPriority > 0 && !attacking.getAttack().isSelfTargetStatusMove() && !attacking.breaksTheMold(), prevent);
    }

    @Test
    public void contraryTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.CONTRARY);

        // Contrary Pokemon will have their stat increased when it should be decreased
        battle.fight(AttackNamesies.STRING_SHOT, AttackNamesies.STRING_SHOT);
        attacking.assertStages(new TestStages().set(-2, Stat.SPEED));
        defending.assertStages(new TestStages().set(2, Stat.SPEED));

        // Will also decrease their stat when it should be increased
        battle.fight(AttackNamesies.AGILITY, AttackNamesies.AGILITY);
        attacking.assertNoStages();
        defending.assertNoStages();

        battle.fight(AttackNamesies.SHELL_SMASH, AttackNamesies.SHELL_SMASH);
        attacking.assertStages(new TestStages().set(2, Stat.ATTACK, Stat.SP_ATTACK, Stat.SPEED)
                                               .set(-1, Stat.DEFENSE, Stat.SP_DEFENSE));
        defending.assertStages(new TestStages().set(-2, Stat.ATTACK, Stat.SP_ATTACK, Stat.SPEED)
                                               .set(1, Stat.DEFENSE, Stat.SP_DEFENSE));

        battle.attackingFight(AttackNamesies.HAZE);
        attacking.assertNoStages();
        defending.assertNoStages();

        // Contrary is affected by Mold Breaker
        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        battle.fight(AttackNamesies.STRING_SHOT, AttackNamesies.STRING_SHOT);
        attacking.assertStages(new TestStages().set(-2, Stat.SPEED));
        defending.assertStages(new TestStages().set(-2, Stat.SPEED));

        // Reset stages and remove Mold Breaker
        battle.fight(AttackNamesies.HAZE, AttackNamesies.GASTRO_ACID);
        attacking.assertNoStages();
        defending.assertNoStages();

        // Mist prevents stat reductions
        battle.defendingFight(AttackNamesies.MIST);

        // String shot is no longer a reduction, so it works
        battle.attackingFight(AttackNamesies.STRING_SHOT);
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(2, Stat.SPEED));

        // Swagger is now a reduction, so it fails to raise/lower attack, but still confuses
        // Persim Berry heals confusion -- it should be consumed (since I don't wanna deal with confusion in tests)
        defending.giveItem(ItemNamesies.PERSIM_BERRY);
        battle.attackingFight(AttackNamesies.SWAGGER);
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(2, Stat.SPEED));
        defending.assertNoEffect(PokemonEffectNamesies.CONFUSION);
        defending.assertConsumedBerry(battle);

        // Simple doubles stat modifications to itself -- shouldn't affect contrary pokemon
        battle.fight(AttackNamesies.HAZE, AttackNamesies.SIMPLE_BEAM);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.SIMPLE));
        attacking.assertNoStages();
        defending.assertNoStages();

        battle.fight(AttackNamesies.STRING_SHOT, AttackNamesies.STRING_SHOT);
        attacking.assertStages(new TestStages().set(-4, Stat.SPEED));
        defending.assertStages(new TestStages().set(2, Stat.SPEED));

        battle.attackingFight(AttackNamesies.HAZE);
        attacking.assertNoStages();
        defending.assertNoStages();

        // Belly Drum sets attack stage to -6 instead of +6
        battle.fight(AttackNamesies.BELLY_DRUM, AttackNamesies.BELLY_DRUM);
        attacking.assertStages(new TestStages().set(6, Stat.ATTACK));
        defending.assertStages(new TestStages().set(-6, Stat.ATTACK));
        attacking.assertHealthRatio(.5);
        defending.assertHealthRatio(.5);

        // Will still succeed and cut health when at -6 instead of +6 for Contrary :(
        battle.emptyHeal();
        battle.fight(AttackNamesies.BELLY_DRUM, AttackNamesies.BELLY_DRUM);
        Assert.assertFalse(attacking.lastMoveSucceeded());
        Assert.assertTrue(defending.lastMoveSucceeded());
        attacking.assertHealthRatio(1);
        defending.assertHealthRatio(.5);
        attacking.assertStages(new TestStages().set(6, Stat.ATTACK));
        defending.assertStages(new TestStages().set(-6, Stat.ATTACK));

        battle.clearAllEffects();
        attacking.withAbility(AbilityNamesies.STURDY);
        defending.giveItem(ItemNamesies.FOCUS_SASH);

        // Leaf Storm is a damage dealing move that also decreases the user's Sp. Attack UNLESS YOU HAVE CONTRARY
        battle.fight(AttackNamesies.LEAF_STORM, AttackNamesies.LEAF_STORM);
        attacking.assertStages(new TestStages().set(-2, Stat.SP_ATTACK));
        defending.assertStages(new TestStages().set(2, Stat.SP_ATTACK));

        battle.fight(AttackNamesies.SWORDS_DANCE, AttackNamesies.SWORDS_DANCE);
        attacking.assertStages(new TestStages().set(2, Stat.ATTACK).set(-2, Stat.SP_ATTACK));
        defending.assertStages(new TestStages().set(-2, Stat.ATTACK).set(2, Stat.SP_ATTACK));

        // Gaining/Losing Contrary does not affect your current stages
        battle.defendingFight(AttackNamesies.SKILL_SWAP);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.CONTRARY));
        Assert.assertFalse(defending.hasAbility(AbilityNamesies.CONTRARY));
        attacking.assertStages(new TestStages().set(2, Stat.ATTACK).set(-2, Stat.SP_ATTACK));
        defending.assertStages(new TestStages().set(-2, Stat.ATTACK).set(2, Stat.SP_ATTACK));
    }

    @Test
    public void sheerForceTest() {
        // Mystical Fire has a 100% chance to reduce target's Sp. Attack
        sheerForceSuccessTest(
                AttackNamesies.MYSTICAL_FIRE,
                (battle, attacking, defending) -> defending.assertNoStages(),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(-1, Stat.SP_ATTACK))
        );

        // Power-Up Punch has a 100% chance to raise the user's Attack
        sheerForceSuccessTest(
                AttackNamesies.POWER_UP_PUNCH,
                (battle, attacking, defending) -> attacking.assertNoStages(),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(1, Stat.ATTACK))
        );

        // Dynamic Punch has a 100% chance to confuse the target
        sheerForceSuccessTest(
                AttackNamesies.DYNAMIC_PUNCH,
                (battle, attacking, defending) -> defending.assertNoEffect(PokemonEffectNamesies.CONFUSION),
                (battle, attacking, defending) -> defending.assertHasEffect(PokemonEffectNamesies.CONFUSION)
        );

        // Inferno has a 100% chance to burn the target
        sheerForceSuccessTest(
                AttackNamesies.INFERNO,
                (battle, attacking, defending) -> defending.assertNoStatus(),
                (battle, attacking, defending) -> defending.assertHasStatus(StatusNamesies.BURNED)
        );

        // Flare Blitz has a chance to Burn, so gets increase from Sheer Force, but should still take recoil damage regardless
        sheerForceSuccessTest(
                AttackNamesies.FLARE_BLITZ,
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertNoStatus();
                },
                (battle, attacking, defending) -> attacking.assertNotFullHealth()
        );

        // Sheer Force does not effect standard recoil moves
        sheerForceFailureTest(
                AttackNamesies.TAKE_DOWN,
                (battle, attacking, defending) -> attacking.assertNotFullHealth()
        );

        // Sheer Force does not effect crit-raising moves
        sheerForceFailureTest(AttackNamesies.NIGHT_SLASH, PokemonManipulator.empty());

        // Sheer Force does not apply to secondary effects that are negative to the user
        sheerForceFailureTest(AttackNamesies.OUTRAGE, PokemonManipulator.empty());
        sheerForceFailureTest(
                AttackNamesies.OVERHEAT,
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(-2, Stat.SP_ATTACK))
        );
    }

    private void sheerForceSuccessTest(AttackNamesies attackNamesies, PokemonManipulator withSheerForceChecks, PokemonManipulator withoutSheerForceChecks) {
        sheerForceTest(true, attackNamesies, withSheerForceChecks, withoutSheerForceChecks);
    }

    // Checks will be the same if it doesn't apply
    private void sheerForceFailureTest(AttackNamesies attackNamesies, PokemonManipulator checks) {
        sheerForceTest(false, attackNamesies, checks, checks);
    }

    private void sheerForceTest(boolean applies, AttackNamesies attackNamesies, PokemonManipulator withSheerForceChecks, PokemonManipulator withoutSheerForceChecks) {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        attacking.withAbility(AbilityNamesies.SHEER_FORCE);
        attacking.setExpectedDamageModifier(applies ? 1.3 : 1.0);
        battle.attackingFight(attackNamesies);
        withSheerForceChecks.manipulate(battle);

        battle.emptyHeal();
        battle.clearAllEffects();

        // Make sure it applies effects without Sheer Force
        attacking.withAbility(AbilityNamesies.NO_ABILITY);
        defending.withAbility(AbilityNamesies.SHEER_FORCE);
        attacking.setExpectedDamageModifier(1.0);
        battle.attackingFight(attackNamesies);
        withoutSheerForceChecks.manipulate(battle);
    }

    @Test
    public void steadfastTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.MOLD_BREAKER);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.STEADFAST);

        // Fake out will cause the defending to flinch, speed will raise and its attack will not execute
        // Steadfast is not affected by mold breaker
        battle.fight(AttackNamesies.FAKE_OUT, AttackNamesies.TACKLE);
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(1, Stat.SPEED));
        attacking.assertFullHealth();
        defending.assertNotFullHealth();

        defending.fullyHeal();

        // Fake out will fail the second time
        battle.fight(AttackNamesies.FAKE_OUT, AttackNamesies.TACKLE);
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(1, Stat.SPEED));
        attacking.assertNotFullHealth();
        defending.assertFullHealth();

        // Make sure speed doesn't raise from other effects
        battle.attackingFight(AttackNamesies.CONFUSE_RAY);
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(1, Stat.SPEED));
    }

    @Test
    public void innerFocusTest() {
        TestBattle battle = TestBattle.createTrainerBattle(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.INNER_FOCUS);
        TestPokemon defending1 = battle.getDefending();
        TestPokemon defending2 = TestPokemon.newTrainerPokemon(PokemonNamesies.SHUCKLE).withAbility(AbilityNamesies.MOLD_BREAKER);
        ((EnemyTrainer)battle.getOpponent()).addPokemon(defending2);
        Assert.assertTrue(battle.getDefending() == defending1);

        // Not fucking Sturdy
        defending1.assertAbility(AbilityNamesies.NO_ABILITY);

        // Fake out will not cause the defending to flinch and it will successfully use Tackle
        // (Fake Out will still strike first even though it is listed second since it has priority)
        battle.fight(AttackNamesies.TACKLE, AttackNamesies.FAKE_OUT);
        attacking.assertNoStages();
        defending1.assertNoStages();
        attacking.assertNotFullHealth();
        defending1.assertNotFullHealth();

        battle.emptyHeal();

        // Make sure other effects can still work
        attacking.withItem(ItemNamesies.PERSIM_BERRY);
        battle.defendingFight(AttackNamesies.CONFUSE_RAY);
        attacking.assertConsumedBerry(battle);

        // Switch opponent Pokemon so I can use Fake Out again
        // NOTE: We need to kill the defending instead of something like whirlwind otherwise Fake Out won't work
        // ANOTHER NOTE: Everything feels a little backwards with attacking and defending in this test
        // because this does not appropriately swap if the player is killed
        battle.attackingFight(AttackNamesies.FISSURE);
        Assert.assertTrue(battle.getDefending() == defending2);
        Assert.assertTrue(defending2.isFirstTurn());

        // Defending2 has Mold Breaker, so attacking will still flinch with Inner Focus
        battle.fight(AttackNamesies.TACKLE, AttackNamesies.FAKE_OUT);
        attacking.assertNoStages();
        defending2.assertNoStages();
        attacking.assertNotFullHealth();
        defending2.assertFullHealth();
    }

    @Test
    public void synchronizeTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.EEVEE, PokemonNamesies.HAPPINY);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.SYNCHRONIZE);

        // Thunder Wave will paralyze the target and then Synchronize with paralyze the attacker
        battle.attackingFight(AttackNamesies.THUNDER_WAVE);
        defending.assertHasStatus(StatusNamesies.PARALYZED);
        attacking.assertHasStatus(StatusNamesies.PARALYZED);

        attacking.giveItem(ItemNamesies.LUM_BERRY);
        defending.giveItem(ItemNamesies.CHERI_BERRY);
        battle.splashFight();
        attacking.assertNoStatus();
        defending.assertNoStatus();
        attacking.assertNotHoldingItem(battle);
        defending.assertNotHoldingItem(battle);

        // Synchronize does not work on Sleep
        battle.attackingFight(AttackNamesies.SPORE);
        defending.assertHasStatus(StatusNamesies.ASLEEP);
        attacking.assertNoStatus();

        defending.withMoves(AttackNamesies.SLEEP_TALK, AttackNamesies.REFRESH);
        battle.defendingFight(AttackNamesies.SLEEP_TALK);
        attacking.assertNoStatus();
        defending.assertNoStatus();

        // Make sure it works with bad poison
        battle.attackingFight(AttackNamesies.TOXIC);
        defending.assertBadPoison();
        attacking.assertBadPoison();

        battle.clearAllEffects();
        battle.emptyHeal();
        attacking.assertNoStatus();
        defending.assertNoStatus();

        // Both Pokemon with Synchronize and status healing berries
        // Attacking uses Thunder Wave and defending is paralyzed
        // Synchronize activates and attacker is paralyzed
        // Attacking heals by consuming berry
        // Defending heals by consuming berry
        attacking.withAbility(AbilityNamesies.SYNCHRONIZE);
        attacking.giveItem(ItemNamesies.LUM_BERRY);
        defending.giveItem(ItemNamesies.CHERI_BERRY);
        battle.attackingFight(AttackNamesies.THUNDER_WAVE);
        attacking.assertNoStatus();
        defending.assertNoStatus();
        attacking.assertNotHoldingItem(battle);
        defending.assertNotHoldingItem(battle);

        battle.clearAllEffects();
        battle.emptyHeal();
        attacking.assertNoStatus();
        defending.assertNoStatus();
        defending.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);

        // Synchronize will not activate for self-inflicted status conditions
        defending.giveItem(ItemNamesies.TOXIC_ORB);
        attacking.giveItem(ItemNamesies.PECHA_BERRY);
        battle.splashFight();
        defending.assertBadPoison();
        attacking.assertNoStatus();

        // Attacking will fling Pecha Berry at defending, curing its Poison by consuming the berry
        // Defending will fling Toxic Orb at attacking, inflicting Poison on it
        battle.fight(AttackNamesies.FLING, AttackNamesies.FLING);
        attacking.assertBadPoison();
        defending.assertNoStatus();
        attacking.assertNotHoldingItem(battle);
        defending.assertNotHoldingItem(battle);
        attacking.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
        defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);

        battle.attackingFight(AttackNamesies.REFRESH);
        attacking.assertNoStatus();
        defending.assertNoStatus();
    }

    @Test
    public void harvestTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.HARVEST);

        // Cell Battery increases attack when hit by an electric type move and is then consumed
        // Make sure it does not reappear even in harsh sunlight
        defending.withItem(ItemNamesies.CELL_BATTERY);
        battle.fight(AttackNamesies.NUZZLE, AttackNamesies.SUNNY_DAY);
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(1, Stat.ATTACK));
        defending.assertConsumedItem(battle);

        battle.emptyHeal();
        battle.clearAllEffects();

        // Give a Rawst Berry and then burn the target
        // Rawst Berry will heal its burn and then be consumed
        // Harvest will then restore the berry since the sunlight is strong
        defending.giveItem(ItemNamesies.RAWST_BERRY);
        battle.fight(AttackNamesies.WILL_O_WISP, AttackNamesies.SUNNY_DAY);
        defending.assertNoStatus();
        defending.assertHoldingItem(battle, ItemNamesies.RAWST_BERRY);
        defending.assertHasEffect(PokemonEffectNamesies.CONSUMED_ITEM);
        defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
    }

    @Test
    public void stanceChangeTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.AEGISLASH, PokemonNamesies.AEGISLASH);
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.STANCE_CHANGE);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.STANCE_CHANGE);

        // Should start in shield form
        assertStanceChangeForm(true, battle, attacking);
        assertStanceChangeForm(true, battle, defending);

        // Sunny Day is a status move, so it will remain in Shield Form
        // Stored Power is Special, so it will change to Blade Form
        battle.fight(AttackNamesies.SUNNY_DAY, AttackNamesies.STORED_POWER);
        assertStanceChangeForm(true, battle, attacking);
        assertStanceChangeForm(false, battle, defending);
        attacking.assertNotFullHealth();
        defending.assertFullHealth();

        // Scary Face is a status move, but only King's Shield will revert back to Shield Forme
        battle.fight(AttackNamesies.SYNTHESIS, AttackNamesies.SCARY_FACE);
        assertStanceChangeForm(true, battle, attacking);
        assertStanceChangeForm(false, battle, defending);
        attacking.assertFullHealth();
        defending.assertFullHealth();
        attacking.assertStages(new TestStages().set(-2, Stat.SPEED));
        defending.assertNoStages();

        // King's Shield has priority and revert to Shield Forme as well as protect from Absorb
        // Absorb changes to Blade Forme, but because it does not make contact, attack will not decrease
        battle.fight(AttackNamesies.ABSORB, AttackNamesies.KINGS_SHIELD);
        assertStanceChangeForm(false, battle, attacking);
        assertStanceChangeForm(true, battle, defending);
        attacking.assertFullHealth();
        defending.assertFullHealth();
        attacking.assertStages(new TestStages().set(-2, Stat.SPEED));
        defending.assertNoStages();

        // Peck will make contact and reduce defending's attack by 2
        battle.fight(AttackNamesies.KINGS_SHIELD, AttackNamesies.PECK);
        assertStanceChangeForm(true, battle, attacking);
        assertStanceChangeForm(false, battle, defending);
        attacking.assertFullHealth();
        defending.assertFullHealth();
        attacking.assertStages(new TestStages().set(-2, Stat.SPEED));
        defending.assertStages(new TestStages().set(-2, Stat.ATTACK));
    }

    private void assertStanceChangeForm(boolean shieldForm, TestBattle battle, TestPokemon stanceChanger) {
        if (shieldForm) {
            TestUtils.assertGreater("", stanceChanger.getStat(battle, Stat.DEFENSE), stanceChanger.getStat(battle, Stat.ATTACK));
        } else {
            TestUtils.assertGreater("", stanceChanger.getStat(battle, Stat.ATTACK), stanceChanger.getStat(battle, Stat.DEFENSE));
        }
    }

    @Test
    public void dancerTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.JOLTEON, PokemonNamesies.SANDSHREW);
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.DANCER);
        TestPokemon defending = battle.getDefending();

        // Does not trigger if only the attacker is the dancer
        battle.attackingFight(AttackNamesies.SWORDS_DANCE);
        attacking.assertStages(new TestStages().set(2, Stat.ATTACK));
        defending.assertNoStages();

        // Defending will user Swords Dance and then attacking will repeat using dancer
        battle.defendingFight(AttackNamesies.SWORDS_DANCE);
        attacking.assertStages(new TestStages().set(4, Stat.ATTACK));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK));

        battle.clearAllEffects();
        attacking.assertNoStages();
        defending.assertNoStages();

        // When both have Dancer they do not continuously dance forever
        defending.withAbility(AbilityNamesies.DANCER);
        battle.attackingFight(AttackNamesies.SWORDS_DANCE);
        attacking.assertStages(new TestStages().set(2, Stat.ATTACK));
        defending.assertStages(new TestStages().set(2, Stat.ATTACK));

        // Revelation Dance will fail (electric against ground) so it will not be repeated in the dance
        battle.attackingFight(AttackNamesies.REVELATION_DANCE);
        attacking.assertFullHealth();
        defending.assertFullHealth();

        // Feather Dance reduces attack by 2
        battle.attackingFight(AttackNamesies.FEATHER_DANCE);
        attacking.assertNoStages();
        defending.assertNoStages();

        // Magic Bounce will reflect the Feather Dance back on the Dancer,
        // but it will NOT count as the opponent using a dance move
        defending.withAbility(AbilityNamesies.MAGIC_BOUNCE);
        battle.attackingFight(AttackNamesies.FEATHER_DANCE);
        attacking.assertStages(new TestStages().set(-2, Stat.ATTACK));
        defending.assertNoStages();

        battle.attackingFight(AttackNamesies.SWORDS_DANCE);
        attacking.assertNoStages();
        defending.assertNoStages();

        // Defending will use Feather Dance normally
        // Dancer will then copy Feather Dance
        // Magic Bounce will then reflect that
        battle.defendingFight(AttackNamesies.FEATHER_DANCE);
        attacking.assertStages(new TestStages().set(-4, Stat.ATTACK));
        defending.assertNoStages();

        battle.clearAllEffects();

        // If the Dancer is under the effects of Taunt, then it won't be able to copy a status dance move
        defending.withAbility(AbilityNamesies.DANCER);
        battle.defendingFight(AttackNamesies.TAUNT);
        battle.defendingFight(AttackNamesies.SWORDS_DANCE);
        attacking.assertHasEffect(PokemonEffectNamesies.TAUNT);
        attacking.assertNoStages();
        defending.assertStages(new TestStages().set(2, Stat.ATTACK));

        battle.clearAllEffects();

        // Attacking will use Snatch
        // Defending will use Swords Dance
        // Attacking will snatch it
        // Dancer will then copy Swords Dance
        // Note: I don't think this is actually the intended behavior
        battle.fight(AttackNamesies.SNATCH, AttackNamesies.SWORDS_DANCE);
        attacking.assertStages(new TestStages().set(4, Stat.ATTACK));
        defending.assertNoStages();

        // TODO: Petal Dance and Lunar Dance
    }

    @Test
    public void sturdyTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.STURDY);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.STURDY);

        // Make sure they actually die for moves that cause the user to faint
        battle.defendingFight(AttackNamesies.EXPLOSION);
        Assert.assertFalse(attacking.isFainted(battle));
        Assert.assertTrue(attacking.getHP() > 0);
        attacking.assertNotFullHealth();
        Assert.assertTrue(defending.isFainted(battle));
    }

    @Test
    public void magicGuardTest() {
        // Make sure they actually die for moves that cause the user to faint (no difference with magic guard)
        magicGuardTest(
                new TestInfo().fight(AttackNamesies.ENDURE, AttackNamesies.EXPLOSION),
                (battle, attacking, defending) -> {
                    Assert.assertFalse(attacking.isFainted(battle));
                    Assert.assertTrue(attacking.getHP() > 0);
                    attacking.assertNotFullHealth();
                    Assert.assertTrue(defending.isFainted(battle));
                }
        );

        // Magic Guard does not protect against struggle
        magicGuardTest(
                new TestInfo().fight(AttackNamesies.ENDURE, AttackNamesies.STRUGGLE),
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertHealthRatio(.75);
                }
        );

        // But it does work against other recoil moves
        magicGuardTest(
                new TestInfo().fight(AttackNamesies.ENDURE, AttackNamesies.TAKE_DOWN),
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertNotFullHealth();
                },
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertFullHealth();
                }
        );

        // Magic Guard takes no poison damage (but will still be considered poisoned)
        magicGuardTest(
                new TestInfo().attackingFight(AttackNamesies.TOXIC),
                (battle, attacking, defending) -> {
                    attacking.assertFullHealth();
                    defending.assertHealthRatio(15/16.0);
                    defending.assertBadPoison();
                },
                (battle, attacking, defending) -> {
                    attacking.assertFullHealth();
                    defending.assertFullHealth();
                    defending.assertBadPoison();
                }
        );

        // Magic Guard prevents leech seed sucking
        // Make sure it does not heal the sucker
        magicGuardTest(
                new TestInfo().fight(AttackNamesies.LEECH_SEED, AttackNamesies.SUPER_FANG),
                (battle, attacking, defending) -> {
                    TestUtils.assertGreater(attacking.getHpString(), attacking.getHPRatio(), .5);
                    defending.assertHealthRatio(7/8.0);
                    defending.assertHasEffect(PokemonEffectNamesies.LEECH_SEED);
                },
                (battle, attacking, defending) -> {
                    attacking.assertHealthRatio(.5, 1);
                    defending.assertFullHealth();
                    defending.assertHasEffect(PokemonEffectNamesies.LEECH_SEED);
                }
        );

        // Should still take damage from Belly Drum
        magicGuardTest(
                new TestInfo().fight(AttackNamesies.GROWL, AttackNamesies.BELLY_DRUM),
                (battle, attacking, defending) -> {
                    attacking.assertFullHealth();
                    defending.assertHealthRatio(.5);
                    attacking.assertNoStages();
                    defending.assertStages(new TestStages().set(Stat.MAX_STAT_CHANGES, Stat.ATTACK));
                }
        );

        // Should take damage from using curse as a ghost-type
        magicGuardTest(
                new TestInfo().defending(PokemonNamesies.GASTLY).defendingFight(AttackNamesies.CURSE),
                (battle, attacking, defending) -> {
                    attacking.assertHealthRatio(.75);
                    defending.assertHealthRatio(.5);
                    attacking.assertNoStages();
                    defending.assertNoStages();
                    attacking.assertHasEffect(PokemonEffectNamesies.CURSE);
                    defending.assertNoEffect(PokemonEffectNamesies.CURSE);
                }
        );

        // But shouldn't take damage from the curse
        magicGuardTest(
                new TestInfo().attacking(PokemonNamesies.GASTLY).attackingFight(AttackNamesies.CURSE),
                (battle, attacking, defending) -> {
                    attacking.assertHealthRatio(.5);
                    defending.assertHealthRatio(.75);
                    attacking.assertNoStages();
                    defending.assertNoStages();
                    attacking.assertNoEffect(PokemonEffectNamesies.CURSE);
                    defending.assertHasEffect(PokemonEffectNamesies.CURSE);
                },
                (battle, attacking, defending) -> {
                    attacking.assertHealthRatio(.5);
                    defending.assertHealthRatio(1);
                    attacking.assertNoStages();
                    defending.assertNoStages();
                    attacking.assertNoEffect(PokemonEffectNamesies.CURSE);
                    defending.assertHasEffect(PokemonEffectNamesies.CURSE);
                }
        );

        // Should take no damage from sandstorm
        magicGuardTest(
                new TestInfo().attackingFight(AttackNamesies.SANDSTORM),
                (battle, attacking, defending) -> {
                    attacking.assertHealthRatio(15/16.0);
                    defending.assertHealthRatio(15/16.0);
                    Assert.assertTrue(battle.hasEffect(WeatherNamesies.SANDSTORM));
                },
                (battle, attacking, defending) -> {
                    attacking.assertHealthRatio(15/16.0);
                    defending.assertFullHealth();
                    Assert.assertTrue(battle.hasEffect(WeatherNamesies.SANDSTORM));
                }
        );

        // Should not take damage from Life Orb
        magicGuardTest(
                new TestInfo().defending(ItemNamesies.LIFE_ORB)
                              .with((battle, attacking, defending) -> {
                                  defending.setExpectedDamageModifier(5324.0/4096.0);
                                  battle.fight(AttackNamesies.ENDURE, AttackNamesies.TACKLE);
                              }),
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertHealthRatio(.9);
                },
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertFullHealth();
                }
        );

        // Should not take damage from Mind Blown
        magicGuardTest(
                new TestInfo().fight(AttackNamesies.ENDURE, AttackNamesies.MIND_BLOWN),
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertHealthRatio(.5);
                },
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertFullHealth();
                }
        );

        // Make sure Magic Guardian still takes direct damage from sap health moves and leech face leeches life
        magicGuardTest(
                new TestInfo().with((battle, attacking, defending) -> {
                    battle.attackingFight(AttackNamesies.BELLY_DRUM);
                    attacking.assertHealthRatio(.5);
                    defending.assertFullHealth();

                    battle.fight(AttackNamesies.ABSORB, AttackNamesies.ENDURE);
                }),
                (battle, attacking, defending) -> {
                    TestUtils.assertGreater(attacking.getHpString(), attacking.getHPRatio(), .5);
                    defending.assertNotFullHealth();
                }
        );

        // Don't lose health from Liquid Ooze (I guess? can't find anything to the contrary...)
        // But don't gain health either
        magicGuardTest(
                new TestInfo().attacking(AbilityNamesies.LIQUID_OOZE)
                              .with((battle, attacking, defending) -> {
                                  battle.defendingFight(AttackNamesies.BELLY_DRUM);
                                  attacking.assertFullHealth();
                                  defending.assertHealthRatio(.5);

                                  battle.fight(AttackNamesies.ENDURE, AttackNamesies.ABSORB);
                              }),
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    TestUtils.assertGreater(defending.getHpString(), .5, defending.getHPRatio());
                },
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertHealthRatio(.5);
                }
        );
    }

    // For when the result is the same with or without magic guard
    private void magicGuardTest(TestInfo setup, PokemonManipulator samesies) {
        setup.doubleTakeSamesies(AbilityNamesies.MAGIC_GUARD, samesies);
    }

    private void magicGuardTest(TestInfo setup, PokemonManipulator withoutMagicGuard, PokemonManipulator withMagicGuard) {
        setup.doubleTake(AbilityNamesies.MAGIC_GUARD, withoutMagicGuard, withMagicGuard);
    }

    @Test
    public void flashFireTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.SQUIRTLE, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.FLASH_FIRE);
        TestPokemon defending = battle.getDefending();

        // Unactivated Fire move
        attacking.setupMove(AttackNamesies.EMBER, battle);
        TestUtils.assertEquals(1, battle.getDamageModifier(attacking, defending));

        // Activate Flash Fire
        battle.defendingFight(AttackNamesies.EMBER);

        // Activated non-Fire move
        attacking.setupMove(AttackNamesies.SURF, battle);
        TestUtils.assertEquals(1, battle.getDamageModifier(attacking, defending));

        // Activated Fire move
        attacking.setupMove(AttackNamesies.EMBER, battle);
        TestUtils.assertEquals(1.5, battle.getDamageModifier(attacking, defending));
    }

    @Test
    public void shieldDustTest() {
        // Inferno has a 100% chance to burn
        // But if the opponent has Shield Dust, then it is actually ZERO PERCENT
        new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                .with((battle, attacking, defending) -> attacking.setExpectedDamageModifier(1.0))
                .attackingFight(AttackNamesies.INFERNO)
                .doubleTake(
                        AbilityNamesies.SHIELD_DUST,
                        (battle, attacking, defending) -> defending.assertHasStatus(StatusNamesies.BURNED),
                        (battle, attacking, defending) -> defending.assertNoStatus()
                );
    }

    @Test
    public void fluffyTest() {
        fluffyTest(AttackNamesies.EMBER, 2);
        fluffyTest(AttackNamesies.FIRE_FANG, 1);
        fluffyTest(AttackNamesies.TACKLE, .5);
        fluffyTest(AttackNamesies.TACKLE, 1, PokemonManipulator.giveAttackingAbility(AbilityNamesies.LONG_REACH));
        fluffyTest(AttackNamesies.FIRE_FANG, 2, PokemonManipulator.giveAttackingAbility(AbilityNamesies.LONG_REACH));
    }

    private void fluffyTest(AttackNamesies attackNamesies, double expectedModifier) {
        fluffyTest(attackNamesies, expectedModifier, PokemonManipulator.empty());
    }

    private void fluffyTest(AttackNamesies attackNamesies, double expectedModifier, PokemonManipulator manipulator) {
        TestBattle battle = new TestInfo(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE).createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Confirm no modifier without Fluffy
        defending.assertAbility(AbilityNamesies.NO_ABILITY);
        manipulator.manipulate(battle);
        attacking.setExpectedDamageModifier(1.0);
        battle.attackingFight(attackNamesies);

        battle.emptyHeal();
        battle.clearAllEffects();

        // Confirm modifier with Fluffy
        defending.setAbility(AbilityNamesies.FLUFFY);
        manipulator.manipulate(battle);
        attacking.setExpectedDamageModifier(expectedModifier);
        battle.attackingFight(attackNamesies);
    }

    @Test
    public void innardsOutTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.GUZZLORD, PokemonNamesies.DIGLETT);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.INNARDS_OUT);

        // Innards Out will deal damage equal to their last HP when killed via direct damage
        // From full HP -- reduce max HP
        battle.attackingFight(AttackNamesies.CRUNCH);
        attacking.assertMissingHp(defending.getMaxHP());
        defending.assertHasStatus(StatusNamesies.FAINTED);

        battle.emptyHeal();

        // Set HP to 1 then murder -- should only take one
        battle.falseSwipePalooza(true);
        attacking.assertFullHealth();
        Assert.assertEquals(1, defending.getHP());
        battle.attackingFight(AttackNamesies.CRUNCH);
        attacking.assertMissingHp(1);
        defending.assertHasStatus(StatusNamesies.FAINTED);

        battle.emptyHeal();

        // Should still take innards out damage if they die from fixed damage
        battle.falseSwipePalooza(true);
        attacking.assertFullHealth();
        Assert.assertEquals(1, defending.getHP());
        battle.attackingFight(AttackNamesies.DRAGON_RAGE);
        attacking.assertMissingHp(1);
        defending.assertHasStatus(StatusNamesies.FAINTED);

        battle.emptyHeal();

        // But not if they die from indirect damage like burn
        battle.falseSwipePalooza(true);
        attacking.assertFullHealth();
        Assert.assertEquals(1, defending.getHP());
        battle.attackingFight(AttackNamesies.WILL_O_WISP);
        attacking.assertFullHealth();
        defending.assertHasStatus(StatusNamesies.FAINTED);
    }

    @Test
    public void suppressAbilityTest() {
        // Gastro Acid will remove Levitate and allow Earthquake to work
        suppressAbilityTest(true, (battle, attacking, defending) -> battle.attackingFight(AttackNamesies.GASTRO_ACID));

        // Core Enforcer (when it attacks second) has the same effect as Gastro Acid
        // (Endure has priority so it goes second here)
        // Btw we need to do all this healing crap to confirm full health (did not get hit by Earthquake etc)
        suppressAbilityTest(true, (battle, attacking, defending) -> {
            defending.withItem(ItemNamesies.CHESTO_BERRY);
            battle.fight(AttackNamesies.CORE_ENFORCER, AttackNamesies.ENDURE);
            defending.assertNotFullHealth();
            battle.defendingFight(AttackNamesies.REST);
            defending.assertFullHealth();
            defending.assertNoStatus();
            defending.assertConsumedBerry(battle);
        });

        // When Core Enforcer goes first, it should fail at suppressing
        suppressAbilityTest(false, (battle, attacking, defending) -> {
            defending.withItem(ItemNamesies.CHESTO_BERRY);
            battle.attackingFight(AttackNamesies.CORE_ENFORCER);
            defending.assertNotFullHealth();
            battle.defendingFight(AttackNamesies.REST);
            defending.assertFullHealth();
            defending.assertNoStatus();
            defending.assertConsumedBerry(battle);
        });
    }

    private void suppressAbilityTest(boolean shouldSuppress, PokemonManipulator suppress) {
        // Suppressed ability will make Earthquake hit a Pokemon with levitate
        suppressAbilityTest(
                shouldSuppress, suppress,
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        // Adds Levitate at the beginning and Earthquake will happen after suppression
                        .setup((battle, attacking, defending) -> defending.withAbility(AbilityNamesies.LEVITATE))
                        .attackingFight(AttackNamesies.EARTHQUAKE),
                (battle, attacking, defending) -> {
                    defending.assertAbility(AbilityNamesies.LEVITATE);
                    defending.assertFullHealth();
                },
                (battle, attacking, defending) -> {
                    defending.assertChangedAbility(AbilityNamesies.NO_ABILITY);
                    defending.assertNotFullHealth();
                }
        );

        // Cannot suppress irreplaceable abilities (like Multitype)
        new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.ARCEUS)
                .setup((battle, attacking, defending) -> defending.withAbility(AbilityNamesies.MULTITYPE))
                .with((battle, attacking, defending) -> {
                    defending.assertAbility(AbilityNamesies.MULTITYPE);
                    Assert.assertFalse(defending.getAbility().isReplaceable());
                })
                .doubleTakeSamesies(
                        suppress,
                        (battle, attacking, defending) -> defending.assertAbility(AbilityNamesies.MULTITYPE)
                );
    }

    private void suppressAbilityTest(boolean shouldSuppress, PokemonManipulator suppress, TestInfo testInfo, PokemonManipulator withAbility, PokemonManipulator suppressedAbility) {
        if (shouldSuppress) {
            testInfo.doubleTake(suppress, withAbility, suppressedAbility);
        } else {
            testInfo.doubleTakeSamesies(suppress, withAbility);
        }
    }

    @Test
    public void magicBounceTest() {
        // Growl decreases opponent's attack by 1 and is reflectable
        magicBounceTest(
                AttackNamesies.GROWL,
                new TestInfo(),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-1, Stat.ATTACK));
                },
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages().set(-1, Stat.ATTACK));
                    defending.assertStages(new TestStages());
                }
        );

        // Status-inducing moves are reflectable
        magicBounceTest(
                AttackNamesies.WILL_O_WISP,
                new TestInfo(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE),
                (battle, attacking, defending) -> {
                    attacking.assertNoStatus();
                    defending.assertHasStatus(StatusNamesies.BURNED);
                },
                (battle, attacking, defending) -> {
                    attacking.assertHasStatus(StatusNamesies.BURNED);
                    defending.assertNoStatus();
                }
        );

        // Gastro Acid negates opponent's ability and is reflectable
        // Overgrow is replaceable and will be negated when reflected
        magicBounceTest(
                AttackNamesies.GASTRO_ACID,
                new TestInfo().attacking(AbilityNamesies.OVERGROW),
                (battle, attacking, defending) -> {
                    attacking.assertAbility(AbilityNamesies.OVERGROW);
                    defending.assertChangedAbility(AbilityNamesies.NO_ABILITY);
                    Assert.assertTrue(attacking.lastMoveSucceeded());
                },
                (battle, attacking, defending) -> {
                    attacking.assertChangedAbility(AbilityNamesies.NO_ABILITY);
                    defending.assertAbility(AbilityNamesies.MAGIC_BOUNCE);
                    Assert.assertTrue(attacking.lastMoveSucceeded());
                }
        );

        // Gastro Acid negates opponent's ability and is reflectable
        // RKS System is not replacable and should fail when reflected
        magicBounceTest(
                AttackNamesies.GASTRO_ACID,
                new TestInfo().attacking(AbilityNamesies.RKS_SYSTEM),
                (battle, attacking, defending) -> {
                    attacking.assertAbility(AbilityNamesies.RKS_SYSTEM);
                    defending.assertChangedAbility(AbilityNamesies.NO_ABILITY);
                    Assert.assertTrue(attacking.lastMoveSucceeded());
                },
                (battle, attacking, defending) -> {
                    attacking.assertAbility(AbilityNamesies.RKS_SYSTEM);
                    defending.assertAbility(AbilityNamesies.MAGIC_BOUNCE);
                    Assert.assertFalse(attacking.lastMoveSucceeded());
                }
        );

        // Heal Pulse is reflectable, but nothing happens when both at full health
        magicBounceTest(
                AttackNamesies.HEAL_PULSE,
                new TestInfo(),
                (battle, attacking, defending) -> Assert.assertFalse(attacking.lastMoveSucceeded())
        );

        // Heal Pulse with enemy not at full
        magicBounceTest(
                AttackNamesies.HEAL_PULSE,
                new TestInfo().attackingFight(AttackNamesies.FALSE_SWIPE)
                              .with((battle, attacking, defending) -> {
                                  attacking.assertFullHealth();
                                  defending.assertNotFullHealth();
                              }),
                (battle, attacking, defending) -> {
                    // Use the move as expected
                    attacking.assertFullHealth();
                    defending.assertFullHealth();
                    Assert.assertTrue(attacking.lastMoveSucceeded());
                },
                (battle, attacking, defending) -> {
                    // Fails when reflected because user has full health
                    attacking.assertFullHealth();
                    defending.assertNotFullHealth();
                    Assert.assertFalse(attacking.lastMoveSucceeded());
                }
        );

        // Heal Pulse with user not at full
        magicBounceTest(
                AttackNamesies.HEAL_PULSE,
                new TestInfo().defendingFight(AttackNamesies.FALSE_SWIPE)
                              .with((battle, attacking, defending) -> {
                                  attacking.assertNotFullHealth();
                                  defending.assertFullHealth();
                              }),
                (battle, attacking, defending) -> {
                    // Fails because defending has full health
                    attacking.assertNotFullHealth();
                    defending.assertFullHealth();
                    Assert.assertFalse(attacking.lastMoveSucceeded());
                },
                (battle, attacking, defending) -> {
                    // Even though defending is full, is reflected so uses attacking's health (and then healing it)
                    attacking.assertFullHealth();
                    defending.assertFullHealth();
                    Assert.assertTrue(attacking.lastMoveSucceeded());
                }
        );

        // Not 100% sure this is how it would work, but I assume Captivate always fails when reflected since it will
        // never be the opposite gender
        magicBounceTest(
                AttackNamesies.CAPTIVATE,
                new TestInfo(PokemonNamesies.JYNX, PokemonNamesies.HITMONLEE)
                        .with((battle, attacking, defending) -> Assert.assertTrue(Gender.oppositeGenders(attacking, defending))),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-2, Stat.SP_ATTACK));
                },
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages());
                    Assert.assertFalse(attacking.lastMoveSucceeded());
                }
        );
    }

    private void magicBounceTest(AttackNamesies attack, TestInfo testInfo, PokemonManipulator samesies) {
        magicBounceTest(attack, testInfo, samesies, samesies);
    }

    private void magicBounceTest(AttackNamesies attackNamesies, TestInfo testInfo, PokemonManipulator withoutBounce, PokemonManipulator withBounce) {
        testInfo.attackingFight(attackNamesies);
        testInfo.doubleTake(AbilityNamesies.MAGIC_BOUNCE, withoutBounce, withBounce);
    }
}
