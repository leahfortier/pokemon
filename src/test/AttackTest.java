package test;

import battle.attack.AttackNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Gender;
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

    @Test
    public void captivateTest() {
        TestPokemon attacking = new TestPokemon(PokemonNamesies.BULBASAUR)
                .withGender(Gender.MALE);
        TestPokemon defending = new TestPokemon(PokemonNamesies.CHARMANDER)
                .withGender(Gender.MALE);

        TestBattle battle = TestBattle.create(attacking, defending);

        // TODO: Test genderless too
        // TODO: This shouldn't use applies and effective separately, but once apply returns a boolean should use that for all
        attacking.setupMove(AttackNamesies.CAPTIVATE, battle, defending);
        Assert.assertFalse(attacking.getAttack().applies(battle, attacking, defending));

        defending.withGender(Gender.FEMALE);
        Assert.assertTrue(attacking.getAttack().applies(battle, attacking, defending));

        attacking.withAbility(AbilityNamesies.OBLIVIOUS);
        Assert.assertTrue(attacking.getAttack().effective(battle, attacking, defending));

        attacking.withAbility(AbilityNamesies.NO_ABILITY);
        defending.withAbility(AbilityNamesies.OBLIVIOUS);
        Assert.assertFalse(attacking.getAttack().effective(battle, attacking, defending));
    }

    @Test
    public void ohkoTest() {
        TestPokemon attacking = new TestPokemon(PokemonNamesies.MAGIKARP);
        TestPokemon defending = new TestPokemon(PokemonNamesies.DRAGONITE);

        TestBattle battle = TestBattle.create(attacking, defending);

        // Ground type should not effect
        battle.attackingFight(AttackNamesies.FISSURE);
        Assert.assertTrue(defending.fullHealth());

        // OHKO,MF
        battle.attackingFight(AttackNamesies.HORN_DRILL);
        Assert.assertTrue(defending.isFainted(battle));

        // Sturdy prevents OHKO
        defending.fullyHeal();
        defending.withAbility(AbilityNamesies.STURDY);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.fullHealth());

        defending.withAbility(AbilityNamesies.NO_ABILITY);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.isFainted(battle));

        defending = new TestPokemon(PokemonNamesies.GLACEON);
        battle.attackingFight(AttackNamesies.SHEER_COLD);
        Assert.assertTrue(defending.fullHealth());
    }

    @Test
    public void multiTurnMoveTest() {
        // TODO
    }
}
