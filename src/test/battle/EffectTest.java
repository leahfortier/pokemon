package test.battle;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.Effect;
import battle.effect.Effect.CastMessageGetter;
import battle.effect.EffectInterfaces.EndTurnSubsider;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.BarrierEffect;
import battle.effect.InvokeInterfaces.BattleEndTurnEffect;
import battle.effect.battle.StandardBattleEffectNamesies;
import battle.effect.battle.terrain.TerrainNamesies;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffect;
import battle.effect.team.TeamEffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import pokemon.stat.User;
import test.general.BaseTest;
import test.general.TestUtils;
import test.pokemon.TestPokemon;
import trainer.TrainerAction;
import trainer.player.Player;
import type.Type;
import util.GeneralUtils;

import java.util.HashSet;

public class EffectTest extends BaseTest {
    @Test
    public void alternateCastTest() {
        for (EffectNamesies effectNamesies : EffectNamesies.values()) {
            Effect effect = effectNamesies.getEffect();

            // True if the effect overrides the alternateCast method
            if (GeneralUtils.hasDeclaredMethod(
                    effect.getClass(),
                    "alternateCast",
                    Battle.class,
                    ActivePokemon.class,
                    ActivePokemon.class,
                    CastSource.class,
                    CastMessageGetter.class
            )) {
                // If it has the alternateCast method, then hasAlternateCast MUST be true
                Assert.assertTrue(effect.hasAlternateCast());

                // In order to have an alternate cast, they need to be able to have the effect
                Assert.assertTrue(effect.canHave());
            } else {
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
    public void barrierTest() {
        for (EffectNamesies effectNamesies : EffectNamesies.values()) {
            Effect effect = effectNamesies.getEffect();
            if (effect instanceof BarrierEffect) {
                String name = effectNamesies.name();
                BarrierEffect barrierEffect = (BarrierEffect)effect;

                // Barrier Effects can only be TeamEffects
                Assert.assertTrue(name, barrierEffect instanceof TeamEffect);

                // Barrier effects can only boost defensive stats
                boolean hasIncrease = false;
                for (Stat stat : Stat.BATTLE_STATS) {
                    if (barrierEffect.isModifyStat(stat)) {
                        hasIncrease = true;
                        Assert.assertTrue(name + " " + stat.getName(), stat == Stat.DEFENSE || stat == Stat.SP_DEFENSE);
                    }
                }
                Assert.assertTrue(name, hasIncrease);
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

        // King's Shield lowers attack when contact was made and only protects against non-status moves
        // TODO: Should probably test with Sucker Punch
        checkProtect(true, AttackNamesies.KINGS_SHIELD, AttackNamesies.TACKLE,
                     (battle, attacking, defending) -> defending.assertStages(new TestStages().set(-1, Stat.ATTACK))
        );
        checkProtect(true, AttackNamesies.KINGS_SHIELD, AttackNamesies.WATER_GUN,
                     (battle, attacking, defending) -> defending.assertStages(new TestStages())
        );
        checkProtect(false, AttackNamesies.KINGS_SHIELD, AttackNamesies.TOXIC,
                     (battle, attacking, defending) -> {
                         attacking.assertBadPoison();
                         defending.assertStages(new TestStages());
                     }
        );
        checkProtect(false, AttackNamesies.KINGS_SHIELD, AttackNamesies.CONFUSE_RAY,
                     (battle, attacking, defending) -> {
                         attacking.assertHasEffect(PokemonEffectNamesies.CONFUSION);
                         defending.assertStages(new TestStages());
                     }
        );

        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        battle.fight(AttackNamesies.PROTECT, AttackNamesies.SCREECH);
        attacking.assertStages(new TestStages());
        defending.assertStages(new TestStages());

        // Make sure wears off by the next turn
        battle.defendingFight(AttackNamesies.SCREECH);
        attacking.assertStages(new TestStages().set(-2, Stat.DEFENSE));
        defending.assertStages(new TestStages());
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
        TestBattle battle = TestBattle.create(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE);
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
            attacking.assertStages(new TestStages());
            Assert.assertTrue(attacking.getEffects().asList().isEmpty());
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
        attacking.assertType(battle, Type.GHOST);
        Assert.assertTrue(attacking.canEscape(battle));

        // Arena trap only works non-levitating Pokemon
        defending.withAbility(AbilityNamesies.ARENA_TRAP);
        Assert.assertTrue(attacking.canEscape(battle));
        battle.attackingFight(AttackNamesies.TAILWIND);
        attacking.assertType(battle, Type.FLYING);
        Assert.assertTrue(attacking.canEscape(battle));
        attacking.giveItem(ItemNamesies.IRON_BALL);
        Assert.assertFalse(attacking.canEscape(battle));
        attacking.removeItem();
        Assert.assertTrue(attacking.canEscape(battle));

        // Only steel-type Pokemon cannot escape from a Pokemon with Magnet Pull
        defending.withAbility(AbilityNamesies.MAGNET_PULL);
        Assert.assertTrue(attacking.canEscape(battle));
        battle.attackingFight(AttackNamesies.GEAR_UP);
        attacking.assertType(battle, Type.STEEL);
        Assert.assertFalse(attacking.canEscape(battle));
        battle.attackingFight(AttackNamesies.HAZE);
        attacking.assertNotType(battle, Type.STEEL);
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

        battle.defendingFight(AttackNamesies.FAIRY_LOCK);
        Assert.assertFalse(attacking.canEscape(battle));
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
            String sourceName = source.getSourceName(attacking);
            if (source.hasSourceName()) {
                // Important since these values are hard-coded in another method
                Assert.assertTrue(source == CastSource.ABILITY || source == CastSource.HELD_ITEM);
                Assert.assertNotNull(sourceName);
            } else {
                Assert.assertNull(sourceName);
            }
        }

        Assert.assertEquals(AbilityNamesies.OVERGROW.getName(), CastSource.ABILITY.getSourceName(attacking));
        Assert.assertEquals(ItemNamesies.ORAN_BERRY.getName(), CastSource.HELD_ITEM.getSourceName(attacking));

        Assert.assertSame(attacking.getAbility(), CastSource.ABILITY.getSource(attacking));
        Assert.assertSame(attacking.getHeldItem(), CastSource.HELD_ITEM.getSource(attacking));
        Assert.assertSame(attacking.getAttack(), CastSource.ATTACK.getSource(attacking));
        Assert.assertSame(attacking.getCastSource(), CastSource.CAST_SOURCE.getSource(attacking));
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
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(-1, Stat.DEFENSE)),
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
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(-1, Stat.ATTACK))
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
                    attacking.assertNotHoldingItem();
                    defending.assertHoldingItem(ItemNamesies.POTION);
                },
                (battle, attacking, defending) -> {
                    Assert.assertFalse(defending.lastMoveSucceeded());
                    attacking.assertHoldingItem(ItemNamesies.POTION);
                    defending.assertNotHoldingItem();
                }
        );

        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .asTrainerBattle()
                        .attacking(ItemNamesies.POTION)
                        .defendingFight(AttackNamesies.KNOCK_OFF),
                (battle, attacking, defending) -> attacking.assertNotHoldingItem(),
                (battle, attacking, defending) -> attacking.assertHoldingItem(ItemNamesies.POTION)
        );

        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .asTrainerBattle()
                        .attacking(ItemNamesies.RAWST_BERRY)
                        .fight(AttackNamesies.WILL_O_WISP, AttackNamesies.PLUCK),
                (battle, attacking, defending) -> {
                    defending.assertNoStatus();
                    attacking.assertConsumedItem(); // No eaten berry
                    defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                    defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
                },
                (battle, attacking, defending) -> {
                    defending.assertHasStatus(StatusNamesies.BURNED);
                    attacking.assertNotConsumedItem();
                    defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                    defending.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
                }
        );

        // Status conditions cannot happen even from indirect sources like Fling
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .defending(ItemNamesies.FLAME_ORB)
                        .defendingFight(AttackNamesies.FLING)
                        .with((battle, attacking, defending) -> Assert.assertFalse(defending.isHoldingItem())),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.BURNED),
                (battle, attacking, defending) -> attacking.assertNoStatus()
        );

        // Defog will remove effects without decreasing evasion
        substituteTest(
                new TestInfo()
                        .attackingFight(AttackNamesies.REFLECT)
                        .with((battle, attacking, defending) -> battle.assertHasEffect(attacking, TeamEffectNamesies.REFLECT))
                        .defendingFight(AttackNamesies.DEFOG)
                        .with((battle, attacking, defending) -> battle.assertNoEffect(attacking, TeamEffectNamesies.REFLECT)),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(-1, Stat.EVASION)),
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
                (battle, attacking, defending) -> attacking.assertHp(1),
                (battle, attacking, defending) -> attacking.assertFullHealth()
        );

        // Should not lose attack to Intimidate
        substituteTest(
                // Create a second Pokemon with Intimidate
                // Then use Roar to lure it out
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .asTrainerBattle()
                        .with((battle, attacking, defending) -> battle.addDefending(PokemonNamesies.SQUIRTLE)
                                                                      .withAbility(AbilityNamesies.INTIMIDATE))
                        .attackingFight(AttackNamesies.ROAR)
                        .with((battle, attacking, defending) -> {
                            // Note: Need to use getDefending() since the defending var is set at the start of the turn
                            Assert.assertTrue(battle.getDefending().isPokemon(PokemonNamesies.SQUIRTLE));
                        }),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(-1, Stat.ATTACK)),
                (battle, attacking, defending) -> attacking.assertNoStages()
        );

        // Should not get poisoned from Toxic Spikes when Baton Passed
        substituteTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .with((battle, attacking, defending) -> battle.addAttacking(PokemonNamesies.SQUIRTLE))
                        .defendingFight(AttackNamesies.TOXIC_SPIKES)
                        .attackingFight(AttackNamesies.BATON_PASS)
                        .with((battle, attacking, defending) -> {
                            Assert.assertTrue(battle.getAttacking().isPokemon(PokemonNamesies.SQUIRTLE));
                            battle.assertHasEffect(attacking, TeamEffectNamesies.TOXIC_SPIKES);
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
                        .with((battle, attacking, defending) -> battle.addAttacking(PokemonNamesies.GRIMER))
                        .defendingFight(AttackNamesies.TOXIC_SPIKES)
                        .attackingFight(AttackNamesies.BATON_PASS),
                (battle, attacking, defending) -> {
                    Assert.assertTrue(attacking.isPokemon(PokemonNamesies.GRIMER));
                    battle.assertNoEffect(attacking, TeamEffectNamesies.TOXIC_SPIKES);
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
                    attacking.assertHp(1);
                },
                (battle, attacking, defending) -> {
                    // Recoil damage is calculated based on actual HP lost, so it will only take the minimum of 1 HP
                    attacking.assertFullHealth();
                    defending.assertMissingHp(1);
                    attacking.assertNoEffect(PokemonEffectNamesies.SUBSTITUTE);

                    // To make sure Endure doesn't fail again
                    battle.splashFight();

                    // No more substitute -- murder is fair game (except don't actualllly murder because it will heal the player)
                    battle.fight(AttackNamesies.ENDURE, AttackNamesies.EARTHQUAKE);
                    attacking.assertNotFullHealth();
                    attacking.assertHp(1);
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
        TestPokemon defending2 = battle.addDefending(notSoStealthy).withAbility(abilityNamesies);
        Assert.assertSame(battle.getDefending(), defending1);

        // Use Stealth Rock -- nothing should really happen
        battle.attackingFight(AttackNamesies.STEALTH_ROCK);
        attacking.assertFullHealth();
        defending1.assertFullHealth();

        // Send out the other Pokemon -- it won't be as stealthy as it thought
        battle.attackingFight(AttackNamesies.WHIRLWIND);
        Assert.assertSame(battle.getDefending(), defending2);
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
        attacking.assertNotHoldingItem(); // Chesto Berry consumed
        attacking.assertHealthRatio(14/16.0, 2);
        defending.assertHealthRatio(12/16.0, 4);
        battle.assertHasEffect(TerrainNamesies.GRASSY_TERRAIN);

        // Break the substitute
        // Terrain should be cleared at the end of that last turn (and should not have healed Charmander)
        battle.defendingFight(AttackNamesies.SHEER_COLD); // Terrain count: 1
        attacking.assertNoEffect(PokemonEffectNamesies.SUBSTITUTE);
        Assert.assertTrue(attacking.isLevitating(battle));
        attacking.assertHealthRatio(14/16.0, 2);
        defending.assertHealthRatio(12/16.0, 4);
        battle.assertNoEffect(TerrainNamesies.GRASSY_TERRAIN);

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
        attacking.assertConsumedItem();
        defending.assertNotHoldingItem();
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
        defending.assertConsumedItem();

        // Make Charmander Flying-type (and therefore a master of levitation)
        defending.withAbility(AbilityNamesies.PROTEAN);
        battle.defendingFight(AttackNamesies.TAILWIND);
        defending.assertType(battle, Type.FLYING);
        Assert.assertTrue(attacking.isLevitating(battle));
        Assert.assertTrue(defending.isLevitating(battle));

        // Give Iron Ball to Charmander -- make sure it removes levitation from flying type as well
        defending.giveItem(ItemNamesies.IRON_BALL);
        defending.assertType(battle, Type.FLYING);
        Assert.assertTrue(attacking.isLevitating(battle));
        Assert.assertFalse(defending.isLevitating(battle));

        // Ingrain will ground the user (explicitly use Splash to remove Flying-type)
        defending.removeItem();
        battle.fight(AttackNamesies.INGRAIN, AttackNamesies.SPLASH);
        attacking.assertHasEffect(PokemonEffectNamesies.INGRAIN);
        defending.assertNotType(battle, Type.FLYING);
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
        defending.assertNotType(battle, Type.FLYING);
        Assert.assertTrue(defending.isSemiInvulnerableFlying());
        Assert.assertFalse(attacking.isLevitating(battle));
        Assert.assertTrue(defending.isLevitating(battle));
        attacking.assertFullHealth();

        // Finish the second turn of Fly -- make sure it lands and no more levitation station
        // Need to use setMove so it doesn't overwrite Fly
        attacking.setMove(new Move(AttackNamesies.ENDURE));
        battle.fight();
        attacking.assertHasEffect(PokemonEffectNamesies.INGRAIN);
        defending.assertNotType(battle, Type.FLYING);
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
            battle.assertNoEffect(attacking, TeamEffectNamesies.SAFEGUARD);
            battle.assertHasEffect(defending, TeamEffectNamesies.SAFEGUARD);
        };

        new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                .with(manipulator)
                .doubleTake(safeguard, withoutManipulator, withManipulator);
    }

    @Test
    public void transformTest() {
        TestInfo testInfo = new TestInfo(PokemonNamesies.DITTO, PokemonNamesies.PIKACHU)
                .attacking(ItemNamesies.QUICK_POWDER)
                .defending(ItemNamesies.LIGHT_BALL)
                .with((battle, attacking, defending) -> {
                    // Confirm no Imposter problems
                    attacking.assertAbility(AbilityNamesies.NO_ABILITY);
                    attacking.assertNoEffect(PokemonEffectNamesies.TRANSFORMED);

                    attacking.assertSpecies(PokemonNamesies.DITTO);
                    defending.assertSpecies(PokemonNamesies.PIKACHU);
                });

        // Quick Powder increases Speed by 50% for Ditto
        testInfo.statModifierTest(1.5, Stat.SPEED, User.ATTACKING);
        testInfo.statModifierTest(1, Stat.SPEED, User.DEFENDING);

        // Light Ball doubles Pikachu's Attack and Sp. Attack
        testInfo.statModifierTest(1, Stat.ATTACK, User.ATTACKING);
        testInfo.statModifierTest(1, Stat.SP_ATTACK, User.ATTACKING);
        testInfo.statModifierTest(2, Stat.ATTACK, User.DEFENDING);
        testInfo.statModifierTest(2, Stat.SP_ATTACK, User.DEFENDING);

        // Transform time!
        testInfo.attackingFight(AttackNamesies.TRANSFORM)
                .with((battle, attacking, defending) -> {
                    attacking.assertHasEffect(PokemonEffectNamesies.TRANSFORMED);
                    defending.assertNoEffect(PokemonEffectNamesies.TRANSFORMED);

                    attacking.assertSpecies(PokemonNamesies.PIKACHU);
                    defending.assertSpecies(PokemonNamesies.PIKACHU);

                    attacking.assertHoldingItem(ItemNamesies.QUICK_POWDER);
                    defending.assertHoldingItem(ItemNamesies.LIGHT_BALL);
                });

        // Not a Ditto anymore so Quick Powder shouldn't work
        testInfo.statModifierTest(1, Stat.SPEED, User.ATTACKING);
        testInfo.statModifierTest(1, Stat.SPEED, User.DEFENDING);

        // But is a Pikachu, but not holding Light Ball so that shouldn't work still
        testInfo.statModifierTest(1, Stat.ATTACK, User.ATTACKING);
        testInfo.statModifierTest(1, Stat.SP_ATTACK, User.ATTACKING);
        testInfo.statModifierTest(2, Stat.ATTACK, User.DEFENDING);
        testInfo.statModifierTest(2, Stat.SP_ATTACK, User.DEFENDING);

        // Switch items!
        testInfo.attackingFight(AttackNamesies.TRICK)
                .with((battle, attacking, defending) -> {
                    attacking.assertHasEffect(PokemonEffectNamesies.TRANSFORMED);

                    attacking.assertSpecies(PokemonNamesies.PIKACHU);
                    defending.assertSpecies(PokemonNamesies.PIKACHU);

                    defending.assertHoldingItem(ItemNamesies.QUICK_POWDER);
                    attacking.assertHoldingItem(ItemNamesies.LIGHT_BALL);
                });

        // Regular Pikachu is holding Quick Powder now -- should do nothing
        testInfo.statModifierTest(1, Stat.SPEED, User.ATTACKING);
        testInfo.statModifierTest(1, Stat.SPEED, User.DEFENDING);

        // Holding Light Ball now and is a (transformed) Pikachu, so should be stronger
        testInfo.statModifierTest(2, Stat.ATTACK, User.ATTACKING);
        testInfo.statModifierTest(2, Stat.SP_ATTACK, User.ATTACKING);
        testInfo.statModifierTest(1, Stat.ATTACK, User.DEFENDING);
        testInfo.statModifierTest(1, Stat.SP_ATTACK, User.DEFENDING);
    }

    @Test
    public void weightChangeTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.ROSERADE, PokemonNamesies.SCOLIPEDE);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Roserade is 32 lbs, Scolipede is 442 lbs
        assertWeight(battle, 32, 442);

        // Light Metal halves weight
        attacking.withAbility(AbilityNamesies.LIGHT_METAL);
        assertWeight(battle, 16, 442);

        // Heavy Metal doubles weight
        defending.withAbility(AbilityNamesies.HEAVY_METAL);
        assertWeight(battle, 16, 884);

        // Float Stone halves weight (stacks with Light Metal)
        attacking.withItem(ItemNamesies.FLOAT_STONE);
        assertWeight(battle, 8, 884);

        // Float Stone should cancel Heavy Metal out for its normal weight
        defending.withItem(ItemNamesies.FLOAT_STONE);
        assertWeight(battle, 8, 442);

        // Autotomize halves the weight and can stack multiple times
        battle.attackingFight(AttackNamesies.AUTOTOMIZE);
        assertWeight(battle, 4, 442);
        attacking.assertStages(new TestStages().set(2, Stat.SPEED));
        defending.assertStages(new TestStages());

        battle.defendingFight(AttackNamesies.AUTOTOMIZE);
        assertWeight(battle, 4, 221);
        attacking.assertStages(new TestStages().set(2, Stat.SPEED));
        defending.assertStages(new TestStages().set(2, Stat.SPEED));

        battle.attackingFight(AttackNamesies.AUTOTOMIZE);
        assertWeight(battle, 2, 221);
        attacking.assertStages(new TestStages().set(4, Stat.SPEED));
        defending.assertStages(new TestStages().set(2, Stat.SPEED));

        battle.attackingFight(AttackNamesies.AUTOTOMIZE);
        assertWeight(battle, 1, 221);
        attacking.assertStages(new TestStages().set(6, Stat.SPEED));
        defending.assertStages(new TestStages().set(2, Stat.SPEED));

        // Snatcher will take the half weight
        battle.fight(AttackNamesies.AUTOTOMIZE, AttackNamesies.SNATCH);
        assertWeight(battle, 1, 110.5);
        attacking.assertStages(new TestStages().set(6, Stat.SPEED));
        defending.assertStages(new TestStages().set(4, Stat.SPEED));

        // Autotomize halved weight even if the Speed is maxed
        battle.attackingFight(AttackNamesies.AUTOTOMIZE);
        assertWeight(battle, .5, 110.5);
        attacking.assertStages(new TestStages().set(6, Stat.SPEED));
        defending.assertStages(new TestStages().set(4, Stat.SPEED));

        // Float Stone no longer working
        battle.attackingFight(AttackNamesies.MAGIC_ROOM);
        assertWeight(battle, 1, 221);
        attacking.assertStages(new TestStages().set(6, Stat.SPEED));
        defending.assertStages(new TestStages().set(4, Stat.SPEED));
        battle.assertHasEffect(StandardBattleEffectNamesies.MAGIC_ROOM);

        // Defending no longer has Heavy Metal
        battle.attackingFight(AttackNamesies.GASTRO_ACID);
        assertWeight(battle, 1, 110.5);
        attacking.assertAbility(AbilityNamesies.LIGHT_METAL);
        defending.assertChangedAbility(AbilityNamesies.NO_ABILITY);
        battle.assertHasEffect(StandardBattleEffectNamesies.MAGIC_ROOM);

        // Attacking no longer has Light Metal
        battle.defendingFight(AttackNamesies.SIMPLE_BEAM);
        assertWeight(battle, 2, 110.5);
        attacking.assertChangedAbility(AbilityNamesies.SIMPLE);
        defending.assertChangedAbility(AbilityNamesies.NO_ABILITY);
        battle.assertHasEffect(StandardBattleEffectNamesies.MAGIC_ROOM);
    }

    private void assertWeight(TestBattle battle, double attackingWeight, double defendingWeight) {
        TestUtils.assertEquals(attackingWeight, battle.getAttacking().getWeight(battle));
        TestUtils.assertEquals(defendingWeight, battle.getDefending().getWeight(battle));
    }

    @Test
    public void octolockTest() {
        // Basic cannot escape and lowered stats
        octolockTest(AbilityNamesies.OVERGROW);
        octolockTest(AbilityNamesies.MOLD_BREAKER);

        // Clear Body should make the Pokemon still unable to escape, but stats should not be lowered
        octolockTest(AbilityNamesies.CLEAR_BODY, new TestStages());

        // Big Pecks only prevents loss from Defense
        octolockTest(AbilityNamesies.BIG_PECKS, new TestStages().set(-1, Stat.SP_DEFENSE));

        // Contrary should be increasing defenses each turn
        octolockTest(AbilityNamesies.CONTRARY, new TestStages().set(1, Stat.DEFENSE, Stat.SP_DEFENSE));

        // Simple should be doubling the decreases each turn
        octolockTest(AbilityNamesies.SIMPLE, new TestStages().set(-2, Stat.DEFENSE, Stat.SP_DEFENSE));

        // Defiant/Competitive sharply increases Attack/Sp. Attack for each reduction
        octolockTest(AbilityNamesies.DEFIANT, new TestStages().set(-1, Stat.DEFENSE, Stat.SP_DEFENSE).set(4, Stat.ATTACK));
        octolockTest(AbilityNamesies.COMPETITIVE, new TestStages().set(-1, Stat.DEFENSE, Stat.SP_DEFENSE).set(4, Stat.SP_ATTACK));

        // Magic Bounce should reflect the Octolock back onto attacking Pokemon unless user has Mold Breaker
        octolockTest(AbilityNamesies.MAGIC_BOUNCE, true, (battle, attacking, defending) -> {
            Assert.assertFalse(attacking.canEscape(battle));
            Assert.assertTrue(defending.canEscape(battle));

            attacking.assertStages(new TestStages().set(-1, Stat.DEFENSE, Stat.SP_DEFENSE));
            defending.assertStages(new TestStages());
        });
    }

    private void octolockTest(AbilityNamesies defendingAbility) {
        octolockTest(defendingAbility, new TestStages().set(-1, Stat.DEFENSE, Stat.SP_DEFENSE));
    }

    private void octolockTest(AbilityNamesies defendingAbility, TestStages defendingStages) {
        octolockTest(defendingAbility, false, octolockCheck(defendingStages));
    }

    // relevantMoldBreaker is true when Octolock is affected by mold breaker (defaults to false for the most part)
    private void octolockTest(AbilityNamesies defendingAbility, boolean relevantMoldBreaker, PokemonManipulator withAbility) {
        PokemonManipulator withoutAbility = octolockCheck(new TestStages().set(-1, Stat.DEFENSE, Stat.SP_DEFENSE));

        // Test with and without the specified ability on the defending Pokemon
        new TestInfo().attackingFight(AttackNamesies.OCTOLOCK)
                      .doubleTake(defendingAbility, withoutAbility, withAbility);

        // Test with and without Mold Breaker for the attacking Pokemon, defending Pokemon always has input ability
        // Note: not using the ability double take method since the ability is on the attacking
        new TestInfo().defending(defendingAbility)
                      .attackingFight(AttackNamesies.OCTOLOCK)
                      .doubleTake(
                              (battle, attacking, defending) -> attacking.withAbility(AbilityNamesies.MOLD_BREAKER),
                              withAbility,
                              relevantMoldBreaker ? withoutAbility : withAbility
                      );
    }

    private PokemonManipulator octolockCheck(TestStages defendingStages) {
        return (battle, attacking, defending) -> {
            Assert.assertTrue(attacking.canEscape(battle));
            Assert.assertFalse(defending.canEscape(battle));

            attacking.assertStages(new TestStages());
            defending.assertStages(defendingStages);
        };
    }

    @Test
    public void confusionDamageTest() {
        // n = 2 because 50% to hurt self in confusion
        int n = 2;
        int numTrials = GeneralUtils.numTrials(.99999, 2);
        Assert.assertEquals(25, numTrials);

        // Each confusion check adds its case numbers to completed, if more are added increase this number
        // Each case adds two numbers because it checks for both being confused and not being confused
        HashSet<Integer> completed = new HashSet<>();
        int numCases = 2;

        for (int i = 0; i < numCases*numTrials; i++) {
            checkConfusionDamage(completed);

            // Only need to pass each case once
            if (completed.size() == n*numCases) {
                // Additional caution that the numCases value does not need to be updated
                for (int j = 0; j < n*numCases; j++) {
                    Assert.assertTrue(completed.contains(j));
                }
                return;
            }
        }

        Assert.fail("Target never hurt themselves in confusion after " + numTrials + " trials.");
    }

    private void checkConfusionDamage(HashSet<Integer> completed) {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.SQUIRTLE);
        TestPokemon defending = battle.getDefending();

        // Possible for the defending Pokemon to hurt themselves in confusion the same turn being confused
        // If so, Growl will be unsuccessful and won't lower attack
        battle.fight(AttackNamesies.CONFUSE_RAY, AttackNamesies.GROWL);
        defending.assertHasEffect(PokemonEffectNamesies.CONFUSION);
        checkConfusionDamage(0, completed, battle, new TestStages().set(-1, Stat.ATTACK));

        // Reset health and stages for simplicity in next check
        defending.fullyHeal();
        defending.assertFullHealth();
        attacking.getStages().reset();
        attacking.assertStages(new TestStages());

        // Manually switch to other Pokemon and potentially hurt self during a switch turn
        // Note: If this seems like a super random test case it's motivated from a bug which caused an NPE checking
        // the switching Pokemon's stages when confusion damage should be using itself as the attacking and defending
        Player player = ((Player)battle.getPlayer());
        defending.withMoves(AttackNamesies.TAIL_WHIP);
        player.setSwitchIndex(1);
        player.performAction(battle, TrainerAction.SWITCH);
        battle.assertFront(attacking2);
        checkConfusionDamage(1, completed, battle, new TestStages().set(-1, Stat.DEFENSE));
    }

    // Checks if the defending Pokemon hurt themselves in confusion
    // Adds the appropriate case num to completed for both scenarios
    // Confirms the correct stages for the attacking Pokemon
    private void checkConfusionDamage(int caseNum, HashSet<Integer> completed, TestBattle battle, TestStages attackingStages) {
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // Did not hurt self in confusion -- move would be executed and attacking stages should change
        if (defending.fullHealth()) {
            completed.add(2*caseNum);
            attacking.assertStages(attackingStages);
        } else {
            completed.add(2*caseNum + 1);
            attacking.assertStages(new TestStages());
        }
    }

    @Test
    public void perishSongTest() {
        perishSongTest(true, true, AttackNamesies.PERISH_SONG, new TestInfo());

        // Soundproof doesn't hear the Perish Song
        perishSongTest(false, true, AttackNamesies.PERISH_SONG, new TestInfo().attacking(AbilityNamesies.SOUNDPROOF));
        perishSongTest(true, false, AttackNamesies.PERISH_SONG, new TestInfo().defending(AbilityNamesies.SOUNDPROOF));
        perishSongTest(false, false, AttackNamesies.PERISH_SONG, new TestInfo().attacking(AbilityNamesies.SOUNDPROOF).defending(AbilityNamesies.SOUNDPROOF));

        // Substitute is not affected by Perish Song
        perishSongTest(true, true, AttackNamesies.PERISH_SONG, new TestInfo().attackingFight(AttackNamesies.SUBSTITUTE));
        perishSongTest(true, true, AttackNamesies.PERISH_SONG, new TestInfo().defendingFight(AttackNamesies.SUBSTITUTE));

        // Making contact with Perish Body causes the Perish Song effect
        perishSongTest(true, true, AttackNamesies.TACKLE, new TestInfo().defending(AbilityNamesies.PERISH_BODY));
        perishSongTest(false, false, AttackNamesies.SWIFT, new TestInfo().defending(AbilityNamesies.PERISH_BODY));

        // Soundproof is not relevant to Perish Body
        perishSongTest(true, true, AttackNamesies.TACKLE, new TestInfo().attacking(AbilityNamesies.SOUNDPROOF).defending(AbilityNamesies.PERISH_BODY));

        // Pokemon with Long Reach do not make contact
        // Only doing the standard test here since the swapping isn't very helpful
        perishSongStandardTest(false, false, AttackNamesies.TACKLE, new TestInfo().attacking(AbilityNamesies.LONG_REACH).defending(AbilityNamesies.PERISH_BODY));

        // Nothing happens when a Pokemon with Perish Body contacts the other Pokemon
        perishSongTest(false, false, AttackNamesies.TACKLE, new TestInfo().attacking(AbilityNamesies.PERISH_BODY));

        // Protect does not protect against Perish Song
        TestBattle battle = TestBattle.create();
        battle.fight(AttackNamesies.PERISH_SONG, AttackNamesies.PROTECT);
        checkPerishing(battle, true, true);
    }

    private void perishSongTest(boolean killAttacking, boolean killDefending, AttackNamesies perishing, TestInfo testInfo) {
        perishSongStandardTest(killAttacking, killDefending, perishing, testInfo);
        perishSongSwapTest(killAttacking, killDefending, perishing, testInfo);
    }

    // Tests Perish Song/Body for the same count (both perish at the same time)
    private void perishSongStandardTest(boolean killAttacking, boolean killDefending, AttackNamesies perishing, TestInfo testInfo) {
        TestBattle battle = testInfo.asTrainerBattle().createBattle();
        TestPokemon attacking1 = battle.getAttacking();
        TestPokemon defending1 = battle.getDefending();
        TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.EEVEE);
        TestPokemon defending2 = battle.addDefending(PokemonNamesies.SQUIRTLE);

        testInfo.manipulate(battle);

        battle.fight(perishing, AttackNamesies.ENDURE);
        checkPerishing(battle, killAttacking, killDefending);

        battle.splashFight();
        checkPerishing(battle, killAttacking, killDefending);

        battle.splashFight();
        checkPerished(killAttacking, battle, attacking1, attacking2);
        checkPerished(killDefending, battle, defending1, defending2);
    }

    // Tests Perish Song/Body for different counts (Pokemon perish on different turns)
    private void perishSongSwapTest(boolean killAttacking, boolean killDefending, AttackNamesies perishing, TestInfo testInfo) {
        TestBattle battle = testInfo.asTrainerBattle().createBattle();
        TestPokemon attacking1 = battle.getAttacking();
        TestPokemon defending1 = battle.getDefending();
        TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.EEVEE);
        TestPokemon defending2 = battle.addDefending(PokemonNamesies.SQUIRTLE).withAbility(AbilityNamesies.SOUNDPROOF);

        testInfo.manipulate(battle);

        // Perish both Pokemon, but then swap the attacking Pokemon (resets Perish Song)
        battle.fight(perishing, AttackNamesies.WHIRLWIND);
        checkPerishing(battle, false, killDefending);

        // For the most part new attacking Pokemon always receives the song because no effects on it
        // Perish Body is generally the same rules though regardless of attacking Pokemon (unless special cased like Long Reach)
        killAttacking |= perishing == AttackNamesies.PERISH_SONG;

        // Perish again with the new attacking Pokemon out front
        battle.fight(perishing, AttackNamesies.ENDURE);
        checkPerishing(battle, killAttacking, killDefending);

        // Baton Pass preserves Perish Song (regardless if new Pokemon is immune etc) and will kill this turn
        // Perish count is zero for defending Pokemon, but attacking still has another turn
        battle.defendingFight(AttackNamesies.BATON_PASS);
        checkPerishing(battle, killAttacking, false);
        checkPerished(killDefending, battle, defending2, defending1);

        // Nowwwww we both perished
        battle.splashFight();
        checkPerished(killAttacking, battle, attacking2, attacking1);
        checkPerished(killDefending, battle, defending2, defending1);
    }

    private void checkPerishing(TestBattle battle, boolean killAttacking, boolean killDefending) {
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();
        attacking.assertEffect(killAttacking, PokemonEffectNamesies.PERISH_SONG);
        defending.assertEffect(killDefending, PokemonEffectNamesies.PERISH_SONG);
        attacking.assertNoStatus();
        defending.assertNoStatus();
    }

    // Perisher is the Pokemon that will die from Perish Song (if shouldPerish)
    // Backup is the other Pokemon from the same team (which replaces perisher if shouldPerish)
    private void checkPerished(boolean shouldPerish, TestBattle battle, TestPokemon perisher, TestPokemon backup) {
        // Neither Pokemon should have Perish Song effect, however the way temporary effects are cleared is when they
        // re-enter the battle, so it is possible for this to fail if not the front Pokemon if swapped while having the effect
        TestPokemon front = (TestPokemon)battle.getTrainer(perisher).front();
        front.assertNoEffect(PokemonEffectNamesies.PERISH_SONG);
        Assert.assertEquals(perisher.isPlayer(), backup.isPlayer());

        // PERISHED
        if (shouldPerish) {
            perisher.assertHp(0);
            perisher.assertHasStatus(StatusNamesies.FAINTED);
        } else {
            Assert.assertTrue(perisher.getHP() > 0);
            perisher.assertNoStatus();
        }

        // Can only check the swap for opponent since player doesn't swap automatically in test
        if (!perisher.isPlayer()) {
            battle.assertFront(shouldPerish ? backup : perisher);
        }
    }
}
