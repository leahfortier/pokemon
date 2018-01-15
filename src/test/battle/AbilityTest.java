package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import test.BaseTest;
import test.TestPokemon;
import type.Type;
import type.TypeAdvantage;

public class AbilityTest extends BaseTest {
    @Test
    public void testLevitate() {
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
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.SHEDINJA);
        TestPokemon attacking = battle.getAttacking();
        battle.getDefending().withAbility(AbilityNamesies.WONDER_GUARD);

        // Status move should work
        attacking.apply(true, AttackNamesies.DRAGON_DANCE, battle);
        attacking.apply(true, AttackNamesies.THUNDER_WAVE, battle);

        // Super-effective moves and moves without type work
        attacking.apply(true, AttackNamesies.SHADOW_BALL, battle);
        battle.emptyHeal();
        attacking.apply(true, AttackNamesies.STRUGGLE, battle);

        // Attacking non-super effective moves should not work
        attacking.apply(false, AttackNamesies.SURF, battle);
        attacking.apply(false, AttackNamesies.VINE_WHIP, battle);
        attacking.apply(false, AttackNamesies.TACKLE, battle);
    }

    @Test
    public void absorbTypeTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.LANTURN);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending().withAbility(AbilityNamesies.VOLT_ABSORB);

        battle.attackingFight(AttackNamesies.CONSTRICT);
        Assert.assertFalse(defending.fullHealth());

        // Thunderbolt should heal
        attacking.apply(false, AttackNamesies.THUNDERBOLT, battle);
        Assert.assertTrue(defending.fullHealth());

        battle.attackingFight(AttackNamesies.CONSTRICT);
        Assert.assertFalse(defending.fullHealth());

        battle.attackingFight(AttackNamesies.WATER_GUN);
        Assert.assertFalse(defending.fullHealth());
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
        Assert.assertFalse(defending.hasEffect(EffectNamesies.CONFUSION));
        Assert.assertFalse(defending.isHoldingItem(battle));
        Assert.assertTrue(defending.hasEffect(EffectNamesies.CONSUMED_ITEM));

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
}
