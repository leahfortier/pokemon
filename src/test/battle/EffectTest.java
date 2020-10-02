package test.battle;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.Effect;
import battle.effect.Effect.CastMessageGetter;
import battle.effect.EffectInterfaces.BooleanHolder;
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
import test.battle.manipulator.PokemonAction.DefendingAction;
import test.battle.manipulator.PokemonManipulator;
import test.battle.manipulator.TestAction;
import test.battle.manipulator.TestInfo;
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
                     new TestAction().defending(AbilityNamesies.PRANKSTER),
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

    @Test
    public void bypassAccuracyTest() {
        // Attacker will fly in the air, making it semi-invulnerable to Tackle which should be a forced miss
        testSemiInvulnerable(false, AttackNamesies.FLY, AttackNamesies.TACKLE);
        testSemiInvulnerable(false, AttackNamesies.FLY, AttackNamesies.ROAR);

        // No Guard will allow Tackle to Hit, regardless of which Pokemon has it
        testSemiInvulnerable(true, true, AttackNamesies.FLY, AttackNamesies.TACKLE, true, new TestInfo().attacking(AbilityNamesies.NO_GUARD).attackingBypass(true));
        testSemiInvulnerable(true, true, AttackNamesies.FLY, AttackNamesies.TACKLE, true, new TestInfo().defending(AbilityNamesies.NO_GUARD).attackingBypass(true));

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
        testInfo = testInfo.copy(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);

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
        defending.setExpectedAccuracyBypass(firstExpected);
        battle.fight();

        // Attacker will be finished with its move and the defending move should hit this time
        defending.setExpectedAccuracyBypass(secondExpected);
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

        // Unless it is sound-based
        substituteTest(
                new TestInfo().defendingFight(AttackNamesies.GROWL),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(-1, Stat.ATTACK))
        );

        // Or the user has Infiltrator
        substituteTest(
                new TestInfo().setup(new TestAction().defending(AbilityNamesies.INFILTRATOR))
                              .defendingFight(AttackNamesies.TAIL_WHIP),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(-1, Stat.DEFENSE))
        );

        // Play Nice bypasses Substitute
        substituteTest(
                new TestInfo().defendingFight(AttackNamesies.PLAY_NICE),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(-1, Stat.ATTACK))
        );

        // Cannot give Yawn effect with Substitute
        substituteTest(
                new TestInfo().defendingFight(AttackNamesies.YAWN),
                (battle, attacking, defending) -> attacking.assertHasEffect(PokemonEffectNamesies.YAWN),
                (battle, attacking, defending) -> attacking.assertNoEffect(PokemonEffectNamesies.YAWN)
        );

        // If Yawn is applied BEFORE substitute though, it can still fall asleep
        substituteTest(
                new TestInfo().setup(new TestAction().defendingFight(AttackNamesies.YAWN)),
                (battle, attacking, defending) -> {
                    // Since this one didn't get an extra turn to use Substitute, it needs a free turn
                    // so that Yawn can take effect
                    attacking.assertHasEffect(PokemonEffectNamesies.YAWN);
                    battle.splashFight();
                    attacking.assertHasStatus(StatusNamesies.ASLEEP);
                },
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.ASLEEP)
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

        // Items cannot be swapped with Trick
        // Note: needs to be a trainer battle or it won't remove the item
        substituteTest(
                new TestInfo().asTrainerBattle()
                              .attacking(ItemNamesies.POTION)
                              .defendingFight(AttackNamesies.TRICK),
                (battle, attacking, defending) -> {
                    attacking.assertNotHoldingItem();
                    defending.assertHoldingItem(ItemNamesies.POTION);
                },
                (battle, attacking, defending) -> {
                    defending.assertLastMoveSucceeded(false);
                    attacking.assertHoldingItem(ItemNamesies.POTION);
                    defending.assertNotHoldingItem();
                }
        );

        // Or gifted with Bestow
        substituteTest(
                new TestInfo().asTrainerBattle()
                              .defending(ItemNamesies.POTION)
                              .defendingFight(AttackNamesies.BESTOW),
                (battle, attacking, defending) -> {
                    attacking.assertHoldingItem(ItemNamesies.POTION);
                    defending.assertNotHoldingItem();
                },
                (battle, attacking, defending) -> {
                    defending.assertLastMoveSucceeded(false);
                    attacking.assertNotHoldingItem();
                    defending.assertHoldingItem(ItemNamesies.POTION);
                }
        );

        // Or knocked off (WITH KNOCK OFF) -- will still receive the 50% boost though
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .asTrainerBattle()
                        .attacking(ItemNamesies.POTION)
                        .with((battle, attacking, defending) -> defending.setExpectedDamageModifier(1.5))
                        .defendingFight(AttackNamesies.KNOCK_OFF),
                (battle, attacking, defending) -> attacking.assertNotHoldingItem(),
                (battle, attacking, defending) -> attacking.assertHoldingItem(ItemNamesies.POTION)
        );

        // Or eaten with Pluck (make sure move still applies damage though even if absorbed)
        substituteTest(
                true,
                new TestInfo(PokemonNamesies.BLIPBUG, PokemonNamesies.RAYQUAZA)
                        .asTrainerBattle()
                        .addAttacking(PokemonNamesies.SQUIRTLE)
                        .attacking(ItemNamesies.RAWST_BERRY)
                        .fight(AttackNamesies.WILL_O_WISP, AttackNamesies.PLUCK),
                (battle, attacking, defending) -> {
                    attacking.assertDead();
                    attacking.assertConsumedItem(); // No eaten berry
                    defending.assertFullHealth();
                    defending.assertNoStatus();
                    defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                    defending.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
                },
                (battle, attacking, defending) -> {
                    attacking.assertFullHealth();
                    attacking.assertNotConsumedItem();
                    attacking.assertHoldingItem(ItemNamesies.RAWST_BERRY);
                    defending.assertHealthRatio(15/16.0);
                    defending.assertHasStatus(StatusNamesies.BURNED);
                    defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                    defending.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
                }
        );

        // Magician steals the opponent's item when it lands an item (fails with Substitute's Sticky Hold)
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .asTrainerBattle()
                        .attacking(ItemNamesies.POTION)
                        .defending(AbilityNamesies.MAGICIAN)
                        .defendingFight(AttackNamesies.SWIFT),
                (battle, attacking, defending) -> {
                    attacking.assertNotHoldingItem();
                    defending.assertHoldingItem(ItemNamesies.POTION);
                },
                (battle, attacking, defending) -> {
                    attacking.assertHoldingItem(ItemNamesies.POTION);
                    defending.assertNotHoldingItem();
                }
        );

        // Pickpocket can steal from behind the substitute
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .asTrainerBattle()
                        .defending(ItemNamesies.POTION)
                        .attacking(AbilityNamesies.PICKPOCKET)
                        .defendingFight(AttackNamesies.TACKLE),
                (battle, attacking, defending) -> {
                    attacking.assertHoldingItem(ItemNamesies.POTION);
                    defending.assertNotHoldingItem();
                },
                (battle, attacking, defending) -> {
                    attacking.assertNotHoldingItem();
                    defending.assertHoldingItem(ItemNamesies.POTION);
                }
        );

        // Status conditions cannot happen even from indirect sources like Fling
        // Actually this fails more because the secondary effect of Fling should be blocked, not
        // because statuses should be blocked
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .defending(ItemNamesies.FLAME_ORB)
                        .defendingFight(AttackNamesies.FLING)
                        .with((battle, attacking, defending) -> defending.assertNotHoldingItem()),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.BURNED),
                (battle, attacking, defending) -> attacking.assertNoStatus()
        );

        // Flinging a Lansat Berry will not raise the critical hit ratio of a substitute
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .defending(ItemNamesies.LANSAT_BERRY)
                        .defendingFight(AttackNamesies.FLING)
                        .with((battle, attacking, defending) -> defending.assertNotHoldingItem()),
                (battle, attacking, defending) -> attacking.assertHasEffect(PokemonEffectNamesies.RAISE_CRITS),
                (battle, attacking, defending) -> attacking.assertNoEffect(PokemonEffectNamesies.RAISE_CRITS)
        );

        // Disable before Substitute, Flinging a Mental Herb won't cure with Substitute
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .attackingFight(AttackNamesies.FALSE_SWIPE)
                        .defendingFight(AttackNamesies.DISABLE)
                        .defending(ItemNamesies.MENTAL_HERB)
                        .defendingFight(AttackNamesies.FLING)
                        .with((battle, attacking, defending) -> defending.assertNotHoldingItem()),
                (battle, attacking, defending) -> attacking.assertNoEffect(PokemonEffectNamesies.DISABLE),
                (battle, attacking, defending) -> attacking.assertHasEffect(PokemonEffectNamesies.DISABLE)
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
                        .attacking(StatusNamesies.BURNED),
                (battle, attacking, defending) -> attacking.assertHp(1),
                (battle, attacking, defending) -> attacking.assertFullHealth()
        );

        // Should not lose attack to Intimidate
        substituteTest(
                new TestInfo().asTrainerBattle()
                              .addDefending(PokemonNamesies.SQUIRTLE, AbilityNamesies.INTIMIDATE)
                              .attackingFight(AttackNamesies.ROAR),
                new TestAction().attacking(new TestStages().set(-1, Stat.ATTACK)),
                new TestAction().attacking(new TestStages())
        );

        // TODO: This is broken
        // Swagger fails against a substitute
        substituteTest(
                new TestInfo().defendingFight(AttackNamesies.SWAGGER),
                (battle, attacking, defending) -> {
                    defending.assertLastMoveSucceeded(true);
                    attacking.assertStages(new TestStages().set(2, Stat.ATTACK));
                    attacking.assertHasEffect(PokemonEffectNamesies.CONFUSION);
                },
                (battle, attacking, defending) -> {
//                    attacking.assertStages(new TestStages());
                    attacking.assertNoEffect(PokemonEffectNamesies.CONFUSION);
//                    defending.assertLastMoveSucceeded(false);
                }
        );

        // Should not get poisoned from Toxic Spikes when Baton Passed
        substituteTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .addAttacking(PokemonNamesies.SQUIRTLE)
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
                        .addAttacking(PokemonNamesies.GRIMER)
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
                    attacking.assertHp(1);
                    defending.assertNotFullHealth();
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

        // Physical contact effects should not trigger from hitting a substitute
        // Mummy gives the Mummy ability on contact
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .attacking(AbilityNamesies.MUMMY)
                        .defending(AbilityNamesies.OVERGROW)
                        .defendingFight(AttackNamesies.TACKLE),
                (battle, attacking, defending) -> defending.assertChangedAbility(AbilityNamesies.MUMMY),
                (battle, attacking, defending) -> defending.assertAbility(AbilityNamesies.OVERGROW)
        );

        // Gooey lowers the contacter's Speed by 1 (no contact with Substitute though)
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .attacking(AbilityNamesies.GOOEY)
                        .defendingFight(AttackNamesies.TACKLE),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(-1, Stat.SPEED)),
                (battle, attacking, defending) -> defending.assertStages(new TestStages())
        );

        // Red Card causes the attacker to switch out when the holder is hit by an attack (prevented by Substitute)
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.HAPPINY)
                        .asTrainerBattle()
                        .addDefending(PokemonNamesies.SQUIRTLE)
                        .attacking(ItemNamesies.RED_CARD)
                        .defendingFight(AttackNamesies.SWIFT),
                (battle, attacking, defending) -> {
                    defending.assertSpecies(PokemonNamesies.SQUIRTLE);
                    attacking.assertConsumedItem();
                },
                (battle, attacking, defending) -> {
                    defending.assertSpecies(PokemonNamesies.HAPPINY);
                    attacking.assertHoldingItem(ItemNamesies.RED_CARD);
                }
        );

        // Life Orb should still damage the holder when the attack is absorbed
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .defending(ItemNamesies.LIFE_ORB)
                        .with((battle, attacking, defending) -> defending.setExpectedDamageModifier(5324.0/4096.0))
                        .defendingFight(AttackNamesies.SWIFT),
                (battle, attacking, defending) -> defending.assertHealthRatio(.9)
        );

        // Rowap Berry causes the attacker to take 1/8 max HP when landing an attack on the holder that isn't behind a Substitute
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .attacking(ItemNamesies.ROWAP_BERRY)
                        .defendingFight(AttackNamesies.SWIFT),
                (battle, attacking, defending) -> {
                    defending.assertHealthRatio(7/8.0);
                    attacking.assertConsumedBerry();
                },
                (battle, attacking, defending) -> {
                    defending.assertFullHealth();
                    attacking.assertHoldingItem(ItemNamesies.ROWAP_BERRY);
                }
        );

        // Gulp Missile changes forms when using Surf (use Protect for full health check convenience)
        // When in gulping form, the Pokemon will deal 1/4 Max HP and lower defense by 1 when hit by an attack
        // Should be able to enter forms with substitute, but cannot leave until substitute is broken and takes damage
        substituteTest(
                true,
                new TestInfo(PokemonNamesies.CRAMORANT, PokemonNamesies.XURKITREE)
                        .attacking(AbilityNamesies.GULP_MISSILE)
                        .fight(AttackNamesies.SURF, AttackNamesies.PROTECT)
                        .fight(AttackNamesies.ENDURE, AttackNamesies.THUNDER),
                (battle, attacking, defending) -> {
                    attacking.assertHp(1);
                    defending.assertHealthRatio(.75);
                    defending.assertStages(new TestStages().set(-1, Stat.DEFENSE));
                },
                (battle, attacking, defending) -> {
                    attacking.assertFullHealth();
                    defending.assertFullHealth();
                    defending.assertStages(new TestStages());
                    attacking.assertNoEffect(PokemonEffectNamesies.SUBSTITUTE);

                    // To make sure Endure doesn't fail again
                    battle.splashFight();

                    // Once substitute is broken though, the form effect should trigger
                    battle.fight(AttackNamesies.ENDURE, AttackNamesies.TACKLE);
                    attacking.assertNotFullHealth();
                    defending.assertHealthRatio(.75);
                    defending.assertStages(new TestStages().set(-1, Stat.DEFENSE));
                }
        );

        // Absorb Bulb boosts Sp. Attack when hit by a Water-move -- does not trigger with Substitute
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .attacking(ItemNamesies.ABSORB_BULB)
                        .defendingFight(AttackNamesies.WATER_GUN),
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages().set(1, Stat.SP_ATTACK));
                    attacking.assertConsumedItem();
                },
                (battle, attacking, defending) -> {
                    attacking.assertStages(new TestStages());
                    attacking.assertHoldingItem(ItemNamesies.ABSORB_BULB);
                }
        );

        // Color Change will fail against a substitute
        substituteTest(
                new TestInfo(PokemonNamesies.KECLEON, PokemonNamesies.EEVEE)
                        .attacking(AbilityNamesies.COLOR_CHANGE)
                        .defendingFight(AttackNamesies.BUBBLE),
                (battle, attacking, defending) -> {
                    attacking.assertType(battle, Type.WATER);
                    attacking.assertNotType(battle, Type.NORMAL);
                },
                (battle, attacking, defending) -> {
                    attacking.assertType(battle, Type.NORMAL);
                    attacking.assertNotType(battle, Type.WATER);
                }
        );

        // Air Balloon will still pop when the holder is behind a substitute (only difference is general absorbed damage)
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .with((battle, attacking, defending) -> {
                            Assert.assertFalse(attacking.isLevitating(battle));
                            attacking.withItem(ItemNamesies.AIR_BALLOON);
                            Assert.assertTrue(attacking.isLevitating(battle));

                            battle.defendingFight(AttackNamesies.EARTHQUAKE);
                            defending.assertLastMoveSucceeded(false);
                            Assert.assertTrue(attacking.isLevitating(battle));
                            attacking.assertHoldingItem(ItemNamesies.AIR_BALLOON);

                            // Pop goes the Air Balloon!
                            battle.defendingFight(AttackNamesies.SWIFT);
                            Assert.assertFalse(attacking.isLevitating(battle));
                            attacking.assertConsumedItem();
                        }),
                (battle, attacking, defending) -> attacking.assertNotFullHealth(),
                (battle, attacking, defending) -> attacking.assertFullHealth()
        );

        // Justified doesn't trigger from Substitute (should raise Attack when hit with a Dark-type move)
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .attacking(AbilityNamesies.JUSTIFIED)
                        .defendingFight(AttackNamesies.BITE),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(1, Stat.ATTACK)),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages())
        );

        // Bide release should be blocked by Substitute
        substituteTest(
                new TestInfo(PokemonNamesies.BLISSEY, PokemonNamesies.SHUCKLE)
                        .with((battle, attacking, defending) -> {
                            attacking.assertFullHealth();
                            defending.assertFullHealth();

                            // Defending Bide will go first since it's an increased priority move
                            battle.fight(AttackNamesies.FALSE_SWIPE, AttackNamesies.BIDE);
                            attacking.assertFullHealth();
                            defending.assertNotFullHealth();
                            defending.assertHasEffect(PokemonEffectNamesies.BIDE);

                            // Keep False Swiping to build up Bide damage
                            battle.fight();
                            attacking.assertFullHealth();
                            defending.assertNotFullHealth();
                            defending.assertHasEffect(PokemonEffectNamesies.BIDE);

                            // Bide should be released this turn -- but should be blocked by substitute
                            battle.fight();
                            defending.assertNoEffect(PokemonEffectNamesies.BIDE);
                        }),
                (battle, attacking, defending) -> attacking.assertNotFullHealth(),
                (battle, attacking, defending) -> attacking.assertFullHealth()
        );

        // Future Sight should be blocked by Substitute
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .with((battle, attacking, defending) -> {
                            attacking.assertFullHealth();
                            defending.assertFullHealth();

                            // Cast Future Sight effect
                            defending.setExpectedAccuracyBypass(true);
                            battle.defendingFight(AttackNamesies.FUTURE_SIGHT);
                            attacking.assertFullHealth();
                            defending.assertFullHealth();
                            battle.assertHasEffect(attacking, TeamEffectNamesies.FUTURE_SIGHT);

                            // Do nothing and let the future reveal itself
                            defending.setExpectedAccuracyBypass(null);
                            battle.splashFight();
                            attacking.assertFullHealth();
                            defending.assertFullHealth();
                            battle.assertHasEffect(attacking, TeamEffectNamesies.FUTURE_SIGHT);

                            // The future is now and it says fuck you substitute why can't I hurt you???
                            battle.splashFight();
                            battle.assertNoEffect(attacking, TeamEffectNamesies.FUTURE_SIGHT);
                        }),
                (battle, attacking, defending) -> attacking.assertNotFullHealth(),
                (battle, attacking, defending) -> attacking.assertFullHealth()
        );

        // Fling effects shouldn't trigger from Substitute, but the item should still be consumed
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .setup(new TestAction().attacking(AbilityNamesies.MAGIC_GUARD)
                                               .defendingFight(AttackNamesies.WILL_O_WISP))
                        .defending(ItemNamesies.RAWST_BERRY)
                        .attacking(StatusNamesies.BURNED)
                        .defendingFight(AttackNamesies.FLING)
                        .with((battle, attacking, defending) -> defending.assertConsumedItem()),
                (battle, attacking, defending) -> attacking.assertNoStatus(),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.BURNED)
        );

        // Flame Orb can still give status conditions to a substitute
        substituteTest(
                new TestInfo()
                        .setup(new TestAction().attacking(ItemNamesies.FLAME_ORB)
                                               .defendingFight(AttackNamesies.MAGIC_ROOM)
                                               .attacking(StatusNamesies.NO_STATUS))
                        .defendingFight(AttackNamesies.MAGIC_ROOM),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.BURNED)
        );

        // Rest should still work with substitute
        substituteTest(
                new TestInfo().fight(AttackNamesies.TAKE_DOWN, AttackNamesies.ENDURE)
                              .attackingFight(AttackNamesies.REST),
                (battle, attacking, defending) -> {
                    attacking.assertLastMoveSucceeded(true);
                    attacking.assertFullHealth();
                    attacking.assertHasStatus(StatusNamesies.ASLEEP);
                }
        );

        // Wake Up Slap should not wake up a Substitute (but should still get the boost)
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .with((battle, attacking, defending) -> {
                            battle.fight(AttackNamesies.TAKE_DOWN, AttackNamesies.ENDURE);
                            battle.attackingFight(AttackNamesies.REST);
                            attacking.assertHasStatus(StatusNamesies.ASLEEP);

                            defending.setExpectedDamageModifier(2.0);
                            battle.defendingFight(AttackNamesies.WAKE_UP_SLAP);
                        }),
                (battle, attacking, defending) -> attacking.assertNoStatus(),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.ASLEEP)
        );

        // Incinerate cannot burn a substitute's item
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .attacking(ItemNamesies.CHESTO_BERRY)
                        .defendingFight(AttackNamesies.INCINERATE),
                (battle, attacking, defending) -> attacking.assertNotHoldingItem(),
                (battle, attacking, defending) -> attacking.assertHoldingItem(ItemNamesies.CHESTO_BERRY)
        );

        // Substitute doesn't prevent self-inflicted stat reductions
        substituteTest(
                new TestInfo().fight(AttackNamesies.LEAF_STORM, AttackNamesies.ENDURE),
                new TestAction().attacking(new TestStages().set(-2, Stat.SP_ATTACK))
        );

        // Spectral Thief's effect pierces Substitute to steal stat boosts (damage is still absorbed)
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .attackingFight(AttackNamesies.CURSE)
                        .attacking(new TestStages().set(1, Stat.ATTACK, Stat.DEFENSE).set(-1, Stat.SPEED))
                        .defending(new TestStages())
                        .defendingFight(AttackNamesies.SPECTRAL_THIEF)
                        .attacking(new TestStages().set(-1, Stat.SPEED))
                        .defending(new TestStages().set(1, Stat.ATTACK, Stat.DEFENSE)),
                (battle, attacking, defending) -> attacking.assertNotFullHealth(),
                (battle, attacking, defending) -> attacking.assertFullHealth()
        );

        // Clear Smog should clear all stat changes but is blocked by substitute
        substituteTest(
                new TestInfo().attackingFight(AttackNamesies.CURSE)
                              .defendingFight(AttackNamesies.CLEAR_SMOG),
                new TestAction().attacking(new TestStages()),
                new TestAction().attacking(new TestStages().set(1, Stat.ATTACK, Stat.DEFENSE).set(-1, Stat.SPEED))
        );

        // Circle Throw/Dragon Tail will not swap a Substitute out
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.CATERPIE)
                        .addAttacking(PokemonNamesies.SQUIRTLE)
                        .defendingFight(AttackNamesies.CIRCLE_THROW),
                (battle, attacking, defending) -> attacking.assertSpecies(PokemonNamesies.SQUIRTLE),
                (battle, attacking, defending) -> attacking.assertSpecies(PokemonNamesies.SHUCKLE)
        );

        // Counter/Mirror Coat should fail because damage is absorbed
        substituteTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .fight(AttackNamesies.COUNTER, AttackNamesies.FALSE_SWIPE),
                (battle, attacking, defending) -> {
                    attacking.assertLastMoveSucceeded(true);
                    attacking.assertNotFullHealth();
                    defending.assertMissingHp(2*(attacking.getMaxHP() - attacking.getHP()));
                },
                (battle, attacking, defending) -> {
                    attacking.assertLastMoveSucceeded(false);
                    attacking.assertFullHealth();
                    defending.assertFullHealth();
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

                    // Make sure Substitute reduces to 75% health, but then heal back to 100% for ease
                    attacking.assertHealthRatio(.75);
                    attacking.healHealthFraction(1);
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

        TestBattle battle = TestBattle.createTrainerBattle(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();
        TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.EEVEE);
        TestPokemon defending2 = battle.addDefending(PokemonNamesies.SQUIRTLE);

        // Protect does not protect against Perish Song
        battle.fight(AttackNamesies.PERISH_SONG, AttackNamesies.PROTECT);
        checkPerishing(battle, true, true);

        battle.clearAllEffects();
        checkPerishing(battle, false, false);

        // Give Perish Song effect only to attacking Pokemon
        defending.withAbility(AbilityNamesies.SOUNDPROOF);
        battle.attackingFight(AttackNamesies.PERISH_SONG);
        checkPerishing(battle, true, false);

        // Perish Body should only add perishing to both when it perishes the contacter
        defending.withAbility(AbilityNamesies.PERISH_BODY);
        battle.attackingFight(AttackNamesies.FALSE_SWIPE);
        checkPerishing(battle, true, false);

        battle.clearAllEffects();
        checkPerishing(battle, false, false);

        // This time only give defending Perish Song
        attacking.withAbility(AbilityNamesies.SOUNDPROOF);
        battle.attackingFight(AttackNamesies.PERISH_SONG);
        checkPerishing(battle, false, true);

        // Perish Body will add perishing to attacking even though defending is already perishing
        battle.attackingFight(AttackNamesies.FALSE_SWIPE);
        checkPerishing(battle, true, true);

        // Make sure defending perishes before attacking
        battle.splashFight();
        checkPerished(true, battle, defending, defending2);
        checkPerishing(battle, true, false);

        battle.splashFight();
        checkPerished(true, battle, defending, defending2);
        checkPerished(true, battle, attacking, attacking2);
        checkPerishing(battle, false, false);
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
        // Neither Pokemon should have Perish Song effect, however temporary effects are currently cleared when
        // re-entering the battle, so it is possible for this to fail if not the front Pokemon if swapped while having the effect
        TestPokemon front = (TestPokemon)battle.getTrainer(perisher).front();
        front.assertNoEffect(PokemonEffectNamesies.PERISH_SONG);
        Assert.assertEquals(perisher.isPlayer(), backup.isPlayer());

        // PERISHED
        if (shouldPerish) {
            perisher.assertDead();
        } else {
            Assert.assertTrue(perisher.getHP() > 0);
            perisher.assertNoStatus();
        }

        // Can only check the swap for opponent since player doesn't swap automatically in test
        if (!perisher.isPlayer()) {
            battle.assertFront(shouldPerish ? backup : perisher);
        }
    }

    @Test
    public void weatherBuffetTest() {
        // Rock, Ground, and Steel-type Pokemon do not take buffet damage in a Sandstorm
        weatherBuffetTest(
                false, true,
                PokemonNamesies.ROGGENROLA, PokemonNamesies.SANDSHREW, PokemonNamesies.KLINK,
                PokemonNamesies.GEODUDE, PokemonNamesies.STEELIX, PokemonNamesies.SHIELDON,
                PokemonNamesies.ARCHEOPS, PokemonNamesies.WOOPER, PokemonNamesies.MAWILE
        );

        // Ice-type Pokemon do not take buffet damage in Hail
        weatherBuffetTest(true, false, PokemonNamesies.SNORUNT, PokemonNamesies.SNOVER);

        // Rock/Ice, Ice/Ground, and Ice/Steel Pokemon -- take damage from neither
        weatherBuffetTest(false, false, PokemonNamesies.AMAURA, PokemonNamesies.SWINUB, PokemonNamesies.SNOWSHREW);

        // Everything else takes both
        weatherBuffetTest(
                true, true,
                PokemonNamesies.EEVEE, PokemonNamesies.CHARMANDER, PokemonNamesies.SQUIRTLE,
                PokemonNamesies.BULBASAUR, PokemonNamesies.DRAGONITE, PokemonNamesies.MIMIKYU
        );

        // Safety Goggles and Utility Umbrella protects from both buffets
        weatherBuffetTest(false, false, new TestInfo().attacking(ItemNamesies.SAFETY_GOGGLES));
        weatherBuffetTest(false, false, new TestInfo().attacking(ItemNamesies.UTILITY_UMBRELLA));

        // Several abilities will block buffet damage
        weatherBuffetTest(false, false, AbilityNamesies.MAGIC_GUARD, AbilityNamesies.OVERCOAT);
        weatherBuffetTest(false, true, AbilityNamesies.SAND_FORCE, AbilityNamesies.SAND_RUSH, AbilityNamesies.SAND_VEIL);
        weatherBuffetTest(true, false, AbilityNamesies.ICE_BODY, AbilityNamesies.SNOW_CLOAK, AbilityNamesies.SLUSH_RUSH);

        // Air Lock/Cloud Nine prevent all weather effects for both Pokemon including buffet damage
        weatherBuffetTest(false, false, false, new TestInfo().attacking(AbilityNamesies.AIR_LOCK));
        weatherBuffetTest(false, false, false, new TestInfo().attacking(AbilityNamesies.CLOUD_NINE));
    }

    private void weatherBuffetTest(boolean buffetSandstorm, boolean buffetHail, PokemonNamesies... buffeted) {
        for (PokemonNamesies pokes : buffeted) {
            weatherBuffetTest(buffetSandstorm, buffetHail, new TestInfo(pokes, PokemonNamesies.EEVEE));
        }
    }

    private void weatherBuffetTest(boolean buffetSandstorm, boolean buffetHail, AbilityNamesies... abilities) {
        for (AbilityNamesies ability : abilities) {
            weatherBuffetTest(buffetSandstorm, buffetHail, new TestInfo().attacking(ability));
        }
    }

    private void weatherBuffetTest(boolean buffetSandstorm, boolean buffetHail, TestInfo testInfo) {
        // By default, defending Pokemon should take buffet damage (mostly testing attacking buffet damage here)
        weatherBuffetTest(buffetSandstorm, buffetHail, true, testInfo);
    }

    private void weatherBuffetTest(boolean buffetSandstorm, boolean buffetHail, boolean defendingBuffet, TestInfo testInfo) {
        weatherBuffetTest(buffetSandstorm, defendingBuffet, AttackNamesies.SANDSTORM, testInfo);
        weatherBuffetTest(buffetHail, defendingBuffet, AttackNamesies.HAIL, testInfo);

        // These weather conditions never buffet
        weatherBuffetTest(false, false, AttackNamesies.SUNNY_DAY, testInfo);
        weatherBuffetTest(false, false, AttackNamesies.RAIN_DANCE, testInfo);
    }

    private void weatherBuffetTest(boolean attackingBuffet, boolean defendingBuffet, AttackNamesies weatherAttack, TestInfo testInfo) {
        TestBattle battle = testInfo.copy(PokemonNamesies.EEVEE, PokemonNamesies.EEVEE).createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        testInfo.manipulate(battle);
        battle.assertWeather(WeatherNamesies.CLEAR_SKIES);

        battle.attackingFight(weatherAttack);
        attacking.assertHealthRatio(attackingBuffet ? 15/16.0 : 1);
        defending.assertHealthRatio(defendingBuffet ? 15/16.0 : 1);

        WeatherNamesies weather = (WeatherNamesies)weatherAttack.getNewAttack().getEffect();
        battle.assertWeather(weather);
    }

    @Test
    public void onDamageDeathEffectTest() {
        // Cotton Down shouldn't change stats of a dead Pokemon, but can still change when it dies
        // (Including stuff with previous changes and Defiant just to double check that the stats aren't
        // empty in the dead case in case they are being reset on death before this check)
        onDamageDeathEffectTest(
                new TestInfo().attackingFight(AttackNamesies.SWORDS_DANCE)
                              .attacking(AbilityNamesies.DEFIANT)
                              .defending(AbilityNamesies.COTTON_DOWN),
                new TestAction().attacking(new TestStages().set(2, Stat.ATTACK)),
                new TestAction().attacking(new TestStages().set(4, Stat.ATTACK).set(-1, Stat.SPEED))
        );

        // Make sure Shell Bell Heals when it kills
        // Uses Kartana and Blissey here because Shell Bell would heal more than Struggle would recoil
        // User at 1 HP, Target at full -- user dies before Shell Bell can heal
        // User at full, Target at 1 -- struggle take to .75, shell bell heals 1
        onDamageDeathEffectTest(
                new TestInfo(PokemonNamesies.KARTANA, PokemonNamesies.BLISSEY)
                        .attacking(ItemNamesies.SHELL_BELL),
                (battle, attacking, defending) -> attacking.assertDead(),
                (battle, attacking, defending) -> attacking.assertHealthRatioDiff(attacking.getMaxHP() + 1, .25)
        );

        // Can still sap from a dead Pokemon with Liquid Ooze
        onDamageDeathEffectTest(
                new TestInfo().attacking(AttackNamesies.GIGA_DRAIN)
                              .defending(AbilityNamesies.LIQUID_OOZE),
                (battle, attacking, defending) -> attacking.assertDead(),
                (battle, attacking, defending) -> attacking.assertMissingHp(1)
        );

        // Thief activates before Rough Skin and will steal the item before it dies
        onDamageDeathEffectTest(
                new TestInfo().attacking(AttackNamesies.THIEF)
                              .defending(AbilityNamesies.ROUGH_SKIN)
                              .defending(ItemNamesies.WATER_STONE),
                (battle, attacking, defending) -> {
                    attacking.assertHoldingItem(ItemNamesies.WATER_STONE);
                    defending.assertNotHoldingItem();
                }
        );

        // Knock Off shouldn't trigger when the opponent dies
        onDamageDeathEffectTest(
                new TestInfo().attacking(AttackNamesies.KNOCK_OFF)
                              .attacking(ItemNamesies.LIFE_ORB)
                              .defending(ItemNamesies.WATER_STONE),
                (battle, attacking, defending) -> defending.assertActualHeldItem(ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM),
                (battle, attacking, defending) -> defending.assertActualHeldItem(ItemNamesies.WATER_STONE)
        );

        // Can't steal when you're already dead no matter how magic you are
        // (activates after recoil, but should be before rough skin etc)
        onDamageDeathEffectTest(
                new TestInfo().attacking(AbilityNamesies.MAGICIAN)
                              .defending(ItemNamesies.WATER_STONE),
                (battle, attacking, defending) -> {
                    attacking.assertNotHoldingItem();
                    defending.assertHoldingItem(ItemNamesies.WATER_STONE);
                    defending.assertNoEffect(PokemonEffectNamesies.CHANGE_ITEM);
                },
                (battle, attacking, defending) -> {
                    attacking.assertHoldingItem(ItemNamesies.WATER_STONE);
                    defending.assertNotHoldingItem();
                    defending.assertHasEffect(PokemonEffectNamesies.CHANGE_ITEM);
                }
        );

        // Pluck eats the berry of a dead boy, and can eat it before effects like Life Orb or Rough Skin etc
        onDamageDeathEffectTest(
                new TestInfo().attacking(AttackNamesies.PLUCK)
                              .attacking(ItemNamesies.LIFE_ORB)
                              .defending(ItemNamesies.LANSAT_BERRY),
                (battle, attacking, defending) -> {
                    attacking.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
                    attacking.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                    defending.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
                    defending.assertHasEffect(PokemonEffectNamesies.CONSUMED_ITEM);
                }
        );

        // Don't lower a dead Pokemon with Gooey, but Gooey can lower when it's dead
        onDamageDeathEffectTest(
                new TestInfo().defending(AbilityNamesies.GOOEY).attackingFight(AttackNamesies.AGILITY),
                new TestAction().attacking(new TestStages().set(2, Stat.SPEED)),
                new TestAction().attacking(new TestStages().set(1, Stat.SPEED))
        );

        // Struggle removes 25%, Aftermath removes 25%
        onDamageDeathEffectTest(
                new TestInfo().defending(AbilityNamesies.AFTERMATH),
                (battle, attacking, defending) -> attacking.assertDead(),
                (battle, attacking, defending) -> attacking.assertHealthRatio(.5, 1)
        );

        // Sticky Barb should not transfer to a dead Pokemon, but can transfer when the holder is dead
        onDamageDeathEffectTest(
                new TestInfo().defending(ItemNamesies.STICKY_BARB),
                (battle, attacking, defending) -> attacking.assertNotHoldingItem(),
                (battle, attacking, defending) -> attacking.assertHoldingItem(ItemNamesies.STICKY_BARB)
        );

        // Red Card should switch the user and be consumed even when the holder dies
        // Red Card does not activate when the holder dies because you can't consume an item when you're dead
        // Red Card also does not activate when the user dies because they're already switching
        onDamageEffectCompareDeathTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .defending(ItemNamesies.RED_CARD),
                (battle, attacking, defending) -> {
                    battle.assertFront(attacking);
                    attacking.assertSpecies(PokemonNamesies.BULBASAUR);
                    defending.assertHoldingItem(ItemNamesies.RED_CARD);
                },
                (battle, attacking, defending) -> {
                    // Alive Charmander consumed Red Card and alive Bulbasaur was swapped for Squirtle
                    battle.assertNotFront(attacking);
                    battle.getAttacking().assertSpecies(PokemonNamesies.SQUIRTLE);

                    battle.assertFront(defending);
                    defending.assertSpecies(PokemonNamesies.CHARMANDER);
                    defending.assertConsumedItem();

                    attacking.assertNoStatus();
                    attacking.assertNotFullHealth();
                    defending.assertNoStatus();
                    defending.assertNotFullHealth();
                }
        );

        // Jaboca Berry cannot be consumed when the holder dies but dead pokes can't eat berries
        // Also can't be consumed by attacking when they're dead because they're dead no more health to lose
        onDamageEffectCompareDeathTest(
                new TestInfo().defending(ItemNamesies.JABOCA_BERRY),
                (battle, attacking, defending) -> {
                    defending.assertHoldingItem(ItemNamesies.JABOCA_BERRY);
                    defending.assertNotConsumedItem();
                },
                (battle, attacking, defending) -> {
                    defending.assertConsumedBerry();
                    attacking.assertHealthRatio(.75 - 1/8.0, 1);
                }
        );

        // Cramorant should release its form when it OR the user dies
        // Gulping form deals 25% health to the attacker when hit
        // (GulpMissile returns true when in gulping/gorging form)
        onDamageDeathEffectTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.CRAMORANT)
                        .defending(AbilityNamesies.GULP_MISSILE)
                        .fight(AttackNamesies.ENDURE, AttackNamesies.SURF)
                        .fight(AttackNamesies.RECOVER, AttackNamesies.HEAL_PULSE)
                        .with((battle, attacking, defending) -> {
                            // Surf will put Cramorant in Gulping Form
                            Assert.assertTrue(((BooleanHolder)defending.getAbility()).getBoolean());

                            // Heal the attacker from surf damage for simple health ratio
                            attacking.assertFullHealth();
                        })
                        .after(((battle, attacking, defending) -> Assert.assertFalse(((BooleanHolder)defending.getAbility()).getBoolean()))),
                (battle, attacking, defending) -> attacking.assertDead(),
                (battle, attacking, defending) -> attacking.assertHealthRatio(.5, 1)
        );

        // Berserk increases its Special Attack by 1 when its HP drops below half health from a direct attack
        // Should trigger even when the attacker is dead this isn't about them
        onDamageDeathEffectTest(
                new TestInfo().defending(AbilityNamesies.BERSERK)
                              .defendingFight(AttackNamesies.BELLY_DRUM),
                new TestAction().defending(new TestStages().set(1, Stat.SP_ATTACK).set(6, Stat.ATTACK)),
                new TestAction().defending(new TestStages().set(6, Stat.ATTACK))
        );

        // Rage increases attack when hit even if attacker dies
        // Give Sturdy instead of using Endure, since it has priority and will lose the raging effect
        onDamageDeathEffectTest(
                new TestInfo().fight(AttackNamesies.ENDURE, AttackNamesies.RAGE)
                              .defending(new TestStages())
                              .defending(AttackNamesies.SPLASH)
                              .defending(AbilityNamesies.STURDY)
                              .with(new DefendingAction().assertEffect(PokemonEffectNamesies.RAGING)),
                new TestAction().defending(new TestStages().set(1, Stat.ATTACK)),
                new TestAction().defending(new TestStages())
        );

        // Absorb Bulb should only trigger when the holder is alive but the user can do its own thing and die
        onDamageDeathEffectTest(
                new TestInfo().defending(ItemNamesies.ABSORB_BULB)
                              .attacking(ItemNamesies.LIFE_ORB)
                              .attacking(AttackNamesies.WATER_GUN)
                              .defendingFight(AttackNamesies.NASTY_PLOT)
                              .defending(new TestStages().set(2, Stat.SP_ATTACK)),
                (battle, attacking, defending) -> {
                    defending.assertStages(new TestStages().set(3, Stat.SP_ATTACK));
                    defending.assertConsumedItem();
                },
                (battle, attacking, defending) -> {
                    defending.assertStages(new TestStages().set(2, Stat.SP_ATTACK));
                    defending.assertHoldingItem(ItemNamesies.ABSORB_BULB);
                }
        );

        // Air Balloon always pops when hit (one of the few items that should be consumed when it is dead)
        onDamageDeathEffectTest(
                new TestInfo().defending(ItemNamesies.AIR_BALLOON)
                              .with((battle, attacking, defending) -> Assert.assertTrue(defending.isLevitating(battle))),
                (battle, attacking, defending) -> {
                    defending.assertConsumedItem();
                    Assert.assertFalse(defending.isLevitating(battle));
                }
        );

        // User's death is irrelevant for Eject Button and will switch Charmander to Pikachu and consume the item
        // If the victim dies, Eject Button will not be consumed but will be switched to Pikachu because
        // it is dead and that is what happens in every case
        onDamageDeathEffectTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .defending(ItemNamesies.EJECT_BUTTON)
                        .after((battle, attacking, defending) -> {
                            battle.assertNotFront(defending);
                            battle.getDefending().assertSpecies(PokemonNamesies.PIKACHU);
                        }),
                (battle, attacking, defending) -> defending.assertConsumedItem(),
                (battle, attacking, defending) -> defending.assertHoldingItem(ItemNamesies.EJECT_BUTTON)
        );

        // Illusion should trigger even when the target dies SO YOU KNOW IT WAS JUST AN ILLUSION
        // Charmander will come out under the ILLUSION of being a Pikachu
        onDamageDeathEffectTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .addDefending(PokemonNamesies.PIKACHU)
                        .defending(AbilityNamesies.ILLUSION)
                        .attackingFight(AttackNamesies.WHIRLWIND)
                        .attackingFight(AttackNamesies.WHIRLWIND)
                        .with((battle, attacking, defending) -> {
                            defending.assertSpecies(PokemonNamesies.CHARMANDER);
                            Assert.assertTrue(defending.getAbility().isActive());
                            Assert.assertEquals("Pikachu", defending.getName());
                            Assert.assertEquals("Charmander", defending.getActualName());
                        })
                        .after((battle, attacking, defending) -> {
                            defending.assertSpecies(PokemonNamesies.CHARMANDER);
                            Assert.assertFalse(defending.getAbility().isActive());
                            Assert.assertEquals("Charmander", defending.getName());
                        }),
                (battle, attacking, defending) -> battle.assertFront(defending),
                (battle, attacking, defending) -> battle.assertNotFront(defending)
        );

        // Clear Smog shouldn't remove stats when the target dies
        onDamageDeathEffectTest(
                new TestInfo().fight(AttackNamesies.SCREECH, AttackNamesies.SWORDS_DANCE)
                              .attackingFight(AttackNamesies.NASTY_PLOT)
                              .attacking(AttackNamesies.CLEAR_SMOG)
                              .attacking(ItemNamesies.LIFE_ORB)
                              .after(new TestAction().attacking(new TestStages().set(2, Stat.SP_ATTACK))),
                new TestAction().defending(new TestStages()),
                new TestAction().defending(new TestStages().set(2, Stat.ATTACK).set(-2, Stat.DEFENSE))
        );

        // Incinerate should destroy the target's item even if they die
        onDamageDeathEffectTest(
                new TestInfo().defending(ItemNamesies.NORMAL_GEM)
                              .attacking(AttackNamesies.INCINERATE)
                              .attacking(ItemNamesies.LIFE_ORB),
                (battle, attacking, defending) -> defending.assertNotHoldingItem()
        );
    }

    // When it's the same effect when either user dies (and is probably also the same when no one dies too)
    private void onDamageDeathEffectTest(TestInfo testInfo, PokemonManipulator samesies) {
        onDamageEffectCompareDeathTest(testInfo, samesies, samesies);
    }

    // Checks on death and without death
    private void onDamageEffectCompareDeathTest(TestInfo testInfo, PokemonManipulator eitherDead, PokemonManipulator bothAlive) {
        onDamageDeathEffectTest(testInfo, eitherDead, eitherDead);
        onDamageDeathEffectTest(false, false, testInfo, bothAlive);
    }

    private void onDamageDeathEffectTest(TestInfo testInfo, PokemonManipulator userDies, PokemonManipulator targetDies) {
        // Give more Pokemon so the battle doesn't end on death
        testInfo.asTrainerBattle()
                .addAttacking(PokemonNamesies.SQUIRTLE)
                .addDefending(PokemonNamesies.PIKACHU);

        onDamageDeathEffectTest(true, false, testInfo, userDies);
        onDamageDeathEffectTest(false, true, testInfo, targetDies);
    }

    private void onDamageDeathEffectTest(boolean killUser, boolean killVictim, TestInfo testInfo, PokemonManipulator afterCheck) {
        TestBattle battle = testInfo.copy().createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        testInfo.manipulate(battle);
        attacking.assertStatus(false, StatusNamesies.FAINTED);
        defending.assertStatus(false, StatusNamesies.FAINTED);

        if (killUser) {
            attacking.setHP(1);
        }
        if (killVictim) {
            defending.setHP(1);
        }

        AttackNamesies defaultDefending = killUser ? AttackNamesies.ENDURE : AttackNamesies.SPLASH;
        testInfo.defaultFight(battle, AttackNamesies.STRUGGLE, defaultDefending);

        attacking.assertStatus(killUser, StatusNamesies.FAINTED);
        defending.assertStatus(killVictim, StatusNamesies.FAINTED);

        afterCheck.manipulate(battle, attacking, defending);
        testInfo.performAfterCheck(battle, attacking, defending);
    }
}
