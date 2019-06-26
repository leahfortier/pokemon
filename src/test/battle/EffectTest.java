package test.battle;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.Effect;
import battle.effect.Effect.CastMessageGetter;
import battle.effect.EffectInterfaces.EndTurnSubsider;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.BattleEndTurnEffect;
import battle.effect.battle.terrain.TerrainNamesies;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import test.BaseTest;
import test.TestPokemon;
import trainer.EnemyTrainer;
import type.Type;

public class EffectTest extends BaseTest {
    @Test
    public void alternateCastTest() {
        for (EffectNamesies effectNamesies : EffectNamesies.values()) {
            Effect effect = effectNamesies.getEffect();
            try {
                // This will throw a NoSuchMethodException if the effect does not override the alternateCast method
                effect.getClass().getDeclaredMethod(
                        "alternateCast",
                        Battle.class,
                        ActivePokemon.class,
                        ActivePokemon.class,
                        CastSource.class,
                        CastMessageGetter.class
                );

                // If it didn't throw an exception, then hasAlternateCast MUST be true
                Assert.assertTrue(effect.hasAlternateCast());

                // In order to have an alternate cast, they need to be able to have the effect
                Assert.assertTrue(effect.canHave());
            } catch (NoSuchMethodException e) {
                // Method was not overridden, hasAlternateCast must be false
                Assert.assertFalse(effect.hasAlternateCast());
            }
        }
    }

    @Test
    public void endTurnSubsiderTest() {
        // Effects which implement the EndTurnSubsider, should always override the getSubsideMessage method
        for (EffectNamesies effectNamesies : EffectNamesies.values()) {
            Effect effect = effectNamesies.getEffect();
            try {
                // This will throw a NoSuchMethodException if the effect does not override the getSubsideMessage method
                // Nothing to confirm on success, as this method can be overridden by other effects that are not EndTurnSubsiders as well
                effect.getClass().getDeclaredMethod("getSubsideMessage", ActivePokemon.class);
            } catch (NoSuchMethodException e) {
                // Method was not overridden, so should not be an EndTurnSubsider (special case for Clear Skies)
                Assert.assertFalse(effectNamesies.name(), effect instanceof EndTurnSubsider);
                if (effect instanceof BattleEndTurnEffect && effectNamesies != WeatherNamesies.CLEAR_SKIES) {
                    Assert.assertFalse(effectNamesies.name(), ((BattleEndTurnEffect)effect).endTurnSubsider());
                }
            }
        }
    }

