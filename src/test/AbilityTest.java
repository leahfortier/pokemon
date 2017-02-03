package test;

import battle.Battle;
import battle.attack.AttackNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import type.TypeAdvantage;

public class AbilityTest {
    private static TestPokemon getPokemon(PokemonNamesies pokemon, AbilityNamesies ability) {
        return new TestPokemon(pokemon).withAbility(ability);
    }

    @Test
    public void testLevitate() {
        TestPokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR);
        TestPokemon defending = getPokemon(PokemonNamesies.KOFFING, AbilityNamesies.LEVITATE);

        Battle battle = TestBattle.create(attacking, defending);

        // Ground moves should not hit a levitating Pokemon
        attacking.setupMove(AttackNamesies.EARTHQUAKE, battle, defending);
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
        TestPokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR);
        TestPokemon defending = getPokemon(PokemonNamesies.SHEDINJA, AbilityNamesies.WONDER_GUARD);

        TestBattle battle = TestBattle.create(attacking, defending);

        // Status move should work
        Assert.assertTrue(attacking.apply(AttackNamesies.DRAGON_DANCE, battle, defending));
        Assert.assertTrue(attacking.apply(AttackNamesies.THUNDER_WAVE, battle, defending));

        // Super-effective moves and moves without type work
        Assert.assertTrue(attacking.apply(AttackNamesies.SHADOW_BALL, battle, defending));
        Assert.assertTrue(attacking.apply(AttackNamesies.STRUGGLE, battle, defending));

        // Attacking non-super effective moves should not work
        Assert.assertFalse(attacking.apply(AttackNamesies.SURF, battle, defending));
        Assert.assertFalse(attacking.apply(AttackNamesies.VINE_WHIP, battle, defending));
        Assert.assertFalse(attacking.apply(AttackNamesies.TACKLE, battle, defending));
    }

    @Test
    public void absorbTypeTest() {
        TestPokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR);
        TestPokemon defending = new TestPokemon(PokemonNamesies.LANTURN).withAbility(AbilityNamesies.VOLT_ABSORB);

        TestBattle battle = TestBattle.create(attacking, defending);

        battle.attackingFight(AttackNamesies.CONSTRICT);
        Assert.assertFalse(defending.fullHealth());

        battle.attackingFight(AttackNamesies.THUNDERBOLT);
        Assert.assertTrue(defending.fullHealth());

        battle.attackingFight(AttackNamesies.CONSTRICT);
        Assert.assertFalse(defending.fullHealth());

        battle.attackingFight(AttackNamesies.WATER_GUN);
        Assert.assertFalse(defending.fullHealth());
    }
}
