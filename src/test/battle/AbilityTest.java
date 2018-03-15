package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import test.BaseTest;
import test.TestPokemon;
import trainer.EnemyTrainer;
import type.Type;
import type.TypeAdvantage;

public class AbilityTest extends BaseTest {
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
            new TestStages().set(Stat.ATTACK, 1).set(Stat.SPEED, 1).test(attacking);
            new TestStages().test(defending);
            defending.assertFullHealth();
        });
        wonderGuardTest(AttackNamesies.THUNDER_WAVE, emptyInfo, (battle, attacking, defending) -> {
            Assert.assertTrue(defending.hasStatus(StatusNamesies.PARALYZED));
            defending.assertFullHealth();
            new TestStages().test(defending);
        });

        PokemonManipulator murder = (battle, attacking, defending) -> Assert.assertTrue(defending.isActuallyDead());

        // Super-effective moves and moves without type work
        wonderGuardTest(AttackNamesies.SHADOW_BALL, emptyInfo, murder);
        wonderGuardTest(AttackNamesies.STRUGGLE, emptyInfo, murder);

        PokemonManipulator allClear = (battle, attacking, defending) -> {
            attacking.assertFullHealth();
            defending.assertFullHealth();
            new TestStages().test(attacking);
            new TestStages().test(defending);
            Assert.assertFalse(attacking.hasStatus());
            Assert.assertFalse(defending.hasStatus());
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
        testInfo.defending(PokemonNamesies.SHEDINJA);

        TestBattle battle = testInfo.createBattle();
        testInfo.manipulate(battle);

        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.WONDER_GUARD);

        battle.attackingFight(attackNamesies);
        testSuccess.manipulate(battle, attacking, defending);
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
        Assert.assertTrue(defending.isType(battle, Type.NORMAL));

        battle.fight(AttackNamesies.WATER_GUN, AttackNamesies.ENDURE);
        Assert.assertTrue(defending.isType(battle, Type.WATER));
        Assert.assertFalse(defending.isType(battle, Type.NORMAL));

        battle.emptyHeal();
        battle.fight(AttackNamesies.EMBER, AttackNamesies.ENDURE);
        Assert.assertTrue(defending.isType(battle, Type.FIRE));
        Assert.assertFalse(defending.isType(battle, Type.WATER));

        // Status moves should not change type
        battle.attackingFight(AttackNamesies.GROWL);
        Assert.assertTrue(defending.isType(battle, Type.FIRE));
        Assert.assertFalse(defending.isType(battle, Type.NORMAL));

        // Status moves should not change type
        battle.attackingFight(AttackNamesies.THUNDER_WAVE);
        Assert.assertTrue(defending.isType(battle, Type.FIRE));
        Assert.assertFalse(defending.isType(battle, Type.ELECTRIC));

        battle.attackingFight(AttackNamesies.TACKLE);
        Assert.assertTrue(defending.isType(battle, Type.NORMAL));
        Assert.assertFalse(defending.isType(battle, Type.FIRE));
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
        manipulator.manipulate(battle, attacking, defending);

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
        new TestStages().set(Stat.SPEED, -2).test(attacking);
        new TestStages().set(Stat.SPEED, 2).test(defending);

        // Will also decrease their stat when it should be increased
        battle.fight(AttackNamesies.AGILITY, AttackNamesies.AGILITY);
        new TestStages().test(attacking);
        new TestStages().test(defending);

        battle.fight(AttackNamesies.SHELL_SMASH, AttackNamesies.SHELL_SMASH);
        new TestStages().set(Stat.ATTACK, 2)
                        .set(Stat.SP_ATTACK, 2)
                        .set(Stat.SPEED, 2)
                        .set(Stat.DEFENSE, -1)
                        .set(Stat.SP_DEFENSE, -1)
                        .test(attacking);
        new TestStages().set(Stat.ATTACK, -2)
                        .set(Stat.SP_ATTACK, -2)
                        .set(Stat.SPEED, -2)
                        .set(Stat.DEFENSE, 1)
                        .set(Stat.SP_DEFENSE, 1)
                        .test(defending);

        battle.attackingFight(AttackNamesies.HAZE);
        new TestStages().test(attacking);
        new TestStages().test(defending);

        // Contrary is affected by Mold Breaker
        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        battle.fight(AttackNamesies.STRING_SHOT, AttackNamesies.STRING_SHOT);
        new TestStages().set(Stat.SPEED, -2).test(attacking);
        new TestStages().set(Stat.SPEED, -2).test(defending);

        // Reset stages and remove Mold Breaker
        battle.fight(AttackNamesies.HAZE, AttackNamesies.GASTRO_ACID);
        new TestStages().test(attacking);
        new TestStages().test(defending);

        // Mist prevents stat reductions
        battle.defendingFight(AttackNamesies.MIST);

        // String shot is no longer a reduction, so it works
        battle.attackingFight(AttackNamesies.STRING_SHOT);
        new TestStages().test(attacking);
        new TestStages().set(Stat.SPEED, 2).test(defending);

        // Swagger is now a reduction, so it fails to raise/lower attack, but still confuses
        // Persim Berry heals confusion -- it should be consumed (since I don't wanna deal with confusion in tests)
        defending.giveItem(ItemNamesies.PERSIM_BERRY);
        battle.attackingFight(AttackNamesies.SWAGGER);
        new TestStages().test(attacking);
        new TestStages().set(Stat.SPEED, 2).test(defending);
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.CONFUSION));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));

        // Simple doubles stat modifications to itself -- shouldn't affect contrary pokemon
        battle.fight(AttackNamesies.HAZE, AttackNamesies.SIMPLE_BEAM);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.SIMPLE));
        new TestStages().test(attacking);
        new TestStages().test(defending);

        battle.fight(AttackNamesies.STRING_SHOT, AttackNamesies.STRING_SHOT);
        new TestStages().set(Stat.SPEED, -4).test(attacking);
        new TestStages().set(Stat.SPEED, 2).test(defending);

        battle.attackingFight(AttackNamesies.HAZE);
        new TestStages().test(attacking);
        new TestStages().test(defending);

        // Belly Drum sets attack stage to -6 instead of +6
        battle.fight(AttackNamesies.BELLY_DRUM, AttackNamesies.BELLY_DRUM);
        new TestStages().set(Stat.ATTACK, 6).test(attacking);
        new TestStages().set(Stat.ATTACK, -6).test(defending);
        attacking.assertHealthRatio(.5);
        defending.assertHealthRatio(.5);

        // Will still succeed and cut health when at -6 instead of +6 for Contrary :(
        battle.emptyHeal();
        battle.fight(AttackNamesies.BELLY_DRUM, AttackNamesies.BELLY_DRUM);
        Assert.assertFalse(attacking.lastMoveSucceeded());
        Assert.assertTrue(defending.lastMoveSucceeded());
        attacking.assertHealthRatio(1);
        defending.assertHealthRatio(.5);
        new TestStages().set(Stat.ATTACK, 6).test(attacking);
        new TestStages().set(Stat.ATTACK, -6).test(defending);

        battle.clearAllEffects();
        attacking.withAbility(AbilityNamesies.STURDY);
        defending.giveItem(ItemNamesies.FOCUS_SASH);

        // Leaf Storm is a damage dealing move that also decreases the user's Sp. Attack UNLESS YOU HAVE CONTRARY
        battle.fight(AttackNamesies.LEAF_STORM, AttackNamesies.LEAF_STORM);
        new TestStages().set(Stat.SP_ATTACK, -2).test(attacking);
        new TestStages().set(Stat.SP_ATTACK, 2).test(defending);

        battle.fight(AttackNamesies.SWORDS_DANCE, AttackNamesies.SWORDS_DANCE);
        new TestStages().set(Stat.ATTACK, 2).set(Stat.SP_ATTACK, -2).test(attacking);
        new TestStages().set(Stat.ATTACK, -2).set(Stat.SP_ATTACK, 2).test(defending);

        // Gaining/Losing Contrary does not affect your current stages
        battle.defendingFight(AttackNamesies.SKILL_SWAP);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.CONTRARY));
        Assert.assertFalse(defending.hasAbility(AbilityNamesies.CONTRARY));
        new TestStages().set(Stat.ATTACK, 2).set(Stat.SP_ATTACK, -2).test(attacking);
        new TestStages().set(Stat.ATTACK, -2).set(Stat.SP_ATTACK, 2).test(defending);
    }

    @Test
    public void sheerForceTest() {
        // Mystical Fire has a 100% chance to reduce target's Sp. Attack
        sheerForceSuccessTest(
                AttackNamesies.MYSTICAL_FIRE,
                (battle, attacking, defending) -> new TestStages().test(defending),
                (battle, attacking, defending) -> new TestStages().set(Stat.SP_ATTACK, -1).test(defending)
        );

        // Power-Up Punch has a 100% chance to raise the user's Attack
        sheerForceSuccessTest(
                AttackNamesies.POWER_UP_PUNCH,
                (battle, attacking, defending) -> new TestStages().test(attacking),
                (battle, attacking, defending) -> new TestStages().set(Stat.ATTACK, 1).test(attacking)
        );

        // Dynamic Punch has a 100% chance to confuse the target
        sheerForceSuccessTest(
                AttackNamesies.DYNAMIC_PUNCH,
                (battle, attacking, defending) -> Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.CONFUSION)),
                (battle, attacking, defending) -> Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.CONFUSION))
        );

        // Inferno has a 100% chance to burn the target
        sheerForceSuccessTest(
                AttackNamesies.INFERNO,
                (battle, attacking, defending) -> Assert.assertFalse(defending.hasStatus()),
                (battle, attacking, defending) -> Assert.assertTrue(defending.hasStatus(StatusNamesies.BURNED))
        );

        // Flare Blitz has a chance to Burn, so gets increase from Sheer Force, but should still take recoil damage regardless
        sheerForceSuccessTest(
                AttackNamesies.FLARE_BLITZ,
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    Assert.assertFalse(defending.hasStatus());
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
                (battle, attacking, defending) -> new TestStages().set(Stat.SP_ATTACK, -2).test(attacking)
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
        battle.setExpectedDamageModifier(applies ? 1.3 : 1.0);
        battle.attackingFight(attackNamesies);
        withSheerForceChecks.manipulate(battle, attacking, defending);

        battle.emptyHeal();
        battle.clearAllEffects();

        // Make sure it applies effects without Sheer Force
        attacking.withAbility(AbilityNamesies.NO_ABILITY);
        defending.withAbility(AbilityNamesies.SHEER_FORCE);
        battle.setExpectedDamageModifier(1.0);
        battle.attackingFight(attackNamesies);
        withoutSheerForceChecks.manipulate(battle, attacking, defending);
    }

    @Test
    public void steadfastTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.MOLD_BREAKER);
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.STEADFAST);

        // Fake out will cause the defending to flinch, speed will raise and its attack will not execute
        // Steadfast is not affected by mold breaker
        battle.fight(AttackNamesies.FAKE_OUT, AttackNamesies.TACKLE);
        new TestStages().test(attacking);
        new TestStages().set(Stat.SPEED, 1).test(defending);
        attacking.assertFullHealth();
        defending.assertNotFullHealth();

        defending.fullyHeal();

        // Fake out will fail the second time
        battle.fight(AttackNamesies.FAKE_OUT, AttackNamesies.TACKLE);
        new TestStages().test(attacking);
        new TestStages().set(Stat.SPEED, 1).test(defending);
        attacking.assertNotFullHealth();
        defending.assertFullHealth();

        // Make sure speed doesn't raise from other effects
        battle.attackingFight(AttackNamesies.CONFUSE_RAY);
        new TestStages().test(attacking);
        new TestStages().set(Stat.SPEED, 1).test(defending);
    }

    @Test
    public void innerFocusTest() {
        TestBattle battle = TestBattle.createTrainerBattle(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking().withAbility(AbilityNamesies.INNER_FOCUS);
        TestPokemon defending1 = battle.getDefending().withAbility(AbilityNamesies.NO_ABILITY); // Not fucking Sturdy
        TestPokemon defending2 = TestPokemon.newTrainerPokemon(PokemonNamesies.SHUCKLE).withAbility(AbilityNamesies.MOLD_BREAKER);
        ((EnemyTrainer)battle.getOpponent()).addPokemon(defending2);
        Assert.assertTrue(battle.getDefending() == defending1);

        // Fake out will not cause the defending to flinch and it will successfully use Tackle
        // (Fake Out will still strike first even though it is listed second since it has priority)
        battle.fight(AttackNamesies.TACKLE, AttackNamesies.FAKE_OUT);
        new TestStages().test(attacking);
        new TestStages().test(defending1);
        attacking.assertNotFullHealth();
        defending1.assertNotFullHealth();

        battle.emptyHeal();

        // Make sure other effects can still work
        attacking.withItem(ItemNamesies.PERSIM_BERRY);
        battle.defendingFight(AttackNamesies.CONFUSE_RAY);
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertTrue(attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));

        // Switch opponent Pokemon so I can use Fake Out again
        // NOTE: We need to kill the defending instead of something like whirlwind otherwise Fake Out won't work
        // ANOTHER NOTE: Everything feels a little backwards with attacking and defending in this test
        // because this does not appropriately swap if the player is killed
        battle.attackingFight(AttackNamesies.FISSURE);
        Assert.assertTrue(battle.getDefending() == defending2);
        Assert.assertTrue(defending2.isFirstTurn());

        // Defending2 has Mold Breaker, so attacking will still flinch with Inner Focus
        battle.fight(AttackNamesies.TACKLE, AttackNamesies.FAKE_OUT);
        new TestStages().test(attacking);
        new TestStages().test(defending2);
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
        Assert.assertTrue(defending.hasStatus(StatusNamesies.PARALYZED));
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.PARALYZED));

        attacking.giveItem(ItemNamesies.LUM_BERRY);
        defending.giveItem(ItemNamesies.CHERI_BERRY);
        battle.splashFight();
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));

        // Synchronize does not work on Sleep
        battle.attackingFight(AttackNamesies.SPORE);
        Assert.assertTrue(defending.hasStatus(StatusNamesies.ASLEEP));
        Assert.assertFalse(attacking.hasStatus());

        defending.withMoves(AttackNamesies.SLEEP_TALK, AttackNamesies.REFRESH);
        battle.defendingFight(AttackNamesies.SLEEP_TALK);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());

        // Make sure it works with bad poison
        battle.attackingFight(AttackNamesies.TOXIC);
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.BADLY_POISONED));

        battle.clearAllEffects();
        battle.emptyHeal();
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());

        // Both Pokemon with Synchronize and status healing berries
        // Attacking uses Thunder Wave and defending is paralyzed
        // Synchronize activates and attacker is paralyzed
        // Attacking heals by consuming berry
        // Defending heals by consuming berry
        attacking.withAbility(AbilityNamesies.SYNCHRONIZE);
        attacking.giveItem(ItemNamesies.LUM_BERRY);
        defending.giveItem(ItemNamesies.CHERI_BERRY);
        battle.attackingFight(AttackNamesies.THUNDER_WAVE);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));

        battle.clearAllEffects();
        battle.emptyHeal();
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY));

        // Synchronize will not activate for self-inflicted status conditions
        defending.giveItem(ItemNamesies.TOXIC_ORB);
        attacking.giveItem(ItemNamesies.PECHA_BERRY);
        battle.splashFight();
        Assert.assertTrue(defending.hasStatus(StatusNamesies.BADLY_POISONED));
        Assert.assertFalse(attacking.hasStatus());

        // Attacking will fling Pecha Berry at defending, curing its Poison by consuming the berry
        // Defending will fling Toxic Orb at attacking, inflicting Poison on it
        battle.fight(AttackNamesies.FLING, AttackNamesies.FLING);
        Assert.assertTrue(attacking.hasStatus(StatusNamesies.BADLY_POISONED));
        Assert.assertFalse(defending.hasStatus());
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
//        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY)); // TODO: Fling isn't working for this

        battle.attackingFight(AttackNamesies.REFRESH);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(defending.hasStatus());
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
        new TestStages().test(attacking);
        new TestStages().set(Stat.ATTACK, 1).test(defending);
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY));

        battle.emptyHeal();
        battle.clearAllEffects();

        // Give a Rawst Berry and then burn the target
        // Rawst Berry will heal its burn and then be consumed
        // Harvest will then restore the berry since the sunlight is strong
        defending.giveItem(ItemNamesies.RAWST_BERRY);
        battle.fight(AttackNamesies.WILL_O_WISP, AttackNamesies.SUNNY_DAY);
        Assert.assertFalse(defending.hasStatus());
        Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.RAWST_BERRY));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.EATEN_BERRY));
    }
}
