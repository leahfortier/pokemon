package test.battle;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.EffectInterfaces.ItemListHolder;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffectNamesies;
import battle.stages.Stages;
import item.ItemNamesies;
import item.bag.Bag;
import item.hold.HoldItem;
import main.Game;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.active.StatValues;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import test.battle.manipulator.PokemonManipulator;
import test.battle.manipulator.TestInfo;
import test.general.BaseTest;
import test.general.TestUtils;
import test.pokemon.TestPokemon;
import trainer.player.Player;
import type.Type;
import type.TypeAdvantage;

import java.util.List;

public class AbilityTest extends BaseTest {
    @Test
    public void descriptionTest() {
        for (AbilityNamesies abilityNamesies : AbilityNamesies.values()) {
            if (abilityNamesies == AbilityNamesies.NO_ABILITY) {
                continue;
            }

            // Make sure all descriptions start capitalized, end with a period, and only contain valid characters
            Ability ability = abilityNamesies.getNewAbility();
            String description = ability.getDescription();
            TestUtils.assertDescription(ability.getName(), description, "[A-Z][a-zA-Z.,'é\"\\- ]+[.]");
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

        defending.assertAbility(AbilityNamesies.COLOR_CHANGE);
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
        checkPriorityPrevention(1, false, AttackNamesies.BIDE);
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
        Assert.assertEquals(beforePriority, attacking.getAttackPriority());
        attacking.apply(beforePriority <= 0 || attacking.getAttack().isSelfTargetStatusMove(), attack, battle);

        battle.emptyHeal();
        manipulator.manipulate(battle);

        attacking.setupMove(attack, battle);
        Assert.assertEquals(afterPriority, attacking.getAttackPriority());
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
        defending.assertConsumedBerry();

        // Simple doubles stat modifications to itself -- shouldn't affect contrary pokemon
        battle.fight(AttackNamesies.HAZE, AttackNamesies.SIMPLE_BEAM);
        attacking.assertChangedAbility(AbilityNamesies.SIMPLE);
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
        attacking.assertLastMoveSucceeded(false);
        defending.assertLastMoveSucceeded(true);
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
        attacking.assertChangedAbility(AbilityNamesies.CONTRARY);
        defending.assertChangedAbility(AbilityNamesies.STURDY);
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
        TestPokemon defending2 = battle.addDefending(PokemonNamesies.SHUCKLE).withAbility(AbilityNamesies.MOLD_BREAKER);
        Assert.assertSame(battle.getDefending(), defending1);

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
        attacking.assertConsumedBerry();

        // Switch opponent Pokemon so I can use Fake Out again
        // NOTE: We need to kill the defending instead of something like whirlwind otherwise Fake Out won't work
        // ANOTHER NOTE: Everything feels a little backwards with attacking and defending in this test
        // because this does not appropriately swap if the player is killed
        battle.attackingFight(AttackNamesies.FISSURE);
        Assert.assertSame(battle.getDefending(), defending2);
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
        attacking.assertNotHoldingItem();
        defending.assertNotHoldingItem();

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
        attacking.assertNotHoldingItem();
        defending.assertNotHoldingItem();

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
        attacking.assertNotHoldingItem();
        defending.assertNotHoldingItem();
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
        defending.assertConsumedItem();

        battle.emptyHeal();
        battle.clearAllEffects();

        // Give a Rawst Berry and then burn the target
        // Rawst Berry will heal its burn and then be consumed
        // Harvest will then restore the berry since the sunlight is strong
        defending.giveItem(ItemNamesies.RAWST_BERRY);
        battle.fight(AttackNamesies.WILL_O_WISP, AttackNamesies.SUNNY_DAY);
        defending.assertNoStatus();
        defending.assertHoldingItem(ItemNamesies.RAWST_BERRY);
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

        // Peck will make contact and reduce defending's attack by 1
        battle.fight(AttackNamesies.KINGS_SHIELD, AttackNamesies.PECK);
        assertStanceChangeForm(true, battle, attacking);
        assertStanceChangeForm(false, battle, defending);
        attacking.assertFullHealth();
        defending.assertFullHealth();
        attacking.assertStages(new TestStages().set(-2, Stat.SPEED));
        defending.assertStages(new TestStages().set(-1, Stat.ATTACK));
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

        // Attacking will snatch the Swords Dance, but defending will not activate Dancer from it
        battle.fight(AttackNamesies.SNATCH, AttackNamesies.SWORDS_DANCE);
        attacking.assertStages(new TestStages().set(2, Stat.ATTACK));
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
                    defending.assertStages(new TestStages().set(Stages.MAX_STAT_CHANGES, Stat.ATTACK));
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
                new TestInfo().attackingFight(AttackNamesies.SANDSTORM)
                              .with((battle, attacking, defending) -> {
                                  attacking.assertHealthRatio(15/16.0);
                                  battle.assertHasEffect(WeatherNamesies.SANDSTORM);
                              }),
                (battle, attacking, defending) -> defending.assertHealthRatio(15/16.0),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Should not take damage from Life Orb
        magicGuardTest(
                new TestInfo().defending(ItemNamesies.LIFE_ORB)
                              .with((battle, attacking, defending) -> {
                                  defending.setExpectedDamageModifier(5324.0/4096.0);
                                  battle.fight(AttackNamesies.ENDURE, AttackNamesies.TACKLE);

                                  attacking.assertNotFullHealth();
                              }),
                (battle, attacking, defending) -> defending.assertHealthRatio(.9),
                (battle, attacking, defending) -> defending.assertFullHealth()
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

                                  attacking.assertNotFullHealth();
                              }),
                (battle, attacking, defending) -> TestUtils.assertGreater(defending.getHpString(), .5, defending.getHPRatio()),
                (battle, attacking, defending) -> defending.assertHealthRatio(.5)
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
        defending.assertDead();

        battle.emptyHeal();

        // Set HP to 1 then murder -- should only take one
        battle.falseSwipePalooza(true);
        attacking.assertFullHealth();
        defending.assertHp(1);
        battle.attackingFight(AttackNamesies.CRUNCH);
        attacking.assertMissingHp(1);
        defending.assertDead();

        battle.emptyHeal();

        // Should still take innards out damage if they die from fixed damage
        battle.falseSwipePalooza(true);
        attacking.assertFullHealth();
        defending.assertHp(1);
        battle.attackingFight(AttackNamesies.DRAGON_RAGE);
        attacking.assertMissingHp(1);
        defending.assertDead();

        battle.emptyHeal();

        // But not if they die from indirect damage like burn
        battle.falseSwipePalooza(true);
        attacking.assertFullHealth();
        defending.assertHp(1);
        battle.attackingFight(AttackNamesies.WILL_O_WISP);
        attacking.assertFullHealth();
        defending.assertDead();
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
            defending.assertConsumedBerry();
        });

        // When Core Enforcer goes first, it should fail at suppressing
        suppressAbilityTest(false, (battle, attacking, defending) -> {
            defending.withItem(ItemNamesies.CHESTO_BERRY);
            battle.attackingFight(AttackNamesies.CORE_ENFORCER);
            defending.assertNotFullHealth();
            battle.defendingFight(AttackNamesies.REST);
            defending.assertFullHealth();
            defending.assertNoStatus();
            defending.assertConsumedBerry();
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

        // Mold Breaker cancels Magic Bounce
        magicBounceTest(
                AttackNamesies.GROWL,
                new TestInfo().attacking(AbilityNamesies.MOLD_BREAKER),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-1, Stat.ATTACK));
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
                    attacking.assertLastMoveSucceeded(true);
                },
                (battle, attacking, defending) -> {
                    attacking.assertChangedAbility(AbilityNamesies.NO_ABILITY);
                    defending.assertAbility(AbilityNamesies.MAGIC_BOUNCE);
                    attacking.assertLastMoveSucceeded(true);
                }
        );

        // Gastro Acid negates opponent's ability and is reflectable
        // RKS System is not replaceable and should fail when reflected
        magicBounceTest(
                AttackNamesies.GASTRO_ACID,
                new TestInfo().attacking(AbilityNamesies.RKS_SYSTEM),
                (battle, attacking, defending) -> {
                    attacking.assertAbility(AbilityNamesies.RKS_SYSTEM);
                    defending.assertChangedAbility(AbilityNamesies.NO_ABILITY);
                    attacking.assertLastMoveSucceeded(true);
                },
                (battle, attacking, defending) -> {
                    attacking.assertAbility(AbilityNamesies.RKS_SYSTEM);
                    defending.assertAbility(AbilityNamesies.MAGIC_BOUNCE);
                    attacking.assertLastMoveSucceeded(false);
                }
        );

        // Heal Pulse is reflectable, but nothing happens when both at full health
        magicBounceTest(
                AttackNamesies.HEAL_PULSE, new TestInfo(),
                (battle, attacking, defending) -> attacking.assertLastMoveSucceeded(false)
        );

        // Heal Pulse with enemy not at full
        magicBounceTest(
                AttackNamesies.HEAL_PULSE,
                new TestInfo().falseSwipePalooza(true)
                              .with((battle, attacking, defending) -> {
                                  attacking.assertFullHealth();
                                  defending.assertHp(1);
                              }),
                (battle, attacking, defending) -> {
                    // Use the move as expected
                    attacking.assertFullHealth();
                    defending.assertHealthRatioDiff(1, -.5);
                    attacking.assertLastMoveSucceeded(true);
                },
                (battle, attacking, defending) -> {
                    // Fails when reflected because user has full health
                    attacking.assertFullHealth();
                    defending.assertHp(1);
                    attacking.assertLastMoveSucceeded(false);
                }
        );

        // Heal Pulse with user not at full
        magicBounceTest(
                AttackNamesies.HEAL_PULSE,
                new TestInfo().falseSwipePalooza(false)
                              .with((battle, attacking, defending) -> {
                                  attacking.assertHp(1);
                                  defending.assertFullHealth();
                              }),
                (battle, attacking, defending) -> {
                    // Fails because defending has full health
                    attacking.assertHp(1);
                    defending.assertFullHealth();
                    attacking.assertLastMoveSucceeded(false);
                },
                (battle, attacking, defending) -> {
                    // Even though defending is full, is reflected so uses attacking's health (and then healing it)
                    attacking.assertHealthRatioDiff(1, -.5);
                    defending.assertFullHealth();
                    attacking.assertLastMoveSucceeded(true);
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
                    attacking.assertLastMoveSucceeded(false);
                }
        );

        // Magic Bounce does not work with ability decreases
        magicBounceTest(
                new TestInfo().attacking(AbilityNamesies.GOOEY).defendingFight(AttackNamesies.FALSE_SWIPE),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-1, Stat.SPEED));
                }
        );

        // Magic Bounce only reflects status moves, so decreases from Acid Spray will not reflect
        magicBounceTest(
                new TestInfo().fight(AttackNamesies.ACID_SPRAY, AttackNamesies.ENDURE),
                (battle, attacking, defending) -> {
                    attacking.assertFullHealth();
                    defending.assertNotFullHealth();
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-2, Stat.SP_DEFENSE));
                }
        );
    }

    private void magicBounceTest(AttackNamesies attack, TestInfo testInfo, PokemonManipulator samesies) {
        magicBounceTest(attack, testInfo, samesies, samesies);
    }

    private void magicBounceTest(TestInfo testInfo, PokemonManipulator samesies) {
        magicBounceTest(testInfo, samesies, samesies);
    }

    private void magicBounceTest(AttackNamesies attackNamesies, TestInfo testInfo, PokemonManipulator withoutBounce, PokemonManipulator withBounce) {
        magicBounceTest(testInfo.attackingFight(attackNamesies), withoutBounce, withBounce);
    }

    private void magicBounceTest(TestInfo testInfo, PokemonManipulator withoutBounce, PokemonManipulator withBounce) {
        testInfo.doubleTake(AbilityNamesies.MAGIC_BOUNCE, withoutBounce, withBounce);
    }

    @Test
    public void mirrorArmorTest() {
        // Growl decreases opponent's attack by 1 and is reflectable
        mirrorArmorTest(
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

        // Unless user has Mold Breaker
        mirrorArmorTest(
                AttackNamesies.GROWL,
                new TestInfo().attacking(AbilityNamesies.MOLD_BREAKER),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-1, Stat.ATTACK));
                }
        );

        // Hyper Cutter prevents attack reduction
        mirrorArmorTest(
                AttackNamesies.GROWL,
                new TestInfo().attacking(AbilityNamesies.HYPER_CUTTER),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-1, Stat.ATTACK));
                },
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages());
                }
        );

        // Competitive sharply raises Sp. Attack when a stat is lowered (works with Mirror Armor)
        mirrorArmorTest(
                AttackNamesies.GROWL,
                new TestInfo().attacking(AbilityNamesies.COMPETITIVE),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-1, Stat.ATTACK));
                },
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages().set(-1, Stat.ATTACK).set(2, Stat.SP_ATTACK));
                    defending.assertStages(new TestStages());
                }
        );

        // Mirror Armor even works against ability decreases
        // Gooey decreases the Speed of Pokemon who make contact with it
        mirrorArmorTest(
                new TestInfo().attacking(AbilityNamesies.GOOEY).defendingFight(AttackNamesies.FALSE_SWIPE),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-1, Stat.SPEED));
                },
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages().set(-1, Stat.SPEED));
                    defending.assertStages(new TestStages());
                }
        );

        // Mirror Armor reflects damaging moves not just status ones (but only reflects stats)
        mirrorArmorTest(
                new TestInfo().fight(AttackNamesies.ACID_SPRAY, AttackNamesies.ENDURE)
                              .with((battle, attacking, defending) -> {
                                  attacking.assertFullHealth();
                                  defending.assertNotFullHealth();
                              }),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-2, Stat.SP_DEFENSE));
                },
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages().set(-2, Stat.SP_DEFENSE));
                    defending.assertStages(new TestStages());
                }
        );

        // Sticky Web is affected by Mirror Armor
        mirrorArmorTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .asTrainerBattle()
                        .with((battle, attacking, defending) -> {
                            TestPokemon attacking2 = battle.addDefending(PokemonNamesies.SQUIRTLE);
                            battle.assertFront(attacking);

                            // Switch to Squirtle and add Sticky Web
                            battle.attackingFight(AttackNamesies.WHIRLWIND);
                            battle.attackingFight(AttackNamesies.STICKY_WEB);
                            battle.assertFront(attacking2);

                            // Now switch baby to Bulby with Mirror Armor which should activate the web
                            battle.attackingFight(AttackNamesies.WHIRLWIND);
                            battle.assertFront(attacking);
                        }),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    defending.assertStages(new TestStages().set(-1, Stat.SPEED));
                },
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages().set(-1, Stat.SPEED));
                    defending.assertStages(new TestStages());
                }
        );

        // Status-inducing moves are not reflectable
        mirrorArmorTest(
                AttackNamesies.WILL_O_WISP,
                new TestInfo(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE),
                (battle, attacking, defending) -> {
                    attacking.assertNoStatus();
                    defending.assertHasStatus(StatusNamesies.BURNED);
                }
        );
    }

    private void mirrorArmorTest(AttackNamesies attack, TestInfo testInfo, PokemonManipulator samesies) {
        mirrorArmorTest(attack, testInfo, samesies, samesies);
    }

    private void mirrorArmorTest(AttackNamesies attackNamesies, TestInfo testInfo, PokemonManipulator withoutArmor, PokemonManipulator withArmor) {
        mirrorArmorTest(testInfo.attackingFight(attackNamesies), withoutArmor, withArmor);
    }

    private void mirrorArmorTest(TestInfo testInfo, PokemonManipulator withoutArmor, PokemonManipulator withArmor) {
        testInfo.doubleTake(AbilityNamesies.MIRROR_ARMOR, withoutArmor, withArmor);
    }

    @Test
    public void beastBoostTest() {
        // These Pokemon should always boost the specified stat when murdering
        beastBoostTest(PokemonNamesies.KARTANA, Stat.ATTACK);
        beastBoostTest(PokemonNamesies.STAKATAKA, Stat.DEFENSE);
        beastBoostTest(PokemonNamesies.XURKITREE, Stat.SP_ATTACK);
        beastBoostTest(PokemonNamesies.REGICE, Stat.SP_DEFENSE);
        beastBoostTest(PokemonNamesies.NINJASK, Stat.SPEED);

        // Make sure test still works with a Special Attack (should be irrelevant)
        beastBoostTest(PokemonNamesies.KARTANA, Stat.ATTACK, AttackNamesies.SWIFT);

        // Make sure boost does not occur when the target is knocked out by indirect damage (like burn)
        beastBoostTest(PokemonNamesies.KARTANA, Stat.ATTACK, AttackNamesies.WILL_O_WISP, new TestStages());

        // Beast Boost only looks at the base stats and should not take stages into account
        beastBoostTest(
                PokemonNamesies.BASTIODON, Stat.DEFENSE, AttackNamesies.TACKLE,
                new TestStages().set(6, Stat.SP_DEFENSE).set(-5, Stat.DEFENSE),
                (battle, attacking, defending) -> {
                    // Defense is naturally higher than Sp. Defense
                    int def = Stat.getStat(Stat.DEFENSE, attacking, defending, battle);
                    int spDef = Stat.getStat(Stat.SP_DEFENSE, attacking, defending, battle);
                    TestUtils.assertGreater(def, spDef);

                    // Maximize Sp. Defense and minimize Defense stages
                    battle.fight(AttackNamesies.AMNESIA, AttackNamesies.SCREECH);
                    battle.fight(AttackNamesies.AMNESIA, AttackNamesies.SCREECH);
                    battle.fight(AttackNamesies.AMNESIA, AttackNamesies.SCREECH);
                    attacking.assertStages(new TestStages().set(6, Stat.SP_DEFENSE).set(-6, Stat.DEFENSE));

                    // Now Sp. Defense should be higher (but Defense will still be increased by Beast Boost)
                    def = Stat.getStat(Stat.DEFENSE, attacking, defending, battle);
                    spDef = Stat.getStat(Stat.SP_DEFENSE, attacking, defending, battle);
                    TestUtils.assertGreater(spDef, def);
                }
        );
    }

    private void beastBoostTest(PokemonNamesies pokes, Stat increased) {
        beastBoostTest(pokes, increased, AttackNamesies.TACKLE);
    }

    private void beastBoostTest(PokemonNamesies pokes, Stat increased, AttackNamesies attack) {
        beastBoostTest(pokes, increased, attack, new TestStages().set(1, increased));
    }

    private void beastBoostTest(PokemonNamesies pokes, Stat highest, AttackNamesies attack, TestStages afterStages) {
        beastBoostTest(pokes, highest, attack, afterStages, PokemonManipulator.empty());
    }

    private void beastBoostTest(PokemonNamesies pokes, Stat highest, AttackNamesies attack, TestStages afterStages, PokemonManipulator extraPostSwipe) {
        deadStatBoostTest(
                pokes, AbilityNamesies.BEAST_BOOST, attack, afterStages,
                (battle, attacking, defending) -> {
                    // Test requires their highest stat is the specified one
                    assertHighestStat(attacking, highest);
                    attacking.assertStages(new TestStages());

                    extraPostSwipe.manipulate(battle);
                }
        );
    }

    // Asserts that the input stat is the Pokemon's highest ignoring HP
    // This is only checking the base stats and does not take stages or anything like that into consideration
    private void assertHighestStat(TestPokemon pokemon, Stat highest) {
        // These don't make sense here
        Assert.assertNotEquals(highest, Stat.HP);
        Assert.assertNotEquals(highest, Stat.ACCURACY);
        Assert.assertNotEquals(highest, Stat.EVASION);
        Assert.assertFalse(highest.isAccuracyStat());

        StatValues stats = pokemon.stats();
        String message = pokemon.statsString();

        int maxStat = stats.get(highest);
        for (Stat stat : Stat.STATS) {
            // Skip HP and max stat
            if (stat == Stat.HP || stat == highest) {
                continue;
            }

            // Max stat should be strictly larger than every other stat
            TestUtils.assertGreater(message, maxStat, stats.get(stat));
        }
    }

    @Test
    public void soulHeartTest() {
        // Receive boosts regardless of if the damage is direct or indirect
        soulHeartTest(AttackNamesies.TACKLE);
        soulHeartTest(AttackNamesies.WILL_O_WISP);
    }

    private void soulHeartTest(AttackNamesies killMove) {
        deadStatBoostTest(
                PokemonNamesies.MAGEARNA, AbilityNamesies.SOUL_HEART, killMove,
                new TestStages().set(1, Stat.SP_ATTACK),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages())
        );
    }

    private void deadStatBoostTest(PokemonNamesies pokes, AbilityNamesies ability, AttackNamesies killMove, TestStages afterStages, PokemonManipulator postSwipe) {
        TestBattle battle = TestBattle.createTrainerBattle(pokes, PokemonNamesies.CLEFFA);
        TestPokemon attacking = battle.getAttacking().withAbility(ability);
        TestPokemon defending1 = battle.getDefending().withAbility(AbilityNamesies.NO_ABILITY);
        TestPokemon defending2 = battle.addDefending(PokemonNamesies.IGGLYBUFF);
        Assert.assertSame(battle.getDefending(), defending1);

        // Set defending HP to 1 so it always dies to a single direct hit or to indirect damage such as burn
        battle.falseSwipePalooza(true);
        postSwipe.manipulate(battle);
        defending1.assertHp(1);

        // Knock the Pokemon out with the attack and check if stat was boosted or not
        battle.attackingFight(killMove);
        Assert.assertSame(battle.getDefending(), defending2);
        attacking.assertStages(afterStages);
    }

    @Test
    public void disguiseTest() {
        // Disguise will block the first direct hit of damage
        disguiseTest(
                AttackNamesies.TACKLE,
                (battle, attacking, defending) -> defending.assertNotFullHealth(),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Disguise does not block indirect damage such as poison
        disguiseTest(
                AttackNamesies.TOXIC,
                (battle, attacking, defending) -> {
                    defending.assertBadPoison();
                    defending.assertHealthRatio(15/16.0);
                }
        );

        // Disguise only blocks the first direct hit of damage, so the second one will not be absorbed
        disguiseTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .attackingFight(AttackNamesies.TACKLE)
                        .attackingFight(AttackNamesies.TACKLE),
                (battle, attacking, defending) -> defending.assertNotFullHealth()
        );

        // Disguise is affected by Mold Breaker and so will take damage from the first hit
        disguiseTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .attacking(AbilityNamesies.MOLD_BREAKER)
                        .attackingFight(AttackNamesies.TACKLE),
                (battle, attacking, defending) -> defending.assertNotFullHealth()
        );

        // Disguise doesn't act actually break when ignored with Mold Breaker
        disguiseTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .with((battle, attacking, defending) -> {
                            // Attack with Mold Breaker, damage is dealt, but Disguise does not break
                            attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
                            battle.fight(AttackNamesies.TACKLE, AttackNamesies.SUNNY_DAY);
                            defending.assertNotFullHealth();

                            // Restore back to full health (just for simplicity of damage check, full health is
                            // not relevant to Disguise itself)
                            battle.defendingFight(AttackNamesies.SYNTHESIS);
                            defending.assertFullHealth();

                            // Suppress the Mold Breaker ability and try to break the disguise again
                            battle.defendingFight(AttackNamesies.GASTRO_ACID);
                            battle.attackingFight(AttackNamesies.TACKLE);
                        }),
                (battle, attacking, defending) -> defending.assertNotFullHealth(),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );
    }

    private void disguiseTest(AttackNamesies attackNamesies, PokemonManipulator samesies) {
        disguiseTest(attackNamesies, samesies, samesies);
    }

    private void disguiseTest(TestInfo testInfo, PokemonManipulator samesies) {
        disguiseTest(testInfo, samesies, samesies);
    }

    private void disguiseTest(AttackNamesies attackNamesies, PokemonManipulator withoutManipulator, PokemonManipulator withManipulator) {
        TestInfo testInfo = new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE).attackingFight(attackNamesies);
        disguiseTest(testInfo, withoutManipulator, withManipulator);
    }

    private void disguiseTest(TestInfo testInfo, PokemonManipulator withoutManipulator, PokemonManipulator withManipulator) {
        testInfo.doubleTake(AbilityNamesies.DISGUISE, withoutManipulator, withManipulator);
    }

    @Test
    public void pickupTest() {
        // Note: Test is currently only checking that all potential items to pick up are hold items, and does not
        // currently confirm its mechanics are working (actually gives items etc)
        ItemListHolder listHolder = (ItemListHolder)AbilityNamesies.PICKUP.getNewAbility();
        List<ItemNamesies> itemList = listHolder.getItems();
        for (ItemNamesies itemNamesies : itemList) {
            Assert.assertTrue(itemNamesies.getName(), itemNamesies.getItem() instanceof HoldItem);
        }
    }

    @Test
    public void cottonDownTest() {
        // Cotton Down activates every time the Pokemon takes direct attack damage
        cottonDownTest(0, AttackNamesies.TOXIC);
        cottonDownTest(-1, AttackNamesies.TACKLE);
        cottonDownTest(-1, AttackNamesies.SWIFT);
        cottonDownTest(-2, AttackNamesies.TWINEEDLE);
    }

    private void cottonDownTest(int expectedStage, AttackNamesies attackNamesies) {
        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.COTTON_DOWN);

        battle.attackingFight(attackNamesies);
        attacking.assertStages(new TestStages().set(expectedStage, Stat.SPEED));
        defending.assertStages(new TestStages());
    }

    @Test
    public void neutralizingGasTest() {
        // Cotton Down lowers the speed of Pokemon who attack it
        neutralizingGasTest(
                new TestInfo().attacking(AbilityNamesies.COTTON_DOWN).defendingFight(AttackNamesies.TACKLE),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(-1, Stat.SPEED)),
                (battle, attacking, defending) -> defending.assertStages(new TestStages())
        );

        // Magic Guard prevents indirect damage like poison
        neutralizingGasTest(
                new TestInfo(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE)
                        .attacking(AbilityNamesies.MAGIC_GUARD)
                        .defendingFight(AttackNamesies.TOXIC),
                (battle, attacking, defending) -> {
                    attacking.assertBadPoison();
                    attacking.assertFullHealth();
                },
                (battle, attacking, defending) -> {
                    attacking.assertBadPoison();
                    attacking.assertHealthRatio(15/16.0);
                }
        );

        // Neutralizing Gas can be suppressed with Gastro Acid
        neutralizingGasTest(
                new TestInfo(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE)
                        .attacking(AbilityNamesies.MAGIC_GUARD)
                        .fight(AttackNamesies.GASTRO_ACID, AttackNamesies.TOXIC),
                (battle, attacking, defending) -> {
                    attacking.assertBadPoison();
                    attacking.assertFullHealth();
                }
        );

        // Gives Overgrow for the case that checks no Neutralizing Gas
        // The ordering is a little weird so writing it this way so it doesn't overwrite
        // Used for tests where the defending ability is always relevant
        PokemonManipulator defendingOvergrow = (battle, attacking, defending) -> {
            if (!defending.hasAbility(AbilityNamesies.NEUTRALIZING_GAS)) {
                defending.assertAbility(AbilityNamesies.NO_ABILITY);
                defending.withAbility(AbilityNamesies.OVERGROW);
            }
        };

        // Currently implemented that Power of Alchemy does NOT steal Neutralizing Gas
        neutralizingGasTest(
                new TestInfo().asTrainerBattle()
                              .attacking(AbilityNamesies.POWER_OF_ALCHEMY)
                              .with(defendingOvergrow)
                              .with((battle, attacking, defending) -> {
                                  // Add an additional Pokemon so the battle doesn't end on a kill
                                  TestPokemon defending2 = battle.addDefending(PokemonNamesies.SQUIRTLE);
                                  battle.assertFront(defending);
                                  battle.falseSwipePalooza(true);
                                  defending.assertHp(1);
                                  battle.attackingFight(AttackNamesies.TACKLE);
                                  defending.assertHp(0);
                                  battle.assertFront(defending2);
                              }),
                (battle, attacking, defending) -> attacking.assertChangedAbility(AbilityNamesies.OVERGROW),
                (battle, attacking, defending) -> attacking.assertAbility(AbilityNamesies.POWER_OF_ALCHEMY)
        );

        // Clear Body prevents stats from being lowered, Octolock lowers Def/Sp.Def at the end of each turn
        neutralizingGasTest(
                new TestInfo().attacking(AbilityNamesies.CLEAR_BODY).defendingFight(AttackNamesies.OCTOLOCK),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages()),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(-1, Stat.DEFENSE, Stat.SP_DEFENSE))
        );

        // Water Veil prevents Burns, but can still be burned with Neutralizing Gas
        neutralizingGasTest(
                new TestInfo().attacking(AbilityNamesies.WATER_VEIL).defendingFight(AttackNamesies.WILL_O_WISP),
                (battle, attacking, defending) -> attacking.assertNoStatus(),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.BURNED)
        );

        // If the Neutralizing Gas Pokemon switches out though, they will be healed of their Burn
        // TODO: Currently statuses are checked before abilities so it is taking burn damage immediately before curing,
        //  but planning on hopefully fixing this soon and then change (14/16.0 , 1) -> (15/16.0) again
        neutralizingGasTest(
                new TestInfo().asTrainerBattle()
                              .attacking(AbilityNamesies.WATER_VEIL)
                              .defendingFight(AttackNamesies.WILL_O_WISP)
                              .with((battle, attacking, defending) -> {
                                  TestPokemon defending2 = battle.addDefending(PokemonNamesies.SQUIRTLE);
                                  battle.assertFront(defending);
                                  battle.attackingFight(AttackNamesies.WHIRLWIND);
                                  battle.assertFront(defending2);

                                  // Regardless of Neutralizing Gas from first Pokemon, the burn should be gone
                                  // (Either never was or was healed when the gas left)
                                  attacking.assertNoStatus();
                              }),
                (battle, attacking, defending) -> attacking.assertFullHealth(),
                (battle, attacking, defending) -> attacking.assertHealthRatio(14/16.0, 1)
        );

        // Skill Swap should not work with Neutralizing Gas
        // Should not matter if attacking or defending Pokemon is performing the swap
        neutralizingGasTest(
                new TestInfo().attacking(AbilityNamesies.BLAZE)
                              .with(defendingOvergrow)
                              .attackingFight(AttackNamesies.SKILL_SWAP),
                (battle, attacking, defending) -> {
                    attacking.assertChangedAbility(AbilityNamesies.OVERGROW);
                    defending.assertChangedAbility(AbilityNamesies.BLAZE);
                },
                (battle, attacking, defending) -> {
                    attacking.assertAbility(AbilityNamesies.NO_ABILITY);
                    defending.assertAbility(AbilityNamesies.NEUTRALIZING_GAS);
                }
        );
    }

    private void neutralizingGasTest(TestInfo testInfo, PokemonManipulator samesies) {
        neutralizingGasTest(testInfo, samesies, samesies);
    }

    // Neutralizing Gas suppresses all other abilities (for the most part)
    private void neutralizingGasTest(TestInfo testInfo, PokemonManipulator normal, PokemonManipulator suppressed) {
        testInfo.doubleTake(AbilityNamesies.NEUTRALIZING_GAS, normal, suppressed);
    }

    @Test
    public void ripenTest() {
        // Oran Berry restores by 10 (20 with Ripen)
        ripenTest(
                triggerHealthBerry(ItemNamesies.ORAN_BERRY),
                (battle, attacking, defending) -> defending.assertHp(11),
                (battle, attacking, defending) -> defending.assertHp(21)
        );

        // Sitrus Berry heals 1/4 max HP (1/2 with Ripen)
        ripenTest(
                triggerHealthBerry(ItemNamesies.SITRUS_BERRY),
                (battle, attacking, defending) -> defending.assertHealthRatioDiff(1, -.25),
                (battle, attacking, defending) -> defending.assertHealthRatioDiff(1, -.5)
        );

        // Leppa heals 10 PP (20 with Ripen) when completely depleted
        ripenTest(
                new TestInfo().with((battle, attacking, defending) -> {
                    defending.withMoves(AttackNamesies.TAIL_WHIP);
                    defending.withItem(ItemNamesies.LEPPA_BERRY);

                    Move tailWhip = defending.getMoves(battle).get(0);
                    Assert.assertEquals(AttackNamesies.TAIL_WHIP, tailWhip.getAttack().namesies());
                    Assert.assertEquals(30, tailWhip.getPP());

                    // Spite should fail first time since defending hasn't used anything yet
                    attacking.setMove(new Move(AttackNamesies.SPITE));
                    defending.setMove(tailWhip);
                    battle.fight();
                    Assert.assertEquals(29, tailWhip.getPP());

                    // Spite will reduce by 4 and the use of Tail will reduce by 1, so -5 PP per turn
                    for (int i = 1; i <= 5; i++) {
                        battle.fight();
                        Assert.assertEquals(29 - 5*i, tailWhip.getPP());
                    }
                    Assert.assertEquals(4, tailWhip.getPP());

                    // No longer use Spite, only natural PP loss for the last few
                    attacking.setMove(new Move(AttackNamesies.SPLASH));
                    battle.fight();
                    battle.fight();
                    battle.fight();
                    Assert.assertEquals(1, tailWhip.getPP());

                    // Use Tail Whip one last time to deplete PP to zero, then Leppa Berry should restore it's PP
                    defending.assertHoldingItem(ItemNamesies.LEPPA_BERRY);
                    battle.fight();
                    defending.assertConsumedBerry();
                }),
                (battle, attacking, defending) -> Assert.assertEquals(10, defending.getMoves(battle).get(0).getPP()),
                (battle, attacking, defending) -> Assert.assertEquals(20, defending.getMoves(battle).get(0).getPP())
        );

        // Passho Berry reduces super-effective Water moves by 50% (75% with Ripen)
        ripenTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .defending(ItemNamesies.PASSHO_BERRY),
                (battle, attacking, defending) -> {
                    attacking.setExpectedDamageModifier(.5);
                    battle.fight(AttackNamesies.WATER_GUN, AttackNamesies.ENDURE);
                },
                (battle, attacking, defending) -> {
                    attacking.setExpectedDamageModifier(.25);
                    battle.fight(AttackNamesies.WATER_GUN, AttackNamesies.ENDURE);
                }
        );

        // Enigma Berry heals 25% max HP (50% with Ripen) when hit by a super-effective move
        ripenTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .falseSwipePalooza(true)
                        .defending(ItemNamesies.ENIGMA_BERRY)
                        .fight(AttackNamesies.WATER_GUN, AttackNamesies.ENDURE),
                (battle, attacking, defending) -> defending.assertHealthRatioDiff(1, -.25),
                (battle, attacking, defending) -> defending.assertHealthRatioDiff(1, -.5)
        );

        // Liechi Berry boosts Attack by 1 (2 with Ripen) when 'in a pinch'
        ripenTest(
                triggerHealthBerry(ItemNamesies.LIECHI_BERRY),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(1, Stat.ATTACK)),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(2, Stat.ATTACK))
        );

        // Starf Berry boosts a random stat by 2 (4 with Ripen) when 'in a pinch'
        ripenTest(
                triggerHealthBerry(ItemNamesies.STARF_BERRY),
                (battle, attacking, defending) -> defending.assertTotalStages(2),
                (battle, attacking, defending) -> defending.assertTotalStages(4)
        );

        // Maranga Berry increases Sp. Defense by 1 (2 with Ripen) when hit by a special attack
        ripenTest(
                new TestInfo().defending(ItemNamesies.MARANGA_BERRY).attackingFight(AttackNamesies.SWIFT),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(1, Stat.SP_DEFENSE)),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(2, Stat.SP_DEFENSE))
        );

        // Jaboca Berry reduces attacker by 1/8 max HP (1/4 with Ripen) when landing a physical attack
        ripenTest(
                new TestInfo().defending(ItemNamesies.JABOCA_BERRY).attackingFight(AttackNamesies.TACKLE),
                (battle, attacking, defending) -> attacking.assertHealthRatio(7/8.0),
                (battle, attacking, defending) -> attacking.assertHealthRatio(3/4.0)
        );

        // Ripen is also relevant when consuming in unconventional ways such as Fling
        ripenTest(
                new TestInfo()
                        .with((battle, attacking, defending) -> {
                            battle.falseSwipePalooza(true);
                            attacking.giveItem(ItemNamesies.ORAN_BERRY);
                            battle.fight(AttackNamesies.FLING, AttackNamesies.ENDURE);

                            // Fling consumes the item for the thrower, but the recipient is the one who eats the berry
                            attacking.assertConsumedItem();
                            defending.assertNotHoldingItem();
                            defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                            defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
                        }),
                (battle, attacking, defending) -> defending.assertHp(11),
                (battle, attacking, defending) -> defending.assertHp(21)
        );

        // Ripen is also relevant when consuming in unconventional ways such as Bug Bite/Pluck
        ripenTest(
                new TestInfo().falseSwipePalooza(true)
                              .attacking(ItemNamesies.ORAN_BERRY)
                              .fight(AttackNamesies.ENDURE, AttackNamesies.PLUCK)
                              .with((battle, attacking, defending) -> {
                                  // Pluck consumes the item for the recipient, but the user is the one who eats the berry
                                  attacking.assertConsumedItem();
                                  defending.assertNotHoldingItem();
                                  defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                                  defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
                              }),
                (battle, attacking, defending) -> defending.assertHp(11),
                (battle, attacking, defending) -> defending.assertHp(21)
        );

        // Ripen only works inside the battle
        TestPokemon bulby = TestPokemon.newPlayerPokemon(PokemonNamesies.BULBASAUR).withAbility(AbilityNamesies.RIPEN);
        bulby.setHP(1);
        Player player = Game.getPlayer();
        player.exitBattle();
        Bag bag = player.getBag();
        bag.addItem(ItemNamesies.ORAN_BERRY);
        bag.usePokemonItem(ItemNamesies.ORAN_BERRY, bulby);
        bulby.assertHp(11);
    }

    // Returns a TestInfo that triggers the specified HeathTriggeredBerry at 1 HP
    // Does so by reducing health to 1, then adding berry held item (so it doesn't trigger while reducing),
    // and then uses False Swipe to trigger berry while at 1 HP
    private TestInfo triggerHealthBerry(ItemNamesies berry) {
        return new TestInfo().falseSwipePalooza(true)
                             .defending(berry)
                             .attackingFight(AttackNamesies.FALSE_SWIPE);
    }

    private void ripenTest(TestInfo testInfo, PokemonManipulator withoutRipen, PokemonManipulator withRipen) {
        testInfo.doubleTake(AbilityNamesies.RIPEN, withoutRipen, withRipen);
    }

    @Test
    public void screenCleanerTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();
        TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.SQUIRTLE);
        attacking2.withAbility(AbilityNamesies.SCREEN_CLEANER);

        // Add Reflect to attacking side, and Light Screen to defending side
        // Also add some entry hazards which are irrelevant to make sure they don't clear
        battle.fight(AttackNamesies.STEALTH_ROCK, AttackNamesies.TOXIC_SPIKES);
        battle.fight(AttackNamesies.REFLECT, AttackNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(attacking, TeamEffectNamesies.REFLECT);
        battle.assertHasEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(attacking, TeamEffectNamesies.TOXIC_SPIKES);
        battle.assertHasEffect(defending, TeamEffectNamesies.STEALTH_ROCK);

        // Whirlwind to send out Screen Cleaner bro
        // Screen Cleaner cleans from both sides (NO ONE IS SAFE)
        battle.assertFront(attacking);
        battle.defendingFight(AttackNamesies.WHIRLWIND);
        battle.assertFront(attacking2);
        battle.assertNoEffect(attacking, TeamEffectNamesies.REFLECT);
        battle.assertNoEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(attacking, TeamEffectNamesies.TOXIC_SPIKES);
        battle.assertHasEffect(defending, TeamEffectNamesies.STEALTH_ROCK);
        attacking2.assertRegularPoison();

        // Send Bulby back out (will absorb Toxic Spikes because Poison-type)
        battle.assertFront(attacking2);
        battle.defendingFight(AttackNamesies.WHIRLWIND);
        battle.assertFront(attacking);
        battle.assertNoEffect(attacking, TeamEffectNamesies.TOXIC_SPIKES);

        // Aurora Veil will fail because not hailing yet
        battle.fight(AttackNamesies.AURORA_VEIL, AttackNamesies.HAIL);
        battle.assertNoEffect(attacking, TeamEffectNamesies.AURORA_VEIL);
        battle.assertWeather(WeatherNamesies.HAILING);

        // Lots of hail = no fail aurora veil
        battle.attackingFight(AttackNamesies.AURORA_VEIL);
        battle.assertHasEffect(attacking, TeamEffectNamesies.AURORA_VEIL);

        // Put Screen Cleaner Squirtle out again and bye bye aurora veil
        battle.assertFront(attacking);
        battle.defendingFight(AttackNamesies.WHIRLWIND);
        battle.assertFront(attacking2);
        battle.assertNoEffect(attacking, TeamEffectNamesies.AURORA_VEIL);

        // Screen Cleaner only removes barrier effects when entering, but they can be added while already out no probs
        battle.fight(AttackNamesies.REFLECT, AttackNamesies.AURORA_VEIL);
        battle.fight(AttackNamesies.AURORA_VEIL, AttackNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(attacking, TeamEffectNamesies.REFLECT);
        battle.assertHasEffect(attacking, TeamEffectNamesies.AURORA_VEIL);
        battle.assertHasEffect(defending, TeamEffectNamesies.LIGHT_SCREEN);
        battle.assertHasEffect(defending, TeamEffectNamesies.AURORA_VEIL);
    }

    @Test
    public void wanderingSpiritTest() {
        // Wandering Spirit swaps abilities on contact
        wanderingSpiritTest(true, AttackNamesies.TACKLE);
        wanderingSpiritTest(false, AttackNamesies.SWIFT);

        // Long Reach doesn't make contact
        wanderingSpiritTest(false, AttackNamesies.TACKLE, AbilityNamesies.LONG_REACH);

        // Stance Change cannot be swapped
        wanderingSpiritTest(false, AttackNamesies.TACKLE, AbilityNamesies.STANCE_CHANGE);

        // Cannot swap with itself
        wanderingSpiritTest(false, AttackNamesies.TACKLE, AbilityNamesies.WANDERING_SPIRIT);
    }

    private void wanderingSpiritTest(boolean shouldSwap, AttackNamesies attack) {
        wanderingSpiritTest(shouldSwap, attack, AbilityNamesies.OVERGROW);
    }

    private void wanderingSpiritTest(boolean shouldSwap, AttackNamesies attack, AbilityNamesies attackingAbility) {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(attackingAbility);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.WANDERING_SPIRIT);

        battle.fight(attack, AttackNamesies.ENDURE);
        if (shouldSwap) {
            attacking.assertChangedAbility(AbilityNamesies.WANDERING_SPIRIT);
            defending.assertChangedAbility(attackingAbility);
        } else {
            attacking.assertAbility(attackingAbility);
            defending.assertAbility(AbilityNamesies.WANDERING_SPIRIT);
        }
    }

    @Test
    public void ownTempoTest() {
        // Own Tempo prevents confusion
        ownTempoTest(
                new TestInfo(),
                (battle, attacking, defending) -> defending.assertHasEffect(PokemonEffectNamesies.CONFUSION),
                (battle, attacking, defending) -> defending.assertNoEffect(PokemonEffectNamesies.CONFUSION)
        );

        // Persim Berry cures confusion (if still holding was never confused)
        ownTempoTest(
                new TestInfo().defending(ItemNamesies.PERSIM_BERRY),
                (battle, attacking, defending) -> {
                    defending.assertNoEffect(PokemonEffectNamesies.CONFUSION);
                    defending.assertConsumedBerry();
                },
                (battle, attacking, defending) -> {
                    defending.assertNoEffect(PokemonEffectNamesies.CONFUSION);
                    defending.assertHoldingItem(ItemNamesies.PERSIM_BERRY);
                }
        );

        // Mental Herb same deal as Persim Berry
        ownTempoTest(
                new TestInfo().defending(ItemNamesies.MENTAL_HERB),
                (battle, attacking, defending) -> {
                    defending.assertNoEffect(PokemonEffectNamesies.CONFUSION);
                    defending.assertConsumedItem();
                },
                (battle, attacking, defending) -> {
                    defending.assertNoEffect(PokemonEffectNamesies.CONFUSION);
                    defending.assertHoldingItem(ItemNamesies.MENTAL_HERB);
                }
        );

        // Mold Breaker will allow the Own Tempo Pokemon to become confused
        // However, Own Tempo will remove the confusion at the end of the turn
        // Note: Thrash case is essentially same as without Mold Breaker since it does not affect it
        ownTempoTest(
                new TestInfo().attacking(AbilityNamesies.MOLD_BREAKER),
                (battle, attacking, defending) -> defending.assertHasEffect(PokemonEffectNamesies.CONFUSION),
                (battle, attacking, defending) -> defending.assertNoEffect(PokemonEffectNamesies.CONFUSION)
        );

        // Mold Breaker + Persim Berry shows that the berry needed to be eaten in the Own Tempo case
        // Need to do the Confuse Ray and Thrash tests separately since Mold Breaker doesn't work with Thrash
        ownTempoConfuseRayTest(
                new TestInfo().attacking(AbilityNamesies.MOLD_BREAKER).defending(ItemNamesies.PERSIM_BERRY),
                (battle, attacking, defending) -> {
                    defending.assertNoEffect(PokemonEffectNamesies.CONFUSION);
                    defending.assertConsumedBerry();
                }
        );
        ownTempoSelfConfusionTest(
                new TestInfo().attacking(AbilityNamesies.MOLD_BREAKER).defending(ItemNamesies.PERSIM_BERRY),
                (battle, attacking, defending) -> {
                    defending.assertNoEffect(PokemonEffectNamesies.CONFUSION);
                    defending.assertConsumedBerry();
                },
                (battle, attacking, defending) -> {
                    defending.assertNoEffect(PokemonEffectNamesies.CONFUSION);
                    defending.assertHoldingItem(ItemNamesies.PERSIM_BERRY);
                }
        );

        // Neutralizing Gas completely negates Own Tempo (will not remove confusion at end of turn)
        ownTempoTest(
                new TestInfo().attacking(AbilityNamesies.NEUTRALIZING_GAS),
                (battle, attacking, defending) -> defending.assertHasEffect(PokemonEffectNamesies.CONFUSION)
        );

        // If the Neutralizing Pokemon leaves battle however, the Pokemon will regain it's immunity and remove
        baseOwnTempoTest(
                new TestInfo().asTrainerBattle()
                              .attacking(AbilityNamesies.NEUTRALIZING_GAS)
                              .with((battle, attacking, defending) -> {
                                  TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.SQUIRTLE);
                                  battle.assertFront(attacking);
                                  attacking.assertAbility(AbilityNamesies.NEUTRALIZING_GAS);
                                  defending.assertAbility(AbilityNamesies.NO_ABILITY);

                                  // Always confuses defending
                                  battle.attackingFight(AttackNamesies.CONFUSE_RAY);
                                  defending.assertHasEffect(PokemonEffectNamesies.CONFUSION);

                                  // Set up switch to non-neutralizer with Baton Pass
                                  // Note: Cannot use something like Whirlwind because chance to fail by hurting self
                                  // in confusion or could remove confusion defeating the test purpose so using an
                                  // arbitrary item instead as using an item does not take a confusion turn or check
                                  // confusion damage
                                  attacking.setMove(new Move(AttackNamesies.BATON_PASS));
                                  battle.setItemAction(defending, ItemNamesies.X_ACCURACY);
                                  defending.assertStages(new TestStages()); // Item not used yet, just setup

                                  battle.fight();
                                  battle.assertFront(attacking2);
                                  defending.assertStages(new TestStages().set(2, Stat.ACCURACY));
                              }),
                (battle, attacking, defending) -> defending.assertHasEffect(PokemonEffectNamesies.CONFUSION),
                (battle, attacking, defending) -> defending.assertNoEffect(PokemonEffectNamesies.CONFUSION)
        );
    }

    private void ownTempoTest(TestInfo testInfo, PokemonManipulator samesies) {
        ownTempoTest(testInfo, samesies, samesies);
    }

    private void ownTempoConfuseRayTest(TestInfo testInfo, PokemonManipulator samesies) {
        ownTempoConfuseRayTest(testInfo, samesies, samesies);
    }

    // TestInfo should not include the method of adding confusion
    private void ownTempoTest(TestInfo testInfo, PokemonManipulator withoutTempo, PokemonManipulator withTempo) {
        ownTempoConfuseRayTest(testInfo, withoutTempo, withTempo);
        ownTempoSelfConfusionTest(testInfo, withoutTempo, withTempo);
    }

    // Confuse normally by attacking with Confuse Ray
    private void ownTempoConfuseRayTest(TestInfo testInfo, PokemonManipulator withoutTempo, PokemonManipulator withTempo) {
        baseOwnTempoTest(
                testInfo.copy().attackingFight(AttackNamesies.CONFUSE_RAY),
                withoutTempo, withTempo
        );
    }

    // Confuse with a Self-Confusion attack like Thrash
    private void ownTempoSelfConfusionTest(TestInfo testInfo, PokemonManipulator withoutTempo, PokemonManipulator withTempo) {
        baseOwnTempoTest(
                testInfo.copy(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE).with((battle, attacking, defending) -> {
                    // Thrash will attack for 2-3 turns and then confuse the user
                    attacking.setMove(new Move(AttackNamesies.RECOVER));
                    defending.setMove(new Move(AttackNamesies.THRASH));

                    battle.fight();
                    defending.assertHasEffect(PokemonEffectNamesies.SELF_CONFUSION);

                    battle.fight();

                    // If attacking still has the effect, then it's three turns -- do that turn
                    if (defending.hasEffect(PokemonEffectNamesies.SELF_CONFUSION)) {
                        battle.fight();
                    }

                    defending.assertNoEffect(PokemonEffectNamesies.SELF_CONFUSION);
                }),
                withoutTempo, withTempo
        );
    }

    // Performs a single double take with and without Own Tempo with everything already set up inside testInfo
    private void baseOwnTempoTest(TestInfo testInfoWithConfusion, PokemonManipulator withoutTempo, PokemonManipulator withTempo) {
        testInfoWithConfusion.doubleTake(AbilityNamesies.OWN_TEMPO, withoutTempo, withTempo);
    }

    @Test
    public void takenUnderHalfTest() {
        // Berserk raises Sp. Attack when directly hit to go below half health
        takenUnderHalfTest(
                new TestInfo().defending(AbilityNamesies.BERSERK),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(1, Stat.SP_ATTACK)),
                (battle, attacking, defending) -> defending.assertStages(new TestStages())
        );

        // Emergency Exit swaps the Pokemon out when damaged below half health
        takenUnderHalfTest(
                new TestInfo(PokemonNamesies.VAPOREON, PokemonNamesies.JOLTEON)
                        .asTrainerBattle()
                        .defending(AbilityNamesies.EMERGENCY_EXIT)
                        .addDefending(PokemonNamesies.FLAREON),
                (battle, attacking, defending) -> defending.assertSpecies(PokemonNamesies.FLAREON),
                (battle, attacking, defending) -> defending.assertSpecies(PokemonNamesies.JOLTEON)
        );

        // Manual setup for Emergency Exit direct damage with U-Turn -- both Pokemon will swap
        takenUnderHalfTest(
                new TestInfo(PokemonNamesies.ESPEON, PokemonNamesies.UMBREON)
                        .asTrainerBattle()
                        .defending(AbilityNamesies.EMERGENCY_EXIT)
                        .addAttacking(PokemonNamesies.LEAFEON)
                        .addDefending(PokemonNamesies.GLACEON)
                        .fight(AttackNamesies.U_TURN, AttackNamesies.ENDURE),
                (battle, attacking, defending) -> {
                    attacking.assertSpecies(PokemonNamesies.LEAFEON);
                    defending.assertSpecies(PokemonNamesies.GLACEON);
                }
        );
    }

    private void takenUnderHalfTest(TestInfo testInfo, PokemonManipulator directUnderHalf, PokemonManipulator indirectUnderHalf) {
        // Directly attack to put under half health -- should trigger
        takenUnderHalfTest(
                testInfo.copy().fight(AttackNamesies.TACKLE, AttackNamesies.ENDURE),
                directUnderHalf
        );

        // Go under half by cutting health with substitute -- should not trigger
        takenUnderHalfTest(
                testInfo.copy().defendingFight(AttackNamesies.SUBSTITUTE),
                (battle, attacking, defending) -> {
                    defending.assertHasEffect(PokemonEffectNamesies.SUBSTITUTE);
                    indirectUnderHalf.manipulate(battle);
                }
        );

        // Go under half by taking damage with burn -- should not trigger
        takenUnderHalfTest(
                testInfo.copy(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE).attackingFight(AttackNamesies.WILL_O_WISP),
                (battle, attacking, defending) -> {
                    defending.assertHasStatus(StatusNamesies.BURNED);
                    indirectUnderHalf.manipulate(battle);
                }
        );

        // Go under half by taking damage with confusion damage -- should not trigger
        takenUnderHalfTest(
                testInfo.copy().with((battle, attacking, defending) -> {
                    // Continuously confuse until the defending hurts itself in confusion
                    while (true) {
                        Assert.assertFalse(defending.getHPRatio() < .5);
                        battle.attackingFight(AttackNamesies.CONFUSE_RAY);
                        if (defending.getHPRatio() < .5) {
                            break;
                        }
                    }
                }),
                indirectUnderHalf
        );

        // Go under half by taking recoil damage -- should not trigger
        takenUnderHalfTest(
                testInfo.copy().fight(AttackNamesies.ENDURE, AttackNamesies.TAKE_DOWN),
                indirectUnderHalf
        );

        // Go under half by via Pain Split -- should not trigger
        // Set attacking to 1 HP so Pain Split decreases defending's HP
        takenUnderHalfTest(
                testInfo.copy().falseSwipePalooza(false).attackingFight(AttackNamesies.PAIN_SPLIT),
                indirectUnderHalf
        );
    }

    private void takenUnderHalfTest(TestInfo testInfo, PokemonManipulator underHalfEffect) {
        TestBattle battle = testInfo.createBattle();
        TestPokemon defending = battle.getDefending();

        defending.setHP(defending.getMaxHP()/2 + 1);
        Assert.assertFalse(defending.getHPRatio() < .5);

        testInfo.manipulate(battle);
        Assert.assertTrue(defending.getHPRatio() < .5);

        underHalfEffect.manipulate(battle);
    }
}
