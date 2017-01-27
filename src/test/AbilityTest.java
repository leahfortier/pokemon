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
        Assert.assertTrue(TypeAdvantage.getAdvantage(attacking, defending, battle) == 0);

        // Even if holding a Ring Target
        defending.giveItem(ItemNamesies.RING_TARGET);
        Assert.assertTrue(TypeAdvantage.getAdvantage(attacking, defending, battle) == 0);

        // Unless the user has mold breaker
        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        Assert.assertTrue(TypeAdvantage.getAdvantage(attacking, defending, battle) > 0);

        defending.removeItem();
        Assert.assertTrue(TypeAdvantage.getAdvantage(attacking, defending, battle) > 0);
    }
}
