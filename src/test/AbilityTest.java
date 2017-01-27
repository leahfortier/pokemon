package test;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
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
        attacking.setMove(new Move(AttackNamesies.EARTHQUAKE));
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
        Assert.assertTrue(battle.ableToAttack(AttackNamesies.DRAGON_DANCE, attacking, defending));
        Assert.assertTrue(battle.ableToAttack(AttackNamesies.THUNDER_WAVE, attacking, defending));

        // Super-effective moves and moves without type work
        Assert.assertTrue(battle.ableToAttack(AttackNamesies.SHADOW_BALL, attacking, defending));
        Assert.assertTrue(battle.ableToAttack(AttackNamesies.STRUGGLE, attacking, defending));

        // Attacking non-super effective moves should not work
        Assert.assertFalse(battle.ableToAttack(AttackNamesies.SURF, attacking, defending));
        Assert.assertFalse(battle.ableToAttack(AttackNamesies.VINE_WHIP, attacking, defending));
        Assert.assertFalse(battle.ableToAttack(AttackNamesies.TACKLE, attacking, defending));
    }
}
