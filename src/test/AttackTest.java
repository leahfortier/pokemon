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
    public void selfSwitchingMoves() {
        TestPokemon attacking1 = new TestPokemon(PokemonNamesies.CHANSEY);
        TestPokemon attacking2 = new TestPokemon(PokemonNamesies.HAPPINY);
        TestPokemon defending = new TestPokemon(PokemonNamesies.SHUCKLE);

        TestBattle battle = TestBattle.createTrainerBattle(attacking1, defending);
        battle.getPlayer().addPokemon(attacking2);

        Assert.assertTrue(battle.getPlayer().front() == attacking1);

        // Use U-Turn -- make sure they swap
        battle.attackingFight(AttackNamesies.U_TURN);
        Assert.assertTrue(battle.getPlayer().front() == attacking2);

        // TODO: Baton Pass

        // TODO: No more remaining Pokemon, wild battles, wimp out, red card, eject button
    }

    @Test
    public void swapOpponentMoves() {
        TestPokemon attacking1 = new TestPokemon(PokemonNamesies.STEELIX);
        TestPokemon attacking2 = new TestPokemon(PokemonNamesies.REGIROCK);
        TestPokemon defending = new TestPokemon(PokemonNamesies.SHUCKLE);

        TestBattle battle = TestBattle.createTrainerBattle(attacking1, defending);
        battle.getPlayer().addPokemon(attacking2);

        Assert.assertTrue(battle.getPlayer().front() == attacking1);

        // Use Dragon Tail -- make sure they swap
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.DRAGON_TAIL);
        Assert.assertTrue(battle.getPlayer().front() == attacking2);

        // Don't swap with Suction Cups
        attacking2.withAbility(AbilityNamesies.SUCTION_CUPS);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.CIRCLE_THROW);
        Assert.assertTrue(battle.getPlayer().front() == attacking2);

        attacking2.withAbility(AbilityNamesies.NO_ABILITY);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.ROAR);
        Assert.assertTrue(battle.getPlayer().front() == attacking1);

        // Don't swap when ingrained
        battle.fight(AttackNamesies.INGRAIN, AttackNamesies.WHIRLWIND);
        Assert.assertTrue(battle.getPlayer().front() == attacking1);

        // TODO: No more remaining Pokemon, wild battles, wimp out, red card, eject button
    }

    @Test
    public void multiTurnMoveTest() {
        // TODO
    }
}
