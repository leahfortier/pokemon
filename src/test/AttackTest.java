package test;

import battle.attack.AttackNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;

public class AttackTest {
    @Test
    public void recoilTest() {
        TestPokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR).withAbility(AbilityNamesies.ROCK_HEAD);
        TestPokemon defending = new TestPokemon(PokemonNamesies.CHARMANDER);

        TestBattle battle = TestBattle.create(attacking, defending);

        battle.attackingFight(AttackNamesies.TAKE_DOWN);
        Assert.assertTrue(attacking.fullHealth());
        Assert.assertFalse(defending.fullHealth());

        defending.fullyHeal();
        Assert.assertTrue(defending.fullHealth());

        battle.defendingFight(AttackNamesies.WOOD_HAMMER);
        Assert.assertFalse(attacking.fullHealth());
        Assert.assertFalse(defending.fullHealth());

        int damage = attacking.getMaxHP() - attacking.getHP();
        Assert.assertTrue(defending.getMaxHP() - defending.getHP() == (int)(Math.ceil(damage/3.0)));

        attacking.fullyHeal();
        defending.fullyHeal();
        defending.withAbility(AbilityNamesies.MAGIC_GUARD);
        battle.defendingFight(AttackNamesies.WOOD_HAMMER);
        Assert.assertTrue(defending.fullHealth());

        // Struggle should still cause recoil damage even if they have Rock Head/Magic Guard
        attacking.fullyHeal();
        battle.attackingFight(AttackNamesies.STRUGGLE);
        Assert.assertTrue(attacking.hasAbility(AbilityNamesies.ROCK_HEAD));
        Assert.assertFalse(attacking.fullHealth());
    }
}