    @Test
    public void protectTest() {
        // Block moves
        checkProtect(true, AttackNamesies.PROTECT, AttackNamesies.TACKLE);
        checkProtect(true, AttackNamesies.DETECT, AttackNamesies.SCREECH);
        checkProtect(true, AttackNamesies.PROTECT, AttackNamesies.THUNDER_WAVE);
        checkProtect(true, AttackNamesies.PROTECT, AttackNamesies.SURF);
        checkProtect(true, AttackNamesies.PROTECT, AttackNamesies.FORESIGHT);

        // Should Protect against non-status 'self target' moves
        checkProtect(true, AttackNamesies.PROTECT, AttackNamesies.RAGE);
        checkProtect(true, AttackNamesies.PROTECT, AttackNamesies.FLAME_CHARGE);

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
                     (battle, attacking, defending) -> defending.assertRegularPoison()
        );
        checkProtect(true, AttackNamesies.BANEFUL_BUNKER, AttackNamesies.WATER_GUN,
                     (battle, attacking, defending) -> defending.assertNoStatus()
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
        Assert.assertEquals(-2, attacking.getStage(Stat.DEFENSE));
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

        manipulator.manipulate(battle);

        attacking.callFullNewMove(battle, defending, protectMove);
        defending.apply(!shouldProtect, attack, battle);

        additionalChecks.manipulate(battle);

        if (shouldProtect) {
            battle.emptyHeal();
            battle.fight(protectMove, attack);

            attacking.assertFullHealth();
            attacking.assertNoStatus();
            Assert.assertTrue(attacking.getEffects().asList().isEmpty());
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
        attacking.getEffects().reset();
        Assert.assertTrue(attacking.canEscape(battle));

        // Ingrain prevents escaping
        battle.defendingFight(AttackNamesies.INGRAIN);
        Assert.assertTrue(attacking.canEscape(battle));
        battle.attackingFight(AttackNamesies.INGRAIN);
        Assert.assertFalse(attacking.canEscape(battle));
        attacking.getEffects().reset();
        Assert.assertTrue(attacking.canEscape(battle));

        // Straight up trap
        battle.defendingFight(AttackNamesies.MEAN_LOOK);
        Assert.assertFalse(attacking.canEscape(battle));
        attacking.getEffects().reset();
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
        checkCritStage(1, new TestInfo().attackingFight(AttackNamesies.RAZOR_LEAF).with(AttackNamesies.TACKLE));

        // +2 crit stage when used, but not when using (I guess it's a status move so it technically doesn't have a stage but whatever)
        checkCritStage(3, new TestInfo().attackingFight(AttackNamesies.FOCUS_ENERGY).with(AttackNamesies.TACKLE));
        checkCritStage(1, new TestInfo().with(AttackNamesies.FOCUS_ENERGY));

        // +2 after using Dire Hit (can only use once -- should fail if used again)
        checkCritStage(3, new TestInfo().with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT)));
        checkCritStage(3, new TestInfo()
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT, true, true))
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT, true, false))
        );

        // +2 from Lansat Berry when health is below 1/4
        checkCritStage(1, new TestInfo().attacking(ItemNamesies.LANSAT_BERRY));
        checkCritStage(1, new TestInfo()
                .attacking(ItemNamesies.LANSAT_BERRY)
                .with((battle, attacking, defending) -> {
                    // Not enough
                    battle.attackingFight(AttackNamesies.BELLY_DRUM);
                    attacking.assertHealthRatio(.5);
                })
        );
        checkCritStage(3, new TestInfo()
                .attacking(PokemonNamesies.BULBASAUR, ItemNamesies.LANSAT_BERRY)
                .with((battle, attacking, defending) -> {
                    attacking.assertNotConsumedItem(battle);
                    battle.falseSwipePalooza(false);
                    attacking.assertConsumedBerry(battle);
                })
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

        checkCritStage(5, new TestInfo()
                .attackingFight(AttackNamesies.FOCUS_ENERGY)
                .with(PokemonManipulator.useItem(ItemNamesies.DIRE_HIT))
        );

        checkCritStage(5, new TestInfo()
                .attackingFight(AttackNamesies.FOCUS_ENERGY)
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
        TestBattle battle = testInfo.createBattle();
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
        attacking.setCastSource(ItemNamesies.NO_ITEM.getItem());

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
        Assert.assertTrue(attacking.getCastSource() == CastSource.CAST_SOURCE.getSource(battle, attacking));
    }

    @Test
    public void bypassAccuracyTest() {
        // Attacker will fly in the air, making it semi-invulverable to Tackle which should be a forced miss
        testSemiInvulnerable(false, AttackNamesies.FLY, AttackNamesies.TACKLE);
        testSemiInvulnerable(false, AttackNamesies.FLY, AttackNamesies.ROAR);

        // No Guard will allow Tackle to Hit, regardless of which Pokemon has it
        testSemiInvulnerable(true, true, AttackNamesies.FLY, AttackNamesies.TACKLE, true, new TestInfo().attacking(AbilityNamesies.NO_GUARD));
        testSemiInvulnerable(true, true, AttackNamesies.FLY, AttackNamesies.TACKLE, true, new TestInfo().defending(AbilityNamesies.NO_GUARD));

        // All of these moves will hit a Flying Pokemon
        testSemiInvulnerable(true, AttackNamesies.FLY, AttackNamesies.GUST);
        testSemiInvulnerable(true, AttackNamesies.FLY, AttackNamesies.WHIRLWIND);

        // Give the attacker berries because confusion/paralysis fucks with the test
        testSemiInvulnerable(true, AttackNamesies.FLY, AttackNamesies.HURRICANE, new TestInfo().attacking(ItemNamesies.PERSIM_BERRY));
        testSemiInvulnerable(true, AttackNamesies.FLY, AttackNamesies.THUNDER, new TestInfo().attacking(ItemNamesies.CHERI_BERRY));

        // Smack Down will disrupt Fly, grounding the Pokemon in the process
        testSemiInvulnerable(true, null, AttackNamesies.FLY, AttackNamesies.SMACK_DOWN, false, new TestInfo());
    }

    private void testSemiInvulnerable(Boolean expected, AttackNamesies multiTurnMove, AttackNamesies defendingMove) {
        testSemiInvulnerable(expected, multiTurnMove, defendingMove, new TestInfo());
    }

    private void testSemiInvulnerable(Boolean expected, AttackNamesies multiTurnMove, AttackNamesies defendingMove, TestInfo testInfo) {
        testSemiInvulnerable(expected, null, multiTurnMove, defendingMove, true, testInfo);
    }

    private void testSemiInvulnerable(Boolean firstExpected, Boolean secondExpected, AttackNamesies multiTurnMove, AttackNamesies defendingMove, boolean fullyExecuted, TestInfo testInfo) {
        testInfo.attacking(PokemonNamesies.SHUCKLE).defending(PokemonNamesies.SHUCKLE);

        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        testInfo.manipulate(battle);

        // Can't use setup move since it fucks with multi-turn moves
        attacking.setMove(new Move(multiTurnMove));
        defending.setMove(new Move(defendingMove));

        int attackingPP = attacking.getMove().getPP();
        int defendingPP = defending.getMove().getPP();

        // Attacker will disappear or whatever, making it semi-invulverable to defending
        battle.setExpectedDefendingAccuracyBypass(firstExpected);
        battle.fight();

        // Attacker will be finished with its move and the defending move should hit this time
        battle.setExpectedDefendingAccuracyBypass(secondExpected);
        battle.fight();

        Assert.assertEquals(attackingPP - (fullyExecuted ? 1 : 0), attacking.getMove().getPP());
        Assert.assertEquals(defendingPP - 2, defending.getMove().getPP());
    }

    @Test
    public void substituteTest() {
        // Status moves won't work against the substitute
        substituteTest(
                new TestInfo().defendingFight(AttackNamesies.THUNDER_WAVE),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.PARALYZED),
                (battle, attacking, defending) -> attacking.assertNoStatus()
        );

        substituteTest(
                new TestInfo().defendingFight(AttackNamesies.TAIL_WHIP),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(Stat.DEFENSE, -1)),
                (battle, attacking, defending) -> attacking.assertNoStages()
        );

        substituteTest(
                new TestInfo(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE).defendingFight(AttackNamesies.LEECH_SEED),
                (battle, attacking, defending) -> attacking.assertHasEffect(PokemonEffectNamesies.LEECH_SEED),
                (battle, attacking, defending) -> attacking.assertNoEffect(PokemonEffectNamesies.LEECH_SEED)
        );

        substituteTest(
                new TestInfo().defendingFight(AttackNamesies.YAWN),
                (battle, attacking, defending) -> attacking.assertHasEffect(PokemonEffectNamesies.YAWN),
                (battle, attacking, defending) -> attacking.assertNoEffect(PokemonEffectNamesies.YAWN)
        );

        // Unless it is sound-based
        substituteTest(
                new TestInfo().defendingFight(AttackNamesies.GROWL),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(Stat.ATTACK, -1))
        );

        // Should still be able to give self-target effects
        substituteTest(
                new TestInfo().attackingFight(AttackNamesies.STOCKPILE),
                (battle, attacking, defending) -> attacking.assertHasEffect(PokemonEffectNamesies.STOCKPILE)
        );

        substituteTest(
                new TestInfo().attackingFight(AttackNamesies.BELLY_DRUM)
                              .with((battle, attacking, defending) -> attacking.assertHealthRatio(.5))
                              .attackingFight(AttackNamesies.AQUA_RING),
                (battle, attacking, defending) -> {
                    attacking.assertHasEffect(PokemonEffectNamesies.AQUA_RING);
                    attacking.assertHealthRatio(9/16.0, 1);
                }
        );

        // Recoil damage should not be absorbed
        substituteTest(
                new TestInfo().fight(AttackNamesies.TAKE_DOWN, AttackNamesies.ENDURE),
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertNotFullHealth();
                }
        );

        // Items cannot be swapped or knocked off or eaten
        // Needs to be a trainer battle or it won't remove the item
        substituteTest(
                new TestInfo().asTrainerBattle()
                              .attacking(ItemNamesies.POTION)
                              .defendingFight(AttackNamesies.TRICK),
                (battle, attacking, defending) -> {
                    Assert.assertFalse(attacking.isHoldingItem(battle));
                    Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.POTION));
                },
                (battle, attacking, defending) -> {
                    Assert.assertFalse(defending.lastMoveSucceeded());
                    Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.POTION));
                    Assert.assertFalse(defending.isHoldingItem(battle));
                }
        );

        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .asTrainerBattle()
                        .attacking(ItemNamesies.POTION)
                        .defendingFight(AttackNamesies.KNOCK_OFF),
                (battle, attacking, defending) -> Assert.assertFalse(attacking.isHoldingItem(battle)),
                (battle, attacking, defending) -> Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.POTION))
        );

        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .asTrainerBattle()
                        .attacking(ItemNamesies.RAWST_BERRY)
                        .fight(AttackNamesies.WILL_O_WISP, AttackNamesies.PLUCK),
                (battle, attacking, defending) -> {
                    defending.assertNoStatus();
                    attacking.assertConsumedItem(battle); // No eaten berry
                    defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                    defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
                },
                (battle, attacking, defending) -> {
                    defending.assertHasStatus(StatusNamesies.BURNED);
                    attacking.assertNotConsumedItem(battle);
                    defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                    defending.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
                }
        );

        // Status conditions cannot happen even from indirect sources like Fling
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .defending(ItemNamesies.FLAME_ORB)
                        .defendingFight(AttackNamesies.FLING)
                        .with((battle, attacking, defending) -> Assert.assertFalse(defending.isHoldingItem(battle))),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.BURNED),
                (battle, attacking, defending) -> attacking.assertNoStatus()
        );

        // Defog will remove effects without decreasing evasion
        substituteTest(
                new TestInfo()
                        .attackingFight(AttackNamesies.REFLECT)
                        .with((battle, attacking, defending) -> Assert.assertTrue(battle.getTrainer(attacking).hasEffect(TeamEffectNamesies.REFLECT)))
                        .defendingFight(AttackNamesies.DEFOG)
                        .with((battle, attacking, defending) -> Assert.assertFalse(battle.getTrainer(attacking).hasEffect(TeamEffectNamesies.REFLECT))),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(Stat.EVASION, -1)),
                (battle, attacking, defending) -> attacking.assertNoStages()
        );

        // Inferno should not burn if it doesn't break the substitute
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .defendingFight(AttackNamesies.INFERNO),
                (battle, attacking, defending) -> {
                    attacking.assertHasStatus(StatusNamesies.BURNED);
                    attacking.assertNotFullHealth();
                },
                (battle, attacking, defending) -> {
                    attacking.assertNoStatus();
                    attacking.assertFullHealth();
                }
        );

        // But it should if the substitute does get broken
        // Give attacking Magic Guard so it doesn't die from Burn damage
        substituteTest(
                true,
                new TestInfo(PokemonNamesies.WEEDLE, PokemonNamesies.XURKITREE)
                        .attacking(AbilityNamesies.MAGIC_GUARD)
                        .fight(AttackNamesies.ENDURE, AttackNamesies.INFERNO)
                        .with((battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.BURNED)),
                (battle, attacking, defending) -> Assert.assertEquals(1, attacking.getHP()),
                (battle, attacking, defending) -> attacking.assertFullHealth()
        );

        // Should not lose attack to Intimidate
        substituteTest(
                // Create a second Pokemon with Intimidate
                // Then use Roar to lure it out
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .asTrainerBattle()
                        .with((battle, attacking, defending) -> {
                            TestPokemon defending2 = TestPokemon.newTrainerPokemon(PokemonNamesies.SQUIRTLE);
                            defending2.withAbility(AbilityNamesies.INTIMIDATE);
                            ((EnemyTrainer)battle.getOpponent()).addPokemon(defending2);
                        })
                        .attackingFight(AttackNamesies.ROAR)
                        .with((battle, attacking, defending) -> {
                            // Need to use getDefending() since the defending var is set at the start of the turn
                            Assert.assertTrue(battle.getDefending().isPokemon(PokemonNamesies.SQUIRTLE));
                        }),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(Stat.ATTACK, -1)),
                (battle, attacking, defending) -> attacking.assertNoStages()
        );

        // Should not get poisoned from Toxic Spikes when Baton Passed
        substituteTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .with((battle, attacking, defending) -> {
                            TestPokemon attacking2 = TestPokemon.newPlayerPokemon(PokemonNamesies.SQUIRTLE);
                            battle.getPlayer().addPokemon(attacking2);
                        })
                        .defendingFight(AttackNamesies.TOXIC_SPIKES)
                        .attackingFight(AttackNamesies.BATON_PASS)
                        .with((battle, attacking, defending) -> {
                            Assert.assertTrue(battle.getAttacking().isPokemon(PokemonNamesies.SQUIRTLE));
                            Assert.assertTrue(battle.getTrainer(attacking).hasEffect(TeamEffectNamesies.TOXIC_SPIKES));
                        }),
                (battle, attacking, defending) -> attacking.assertRegularPoison(), // Only one layer
                (battle, attacking, defending) -> {
                    // TODO: Fix this in Baton Pass -- not currently working since it adds the effects AFTER it is already in battle so it doesn't work for EntryEffects like Toxic Spikes
//                    attacking.assertNoStatus();
                }
        );

        // Should still absorb Toxic Spikes for Baton Pass to grounded Poison Poke though
        substituteTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .with((battle, attacking, defending) -> {
                            TestPokemon attacking2 = TestPokemon.newPlayerPokemon(PokemonNamesies.GRIMER);
                            battle.getPlayer().addPokemon(attacking2);
                        })
                        .defendingFight(AttackNamesies.TOXIC_SPIKES)
                        .attackingFight(AttackNamesies.BATON_PASS),
                (battle, attacking, defending) -> {
                    Assert.assertTrue(attacking.isPokemon(PokemonNamesies.GRIMER));
                    Assert.assertFalse(battle.getTrainer(attacking).hasEffect(TeamEffectNamesies.TOXIC_SPIKES));
                    attacking.assertNoStatus();
                }
        );

        // Break the substitute -- user should still have full health
        substituteTest(
                true,
                new TestInfo(PokemonNamesies.HAPPINY, PokemonNamesies.KARTANA)
                        .fight(AttackNamesies.ENDURE, AttackNamesies.HEAD_SMASH),
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    Assert.assertEquals(1, attacking.getHP());
                },
                (battle, attacking, defending) -> {
                    // Recoil damage is calculated based on actual HP lost, so it will only take the minimum of 1 HP
                    attacking.assertFullHealth();
                    Assert.assertEquals(defending.getMaxHP() - 1, defending.getHP());
                    attacking.assertNoEffect(PokemonEffectNamesies.SUBSTITUTE);

                    // To make sure Endure doesn't fail again
                    battle.splashFight();

                    // No more substitute -- murder is fair game (except don't actualllly murder because it will heal the player)
                    battle.fight(AttackNamesies.ENDURE, AttackNamesies.EARTHQUAKE);
                    attacking.assertNotFullHealth();
                    Assert.assertEquals(1, attacking.getHP());
                }
        );
    }

    private void substituteTest(TestInfo testInfo, PokemonManipulator samesies) {
        substituteTest(testInfo, samesies, samesies);
    }

    private void substituteTest(TestInfo testInfo, PokemonManipulator without, PokemonManipulator with) {
        substituteTest(false, testInfo, without, with);
    }

    private void substituteTest(boolean broken, TestInfo testInfo, PokemonManipulator without, PokemonManipulator with) {
        testInfo.doubleTake(
                (battle, attacking, defending) -> {
                    battle.attackingFight(AttackNamesies.SUBSTITUTE);
                    attacking.assertHealthRatio(.75);

                    battle.emptyHeal();
                    attacking.assertFullHealth();
                    attacking.assertHasEffect(PokemonEffectNamesies.SUBSTITUTE);
                },
                (battle, attacking, defending) -> {
                    attacking.assertNoEffect(PokemonEffectNamesies.SUBSTITUTE);
                    without.manipulate(battle, attacking, defending);
                },
                (battle, attacking, defending) -> {
                    attacking.assertEffect(!broken, PokemonEffectNamesies.SUBSTITUTE);
                    with.manipulate(battle, attacking, defending);
                    attacking.assertEffect(!broken, PokemonEffectNamesies.SUBSTITUTE);
                }
        );
    }

    @Test
    public void stealthRockTest() {
        // Dual-type both resistant
        stealthRockTest(31/32.0, PokemonNamesies.LUCARIO, PokemonNamesies.EXCADRILL);

        // Single-type resistant
        stealthRockTest(15/16.0, PokemonNamesies.HITMONLEE, PokemonNamesies.SANDSHREW, PokemonNamesies.MAWILE);

        // Dual-type one resistant one neutral
        stealthRockTest(15/16.0, PokemonNamesies.MEDICHAM, PokemonNamesies.QUAGSIRE, PokemonNamesies.MAGNEMITE);

        // Single-type neutral
        stealthRockTest(7/8.0, PokemonNamesies.SQUIRTLE, PokemonNamesies.SUDOWOODO, PokemonNamesies.ESPEON);

        // Dual-type both neutral
        stealthRockTest(7/8.0, PokemonNamesies.BULBASAUR, PokemonNamesies.SPIRITOMB, PokemonNamesies.LANTURN);

        // Dual-type one weak, one resistant
        stealthRockTest(7/8.0, PokemonNamesies.INFERNAPE, PokemonNamesies.SWINUB, PokemonNamesies.SCIZOR, PokemonNamesies.SKARMORY);

        // Single-type weak
        stealthRockTest(3/4.0, PokemonNamesies.CHARMANDER, PokemonNamesies.GLACEON, PokemonNamesies.TORNADUS, PokemonNamesies.CATERPIE);

        // Dual-type one weak one neutral
        stealthRockTest(3/4.0, PokemonNamesies.CHANDELURE, PokemonNamesies.SNOVER, PokemonNamesies.SHUCKLE, PokemonNamesies.PIDGEOT);

        // Dual-type both weak
        stealthRockTest(1/2.0, PokemonNamesies.CHARIZARD, PokemonNamesies.ARTICUNO, PokemonNamesies.YANMEGA, PokemonNamesies.VOLCARONA);
    }

    private void stealthRockTest(double expectedHealthFraction, PokemonNamesies... notSoStealthy) {
        for (PokemonNamesies defendingPokemon : notSoStealthy) {
            stealthRockTest(expectedHealthFraction, defendingPokemon, AbilityNamesies.NO_ABILITY);
            stealthRockTest(1, defendingPokemon, AbilityNamesies.MAGIC_GUARD);
        }
    }

    private void stealthRockTest(double expectedHealthFraction, PokemonNamesies notSoStealthy, AbilityNamesies abilityNamesies) {
        TestBattle battle = TestBattle.createTrainerBattle(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending1 = battle.getDefending();
        TestPokemon defending2 = TestPokemon.newTrainerPokemon(notSoStealthy).withAbility(abilityNamesies);

        ((EnemyTrainer)battle.getOpponent()).addPokemon(defending2);
        Assert.assertTrue(battle.getDefending() == defending1);

        // Use Stealth Rock -- nothing should really happen
        battle.attackingFight(AttackNamesies.STEALTH_ROCK);
        attacking.assertFullHealth();
        defending1.assertFullHealth();

        // Send out the other Pokemon -- it won't be as stealthy as it thought
        battle.attackingFight(AttackNamesies.WHIRLWIND);
        Assert.assertTrue(battle.getDefending() == defending2);
        defending2.assertHealthRatio(expectedHealthFraction);
    }

    @Test
    public void grassyTerrainTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Reduce healthsies -- writing .75 and .5 this way for clarity later
        battle.fight(AttackNamesies.SUBSTITUTE, AttackNamesies.BELLY_DRUM);
        attacking.assertHealthRatio(12/16.0);
        defending.assertHealthRatio(8/16.0);

        // Grassy Terrain heals grounded Pokemon by 1/16 at the end of the turn
        battle.defendingFight(AttackNamesies.GRASSY_TERRAIN); // Terrain count: 5
        attacking.assertHealthRatio(13/16.0, 1);
        defending.assertHealthRatio(9/16.0, 1);

        // Fails because Bulby is behind a substitute
        battle.defendingFight(AttackNamesies.TELEKINESIS); // Terrain count: 4
        Assert.assertFalse(attacking.isLevitating(battle));
        attacking.assertNoEffect(PokemonEffectNamesies.TELEKINESIS);
        defending.assertNoEffect(PokemonEffectNamesies.TELEKINESIS);
        attacking.assertHealthRatio(14/16.0, 2);
        defending.assertHealthRatio(10/16.0, 2);

        // Bulby levitates with Magnet Rise -- should no longer heal from Grassy Terrain
        battle.attackingFight(AttackNamesies.MAGNET_RISE); // Terrain count: 3
        Assert.assertTrue(attacking.isLevitating(battle));
        attacking.assertHasEffect(PokemonEffectNamesies.MAGNET_RISE);
        defending.assertNoEffect(PokemonEffectNamesies.MAGNET_RISE);
        attacking.assertHealthRatio(14/16.0, 2);
        defending.assertHealthRatio(11/16.0, 3);

        // Grass Whistle will work against Substitute since it is sound-based
        // Note: If this seems random as shit it's because I thought Grassy Terrain only healed Grass-Pokemon
        // at first and I originally had Protean Charmander here but then left this because whatever
        attacking.withItem(ItemNamesies.CHESTO_BERRY);
        battle.defendingFight(AttackNamesies.GRASS_WHISTLE); // Terrain count: 2
        attacking.assertHasEffect(PokemonEffectNamesies.SUBSTITUTE);
        Assert.assertTrue(attacking.isLevitating(battle));
        attacking.assertNoStatus();
        Assert.assertFalse(attacking.isHoldingItem(battle)); // Chesto Berry consumed
        attacking.assertHealthRatio(14/16.0, 2);
        defending.assertHealthRatio(12/16.0, 4);
        Assert.assertTrue(battle.hasEffect(TerrainNamesies.GRASSY_TERRAIN));

        // Break the substitute
        // Terrain should be cleared at the end of that last turn (and should not have healed Charmander)
        battle.defendingFight(AttackNamesies.SHEER_COLD); // Terrain count: 1
        attacking.assertNoEffect(PokemonEffectNamesies.SUBSTITUTE);
        Assert.assertTrue(attacking.isLevitating(battle));
        attacking.assertHealthRatio(14/16.0, 2);
        defending.assertHealthRatio(12/16.0, 4);
        Assert.assertFalse(battle.hasEffect(TerrainNamesies.GRASSY_TERRAIN));

        // Make sure we don't heal at the end of this turn
        // Telekinesis should succeed since Substitute was broken
        battle.defendingFight(AttackNamesies.TELEKINESIS);
        attacking.assertHasEffect(PokemonEffectNamesies.TELEKINESIS);
        Assert.assertTrue(attacking.isLevitating(battle));
        attacking.assertHealthRatio(14/16.0, 2);
        defending.assertHealthRatio(12/16.0, 4);
    }

    @Test
    public void levitationTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Non-flying type Pokemon with no items/abilities/etc. will not be levitating
        Assert.assertFalse(attacking.isLevitating(battle));
        Assert.assertFalse(defending.isLevitating(battle));

        // Levitate ability gives levitation (who knew????)
        attacking.withAbility(AbilityNamesies.LEVITATE);
        Assert.assertTrue(attacking.isLevitating(battle));
        Assert.assertFalse(defending.isLevitating(battle));

        // Iron Ball will ground a levitating Pokemon
        attacking.withItem(ItemNamesies.IRON_BALL);
        Assert.assertFalse(attacking.isLevitating(battle));
        Assert.assertFalse(defending.isLevitating(battle));

        // Magnet Rise gives levitation
        battle.defendingFight(AttackNamesies.MAGNET_RISE);
        defending.assertHasEffect(PokemonEffectNamesies.MAGNET_RISE);
        Assert.assertFalse(attacking.isLevitating(battle));
        Assert.assertTrue(defending.isLevitating(battle));

        // But is removed when pelted with an Iron Ball (not true in actual games)
        battle.fight(AttackNamesies.FLING, AttackNamesies.ENDURE);
        attacking.assertConsumedItem(battle);
        Assert.assertFalse(defending.isHoldingItem(battle));
        defending.assertNoEffect(PokemonEffectNamesies.MAGNET_RISE);
        Assert.assertTrue(attacking.isLevitating(battle));
        Assert.assertFalse(defending.isLevitating(battle));

        // Air Balloon will give the levitation effect
        defending.withItem(ItemNamesies.AIR_BALLOON);
        Assert.assertTrue(attacking.isLevitating(battle));
        Assert.assertTrue(defending.isLevitating(battle));

        // Until it pops (triggered by being hit)
        battle.attackingFight(AttackNamesies.FALSE_SWIPE);
        Assert.assertTrue(attacking.isLevitating(battle));
        Assert.assertFalse(defending.isLevitating(battle));
        defending.assertConsumedItem(battle);

        // Make Charmander Flying-type (and therefore a master of levitation)
        defending.withAbility(AbilityNamesies.PROTEAN);
        battle.defendingFight(AttackNamesies.TAILWIND);
        Assert.assertTrue(defending.isType(battle, Type.FLYING));
        Assert.assertTrue(attacking.isLevitating(battle));
        Assert.assertTrue(defending.isLevitating(battle));

        // Give Iron Ball to Charmander -- make sure it removes levitation from flying type as well
        defending.giveItem(ItemNamesies.IRON_BALL);
        Assert.assertTrue(defending.isType(battle, Type.FLYING));
        Assert.assertTrue(attacking.isLevitating(battle));
        Assert.assertFalse(defending.isLevitating(battle));

        // Ingrain will ground the user (explicitly use Splash to remove Flying-type)
        defending.removeItem();
        battle.fight(AttackNamesies.INGRAIN, AttackNamesies.SPLASH);
        attacking.assertHasEffect(PokemonEffectNamesies.INGRAIN);
        Assert.assertFalse(defending.isType(battle, Type.FLYING));
        Assert.assertFalse(attacking.isLevitating(battle));
        Assert.assertFalse(defending.isLevitating(battle));
        Assert.assertTrue(attacking.isGrounded(battle));
        Assert.assertFalse(defending.isGrounded(battle));

        // Attacking shouldn't be able to fly when grounded
        attacking.apply(false, AttackNamesies.FLY, battle);

        // But the non-grounded defending can do whatever the fuck it wants
        defending.withAbility(AbilityNamesies.NO_ABILITY);
        battle.defendingFight(AttackNamesies.FLY);
        attacking.assertHasEffect(PokemonEffectNamesies.INGRAIN);
        Assert.assertFalse(defending.isType(battle, Type.FLYING));
        Assert.assertTrue(defending.isSemiInvulnerableFlying());
        Assert.assertFalse(attacking.isLevitating(battle));
        Assert.assertTrue(defending.isLevitating(battle));
        attacking.assertFullHealth();

        // Finish the second turn of Fly -- make sure it lands and no more levitation station
        // Need to use setMove so it doesn't overwrite Fly
        attacking.setMove(new Move(AttackNamesies.ENDURE));
        battle.fight();
        attacking.assertHasEffect(PokemonEffectNamesies.INGRAIN);
        Assert.assertFalse(defending.isType(battle, Type.FLYING));
        Assert.assertFalse(defending.isSemiInvulnerableFlying());
        Assert.assertFalse(attacking.isLevitating(battle));
        Assert.assertFalse(defending.isLevitating(battle));
        attacking.assertNotFullHealth();
    }

    @Test
    public void safeguardTest() {
        // Also should not work on self-confusion fatigue things
        safeguardTest(
                (battle, attacking, defending) -> {
                    // Thrash will attack for 2-3 turns and then confuse the user
                    attacking.setMove(new Move(AttackNamesies.RECOVER));
                    defending.setMove(new Move(AttackNamesies.THRASH));

                    battle.fight();
                    defending.assertHasEffect(PokemonEffectNamesies.SELF_CONFUSION);

                    battle.fight();

                    // If attacking still has the effect, then it's three turns -- do that turn
                    if (defending.hasEffect(PokemonEffectNamesies.SELF_CONFUSION)) {
                        battle.fight();
                    }

                    defending.assertNoEffect(PokemonEffectNamesies.SELF_CONFUSION);
                },
                (battle, attacking, defending) -> defending.assertHasEffect(PokemonEffectNamesies.CONFUSION)
        );

        // Safeguard prevents against status conditions (like poison)
        safeguardTest(
                (battle, attacking, defending) -> battle.attackingFight(AttackNamesies.TOXIC),
                (battle, attacking, defending) -> defending.assertBadPoison(),
                (battle, attacking, defending) -> defending.assertNoStatus()
        );

        // (And also burn)
        safeguardTest(
                (battle, attacking, defending) -> battle.attackingFight(AttackNamesies.WILL_O_WISP),
                (battle, attacking, defending) -> defending.assertHasStatus(StatusNamesies.BURNED),
                (battle, attacking, defending) -> defending.assertNoStatus()
        );

        // Safeguard protects against confusion also
        safeguardTest(
                (battle, attacking, defending) -> battle.attackingFight(AttackNamesies.CONFUSE_RAY),
                (battle, attacking, defending) -> defending.assertHasEffect(PokemonEffectNamesies.CONFUSION),
                (battle, attacking, defending) -> defending.assertNoEffect(PokemonEffectNamesies.CONFUSION)
        );

        // Unless the attacker has Infiltrator
        safeguardTest(
                (battle, attacking, defending) -> {
                    attacking.withAbility(AbilityNamesies.INFILTRATOR);
                    battle.attackingFight(AttackNamesies.WILL_O_WISP);
                },
                (battle, attacking, defending) -> defending.assertHasStatus(StatusNamesies.BURNED)
        );

        // For confusion too
        safeguardTest(
                (battle, attacking, defending) -> {
                    attacking.withAbility(AbilityNamesies.INFILTRATOR);
                    battle.attackingFight(AttackNamesies.CONFUSE_RAY);
                },
                (battle, attacking, defending) -> defending.assertHasEffect(PokemonEffectNamesies.CONFUSION)
        );

        // Safeguard does not prevent against self-inflicted statuses like Toxic Orb
        safeguardTest(
                (battle, attacking, defending) -> {
                    defending.giveItem(ItemNamesies.TOXIC_ORB);
                    battle.splashFight();
                },
                (battle, attacking, defending) -> defending.assertBadPoison()
        );

        // Same deal with Rest (False Swipe so that Rest doesn't fail for full health reasons)
        safeguardTest(
                (battle, attacking, defending) -> battle.fight(AttackNamesies.FALSE_SWIPE, AttackNamesies.REST),
                (battle, attacking, defending) -> defending.assertHasStatus(StatusNamesies.ASLEEP)
        );

        // Also should not work on self-confusion fatigue things
        safeguardTest(
                (battle, attacking, defending) -> {
                    // Thrash will attack for 2-3 turns and then confuse the user
                    attacking.setMove(new Move(AttackNamesies.THRASH));
                    defending.setMove(new Move(AttackNamesies.RECOVER));

                    battle.fight();
                    attacking.assertHasEffect(PokemonEffectNamesies.SELF_CONFUSION);

                    battle.fight();

                    // If attacking still has the effect, then it's three turns -- do that turn
                    if (attacking.hasEffect(PokemonEffectNamesies.SELF_CONFUSION)) {
                        battle.fight();
                    }

                    attacking.assertNoEffect(PokemonEffectNamesies.SELF_CONFUSION);
                },
                (battle, attacking, defending) -> attacking.hasEffect(PokemonEffectNamesies.CONFUSION)
        );
    }

    private void safeguardTest(PokemonManipulator manipulator, PokemonManipulator samesies) {
        safeguardTest(manipulator, samesies, samesies);
    }

    private void safeguardTest(PokemonManipulator manipulator, PokemonManipulator withoutManipulator, PokemonManipulator withManipulator) {
        PokemonManipulator safeguard = (battle, attacking, defending) -> {
            battle.defendingFight(AttackNamesies.SAFEGUARD);
            Assert.assertFalse(battle.getPlayer().hasEffect(TeamEffectNamesies.SAFEGUARD));
            Assert.assertTrue(battle.getOpponent().hasEffect(TeamEffectNamesies.SAFEGUARD));
        };

        new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                .with(manipulator)
                .doubleTake(safeguard, withoutManipulator, withManipulator);
    }
}
