package test;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.status.StatusCondition;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;

public class EffectTest {
    @Test
    public void protectTest() {
        // Block moves
        checkProtect(true, AttackNamesies.PROTECT, AttackNamesies.TACKLE);
        checkProtect(true, AttackNamesies.DETECT, AttackNamesies.SCREECH);
        checkProtect(true, AttackNamesies.PROTECT, AttackNamesies.THUNDER_WAVE);
        checkProtect(true, AttackNamesies.PROTECT, AttackNamesies.SURF);
        checkProtect(true, AttackNamesies.PROTECT, AttackNamesies.FORESIGHT);

        // Protect-piercing, Field moves, and Self Target moves
        checkProtect(false, AttackNamesies.PROTECT, AttackNamesies.FEINT);
        checkProtect(false, AttackNamesies.PROTECT, AttackNamesies.PSYCHIC_TERRAIN);
        checkProtect(false, AttackNamesies.PROTECT, AttackNamesies.SWORDS_DANCE);
        checkProtect(false, AttackNamesies.DETECT, AttackNamesies.SUBSTITUTE);

        // Crafty Shield only protects against Status Moves
        checkProtect(true, AttackNamesies.CRAFTY_SHIELD, AttackNamesies.TOXIC);
        checkProtect(true, AttackNamesies.CRAFTY_SHIELD, AttackNamesies.CONFUSE_RAY);
        checkProtect(false, AttackNamesies.CRAFTY_SHIELD, AttackNamesies.CONSTRICT);
        checkProtect(false, AttackNamesies.CRAFTY_SHIELD, AttackNamesies.WATER_GUN);
        checkProtect(false, AttackNamesies.CRAFTY_SHIELD, AttackNamesies.FEINT);
        checkProtect(false, AttackNamesies.CRAFTY_SHIELD, AttackNamesies.DRAGON_DANCE);
        checkProtect(false, AttackNamesies.CRAFTY_SHIELD, AttackNamesies.MIST);

        // Quick guard only protects against increased priority moves
        checkProtect(true, AttackNamesies.QUICK_GUARD, AttackNamesies.QUICK_ATTACK);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.AVALANCHE);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.THUNDER_WAVE);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.CONSTRICT);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.WATER_GUN);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.FEINT);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.DRAGON_DANCE);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.MIST);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.CONFUSE_RAY,
                (battle, attacking, defending) -> {
                    defending.setAbility(AbilityNamesies.PRANKSTER);
                    attacking.callNewMove(battle, defending, new Move(AttackNamesies.QUICK_GUARD));
                    defending.setupMove(AttackNamesies.CONFUSE_RAY, battle);
                    Assert.assertFalse(defending.getAttack().apply(defending, attacking, battle));
                });

        // Baneful Bunker poisons when contact is made
        checkProtect(true, AttackNamesies.BANEFUL_BUNKER, AttackNamesies.TACKLE,
                (battle, attacking, defending) -> Assert.assertTrue(defending.hasStatus(StatusCondition.POISONED)));
        checkProtect(true, AttackNamesies.BANEFUL_BUNKER, AttackNamesies.WATER_GUN,
                (battle, attacking, defending) -> Assert.assertFalse(defending.hasStatus(StatusCondition.POISONED)));

        // King's Shield lowers attack when contact was made
        checkProtect(true, AttackNamesies.KINGS_SHIELD, AttackNamesies.TACKLE,
                (battle, attacking, defending) -> Assert.assertTrue(defending.getStage(Stat.ATTACK) == -2));
        checkProtect(true, AttackNamesies.KINGS_SHIELD, AttackNamesies.WATER_GUN,
                (battle, attacking, defending) -> Assert.assertTrue(defending.getStage(Stat.ATTACK) == 0));

        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        battle.fight(AttackNamesies.PROTECT, AttackNamesies.SCREECH);
        Assert.assertTrue(attacking.getStage(Stat.DEFENSE) == 0);
        Assert.assertTrue(defending.getStage(Stat.DEFENSE) == 0);

        // Make sure wears off by the next turn
        battle.defendingFight(AttackNamesies.SCREECH);
        Assert.assertTrue(attacking.getStage(Stat.DEFENSE) < 0);
        Assert.assertTrue(defending.getStage(Stat.DEFENSE) == 0);
    }

    private void checkProtect(boolean shouldProtect, AttackNamesies protectMove, AttackNamesies attack) {
        checkProtect(shouldProtect, protectMove, attack, PokemonManipulator.empty());
    }

    private void checkProtect(boolean shouldProtect, AttackNamesies protectMove, AttackNamesies attack, PokemonManipulator manipulator) {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        attacking.callNewMove(battle, defending, new Move(protectMove));
        defending.apply(!shouldProtect, attack, battle);
        manipulator.manipulate(battle, attacking, defending);

        if (shouldProtect) {
            battle.emptyHeal();
            battle.fight(protectMove, attack);

            Assert.assertTrue(attacking.fullHealth());
            Assert.assertFalse(attacking.hasStatus());
            Assert.assertTrue(attacking.getEffects().isEmpty());

            for (Stat stat : Stat.BATTLE_STATS) {
                Assert.assertTrue(attacking.getStage(stat) == 0);
            }
        }
    }
}
