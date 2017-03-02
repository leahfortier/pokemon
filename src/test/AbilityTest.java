package test;

import battle.attack.AttackNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import type.Type;
import type.TypeAdvantage;
import util.RandomUtils;

public class AbilityTest {
    @Test
    public void printSeed() {
        System.out.println("Random Seed: " + RandomUtils.getSeed());
    }

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
}
