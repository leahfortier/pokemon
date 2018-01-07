package test.battle;

import battle.attack.AttackNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
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
        attacking.apply(beforePriority <= 0 || attacking.getAttack().isSelfTarget(), attack, battle);

        battle.emptyHeal();
        manipulator.manipulate(battle, attacking, defending);

        attacking.setupMove(attack, battle);
        Assert.assertEquals(afterPriority, battle.getAttackPriority(attacking));
        attacking.apply(!prevent, attack, battle);
        Assert.assertEquals(afterPriority > 0 && !attacking.getAttack().isSelfTarget() && !attacking.breaksTheMold(), prevent);
    }
}
