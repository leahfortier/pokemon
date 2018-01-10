package test.battle;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.generic.CastSource;
import battle.effect.status.StatusCondition;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import test.BaseTest;
import test.TestPokemon;
import type.Type;

public class EffectTest extends BaseTest {
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
        checkProtect(true, AttackNamesies.QUICK_GUARD, AttackNamesies.BABY_DOLL_EYES);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.AVALANCHE);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.THUNDER_WAVE);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.CONSTRICT);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.WATER_GUN);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.FEINT);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.DRAGON_DANCE);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.MIST);
        checkProtect(false, AttackNamesies.QUICK_GUARD, AttackNamesies.CONFUSE_RAY);
        checkProtect(true, AttackNamesies.QUICK_GUARD, AttackNamesies.CONFUSE_RAY,
                     PokemonManipulator.giveDefendingAbility(AbilityNamesies.PRANKSTER),
                     PokemonManipulator.empty()
        );

        // Baneful Bunker poisons when contact is made
        checkProtect(true, AttackNamesies.BANEFUL_BUNKER, AttackNamesies.TACKLE,
                     (battle, attacking, defending) -> Assert.assertTrue(defending.hasStatus(StatusCondition.POISONED))
        );
        checkProtect(true, AttackNamesies.BANEFUL_BUNKER, AttackNamesies.WATER_GUN,
                     (battle, attacking, defending) -> Assert.assertFalse(defending.hasStatus(StatusCondition.POISONED))
        );

        // King's Shield lowers attack when contact was made
        checkProtect(true, AttackNamesies.KINGS_SHIELD, AttackNamesies.TACKLE,
                     (battle, attacking, defending) -> Assert.assertTrue(defending.getStage(Stat.ATTACK) == -2)
        );
        checkProtect(true, AttackNamesies.KINGS_SHIELD, AttackNamesies.WATER_GUN,
                     (battle, attacking, defending) -> Assert.assertTrue(defending.getStage(Stat.ATTACK) == 0)
        );

        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        battle.fight(AttackNamesies.PROTECT, AttackNamesies.SCREECH);
        Assert.assertEquals(0, attacking.getStage(Stat.DEFENSE));
        Assert.assertEquals(0, defending.getStage(Stat.DEFENSE));

        // Make sure wears off by the next turn
        battle.defendingFight(AttackNamesies.SCREECH);
        Assert.assertTrue(attacking.getStage(Stat.DEFENSE) < 0);
        Assert.assertEquals(0, defending.getStage(Stat.DEFENSE));
    }

    private void checkProtect(boolean shouldProtect, AttackNamesies protectMove, AttackNamesies attack) {
        checkProtect(shouldProtect, protectMove, attack, PokemonManipulator.empty(), PokemonManipulator.empty());
    }

    private void checkProtect(boolean shouldProtect, AttackNamesies protectMove, AttackNamesies attack, PokemonManipulator additionalChecks) {
        checkProtect(shouldProtect, protectMove, attack, PokemonManipulator.empty(), additionalChecks);
    }

    private void checkProtect(boolean shouldProtect,
                              AttackNamesies protectMove,
                              AttackNamesies attack,
                              PokemonManipulator manipulator,
                              PokemonManipulator additionalChecks) {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        manipulator.manipulate(battle, attacking, defending);

        attacking.callNewMove(battle, defending, new Move(protectMove));
        defending.apply(!shouldProtect, attack, battle);

        additionalChecks.manipulate(battle, attacking, defending);

        if (shouldProtect) {
            battle.emptyHeal();
            battle.fight(protectMove, attack);

            Assert.assertTrue(attacking.fullHealth());
            Assert.assertFalse(attacking.hasStatus());
            Assert.assertTrue(attacking.getEffects().isEmpty());
            for (Stat stat : Stat.BATTLE_STATS) {
                Assert.assertEquals(0, attacking.getStage(stat));
            }
        }
    }

    @Test
    public void trappingTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        Assert.assertTrue(attacking.canEscape(battle));

        // Can't escape from a Pokemon with Shadow Tag
        defending.withAbility(AbilityNamesies.SHADOW_TAG);
        Assert.assertFalse(attacking.canEscape(battle));

        // Unless holding a shed shell
        attacking.giveItem(ItemNamesies.SHED_SHELL);
        Assert.assertTrue(attacking.canEscape(battle));
        attacking.removeItem();
        Assert.assertFalse(attacking.canEscape(battle));

        // Or both Pokemon have Shadow Tag
        attacking.withAbility(AbilityNamesies.SHADOW_TAG);
        Assert.assertTrue(attacking.canEscape(battle));

        // Mold Breaker shouldn't work
        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        Assert.assertFalse(attacking.canEscape(battle));

        // Ghost-type Pokemon can always escape
        attacking.withAbility(AbilityNamesies.PROTEAN);
        battle.attackingFight(AttackNamesies.SPITE);
        Assert.assertTrue(attacking.isType(battle, Type.GHOST));
        Assert.assertTrue(attacking.canEscape(battle));

        // Arena trap only works non-levitating Pokemon
        defending.withAbility(AbilityNamesies.ARENA_TRAP);
        Assert.assertTrue(attacking.canEscape(battle));
        battle.attackingFight(AttackNamesies.TAILWIND);
        Assert.assertTrue(attacking.isType(battle, Type.FLYING));
        Assert.assertTrue(attacking.canEscape(battle));
        attacking.giveItem(ItemNamesies.IRON_BALL);
        Assert.assertFalse(attacking.canEscape(battle));
        attacking.removeItem();
        Assert.assertTrue(attacking.canEscape(battle));

        // Only steel-type Pokemon cannot escape from a Pokemon with Magnet Pull
        defending.withAbility(AbilityNamesies.MAGNET_PULL);
        Assert.assertTrue(attacking.canEscape(battle));
        battle.attackingFight(AttackNamesies.GEAR_UP);
        Assert.assertTrue(attacking.isType(battle, Type.STEEL));
        Assert.assertFalse(attacking.canEscape(battle));
        battle.attackingFight(AttackNamesies.HAZE);
        Assert.assertFalse(attacking.isType(battle, Type.STEEL));
        Assert.assertTrue(attacking.canEscape(battle));

        // Partial trapping moves trap
        battle.defendingFight(AttackNamesies.WHIRLPOOL);
        Assert.assertFalse(attacking.canEscape(battle));
        attacking.getEffects().clear();
        Assert.assertTrue(attacking.canEscape(battle));

        // Ingrain prevents escaping
        battle.defendingFight(AttackNamesies.INGRAIN);
        Assert.assertTrue(attacking.canEscape(battle));
        battle.attackingFight(AttackNamesies.INGRAIN);
        Assert.assertFalse(attacking.canEscape(battle));
        attacking.getEffects().clear();
        Assert.assertTrue(attacking.canEscape(battle));

        // Straight up trap
        battle.defendingFight(AttackNamesies.MEAN_LOOK);
        Assert.assertFalse(attacking.canEscape(battle));
        attacking.getEffects().clear();
        Assert.assertTrue(attacking.canEscape(battle));

        // TODO: This is not supposed to block Ghosts from escaping
        battle.defendingFight(AttackNamesies.FAIRY_LOCK);
        Assert.assertFalse(attacking.canEscape(battle));
    }

    @Test
    public void critStageTest() {
        checkCritStage(1, new TestInfo().with(AttackNamesies.TACKLE));

        // +1 crit stage when using -- but no effect when used previously
        checkCritStage(2, new TestInfo().with(AttackNamesies.RAZOR_LEAF));
        checkCritStage(1, new TestInfo().attackingFight(AttackNamesies.RAZOR_LEAF));

        // +1 crit stage when used, but not when using (I guess it's a status move so it technically doesn't have a stage but whatever)
        checkCritStage(2, new TestInfo().attackingFight(AttackNamesies.FOCUS_ENERGY));
        checkCritStage(1, new TestInfo().with(AttackNamesies.FOCUS_ENERGY));

        // +1 after using Dire Hit (can only use once -- should fail if used again)
        checkCritStage(2, new TestInfo().with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT)));
        checkCritStage(2, new TestInfo()
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT, true))
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT, false))
        );

        // +1 from Lansat Berry when health is below 1/4
        checkCritStage(1, new TestInfo().attacking(ItemNamesies.LANSAT_BERRY));
        checkCritStage(1, new TestInfo()
                .attacking(ItemNamesies.LANSAT_BERRY)
                .with((battle, attacking, defending) -> {
                    // Not enough
                    battle.attackingFight(AttackNamesies.BELLY_DRUM);
                    attacking.assertHealthRatio(.5);
                })
        );
        checkCritStage(2, new TestInfo()
                .attacking(PokemonNamesies.BULBASAUR, ItemNamesies.LANSAT_BERRY)
                .with((battle, attacking, defending) -> battle.falseSwipePalooza(false))
        );

        // Razor Claw and Scope Lens increase by 1
        checkCritStage(2, new TestInfo().attacking(ItemNamesies.RAZOR_CLAW));
        checkCritStage(2, new TestInfo().attacking(ItemNamesies.SCOPE_LENS));

        // Lucky Punch increases by 2 but only for Chansey (Night Slash is also +1 when using)
        checkCritStage(2, new TestInfo().attacking(PokemonNamesies.CHANSEY).with(AttackNamesies.NIGHT_SLASH));
        checkCritStage(4, new TestInfo().attacking(PokemonNamesies.CHANSEY, ItemNamesies.LUCKY_PUNCH).with(AttackNamesies.NIGHT_SLASH));
        checkCritStage(2, new TestInfo().attacking(PokemonNamesies.FARFETCHD, ItemNamesies.LUCKY_PUNCH).with(AttackNamesies.NIGHT_SLASH));

        // Stick increases by 2 but only for Farfetch'd
        checkCritStage(3, new TestInfo().attacking(PokemonNamesies.FARFETCHD, ItemNamesies.STICK));
        checkCritStage(1, new TestInfo().attacking(PokemonNamesies.CHANSEY, ItemNamesies.STICK));

        // Super Luck increases by 1 (unaffected by Mold Breaker)
        checkCritStage(2, new TestInfo().attacking(AbilityNamesies.SUPER_LUCK));
        checkCritStage(2, new TestInfo().attacking(AbilityNamesies.SUPER_LUCK).defending(AbilityNamesies.MOLD_BREAKER));

        // Effects should stack
        checkCritStage(5, new TestInfo()
                .attacking(PokemonNamesies.CHANSEY, ItemNamesies.LUCKY_PUNCH)
                .attacking(AbilityNamesies.SUPER_LUCK)
                .with(AttackNamesies.NIGHT_SLASH)
        );

        checkCritStage(3, new TestInfo()
                .attackingFight(AttackNamesies.FOCUS_ENERGY)
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT))
        );

        checkCritStage(4, new TestInfo()
                .attackingFight(AttackNamesies.FOCUS_ENERGY)
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT))
                .attacking(ItemNamesies.LANSAT_BERRY).with((battle, attacking, defending) -> battle.falseSwipePalooza(false))
        );

        // Can't go over the max
        checkCritStage(5, new TestInfo()
                .attacking(PokemonNamesies.CHANSEY, ItemNamesies.LUCKY_PUNCH)
                .attacking(AbilityNamesies.SUPER_LUCK)
                .with(AttackNamesies.NIGHT_SLASH)
                .attackingFight(AttackNamesies.FOCUS_ENERGY)
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT))
        );
    }

    private void checkCritStage(int expectedStage, TestInfo testInfo) {
        TestBattle battle = TestBattle.create(testInfo.attackingName, testInfo.defendingName);
        TestPokemon attacking = battle.getAttacking();

        int beforeStage = battle.getCritStage(attacking);
        Assert.assertEquals(1, beforeStage);

        testInfo.manipulate(battle);

        int afterStage = battle.getCritStage(attacking);
        Assert.assertEquals(expectedStage, afterStage);
    }

    // TODO: I don't know if EffectTest makes sense for this but whatever
    @Test
    public void sourceTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        attacking.withAbility(AbilityNamesies.OVERGROW);
        attacking.giveItem(ItemNamesies.ORAN_BERRY);
        attacking.setupMove(AttackNamesies.SWITCHEROO, battle);
        attacking.getAttributes().setCastSource(ItemNamesies.NO_ITEM.getItem());

        for (CastSource source : CastSource.values()) {
            String sourceName = source.getSourceName(battle, attacking);
            if (source.hasSourceName()) {
                // Important since these values are hard-coded in another method
                Assert.assertTrue(source == CastSource.ABILITY || source == CastSource.HELD_ITEM);
                Assert.assertNotNull(sourceName);
            } else {
                Assert.assertNull(sourceName);
            }
        }

        Assert.assertEquals(AbilityNamesies.OVERGROW.getName(), CastSource.ABILITY.getSourceName(battle, attacking));
        Assert.assertEquals(ItemNamesies.ORAN_BERRY.getName(), CastSource.HELD_ITEM.getSourceName(battle, attacking));

        Assert.assertTrue(attacking.getAbility() == CastSource.ABILITY.getSource(battle, attacking));
        Assert.assertTrue(attacking.getHeldItem(battle) == CastSource.HELD_ITEM.getSource(battle, attacking));
        Assert.assertTrue(attacking.getAttack() == CastSource.ATTACK.getSource(battle, attacking));
        Assert.assertTrue(attacking.getAttributes().getCastSource() == CastSource.CAST_SOURCE.getSource(battle, attacking));
    }
}
