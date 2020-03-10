package test.battle;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.effect.EffectInterfaces.EntryHazard;
import battle.effect.attack.MultiTurnMove;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffectNamesies;
import item.Item;
import item.ItemNamesies;
import item.bag.BagCategory;
import item.berry.Berry;
import item.berry.CategoryBerry;
import item.use.BallItem;
import item.use.BattleUseItem;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import test.general.BaseTest;
import test.general.TestUtils;
import test.pokemon.TestPokemon;

import java.util.EnumSet;
import java.util.Set;

public class ItemTest extends BaseTest {
    @Test
    public void descriptionTest() {
        for (ItemNamesies itemNamesies : ItemNamesies.values()) {
            if (itemNamesies == ItemNamesies.NO_ITEM) {
                continue;
            }

            // Make sure all descriptions start capitalized, end with a period/exclamation,
            // and only contain valid characters
            Item item = itemNamesies.getItem();
            String description = item.getDescription();
            TestUtils.assertDescription(item.getName(), description, "[A-Z][a-zA-Z0-9.,'é\\- ]+[.!]");
        }
    }

    @Test
    public void categoryTest() {
        for (ItemNamesies itemNamesies : ItemNamesies.values()) {
            Item item = itemNamesies.getItem();

            // If it has battle categories, then it must be a BattleUseItem
            Assert.assertEquals(
                    item.getName(),
                    item instanceof BattleUseItem || item instanceof BallItem,
                    item.getBattleBagCategories().iterator().hasNext()
            );

            // Berry category iff it is a berry
            Assert.assertEquals(
                    item.getName() + " " + item.getBagCategory(),
                    item instanceof Berry,
                    item.getBagCategory() == BagCategory.BERRY
            );

            // Category berries can't be for status moves
            if (item instanceof CategoryBerry) {
                MoveCategory category = ((CategoryBerry)item).getCategory();
                Assert.assertNotNull(item.getName(), category);
                Assert.assertNotEquals(item.getName(), MoveCategory.STATUS, category);
            }

            // Only Key Items and TMs do not have quantities
            Assert.assertEquals(
                    item.getName(),
                    item.getBagCategory() == BagCategory.KEY_ITEM || item.getBagCategory() == BagCategory.TM,
                    !item.hasQuantity()
            );
        }
    }

    @Test
    public void priceTest() {
        for (ItemNamesies itemNamesies : ItemNamesies.values()) {
            Item item = itemNamesies.getItem();

            if (itemNamesies == ItemNamesies.NO_ITEM) {
                continue;
            }

            // If it has a quantity, then it should have a price
            Set<ItemNamesies> noPrice = EnumSet.of(ItemNamesies.MASTER_BALL, ItemNamesies.SAFARI_BALL);
            if (noPrice.contains(itemNamesies)) {
                Assert.assertEquals(item.getName(), 0, item.getPrice());
            } else if (item.hasQuantity()) {
                Assert.assertTrue(item.getName(), item.getPrice() > 1);
                Assert.assertTrue(item.getName(), item.getPrice() > item.getSellPrice());
            } else {
                Assert.assertEquals(item.getName(), -1, item.getPrice());
            }
        }
    }

    @Test
    public void swapItemsTest() {
        // Swapping items works differently for wild battles vs trainer battles
        swapItemsTest(false);
        swapItemsTest(true);
    }

    private void swapItemsTest(boolean isTrainerBattle) {
        TestBattle battle = TestBattle.create(isTrainerBattle, PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        defending.giveItem(ItemNamesies.WATER_STONE);

        // Thief -- confirm stolen item
        battle.fight(AttackNamesies.THIEF, AttackNamesies.ENDURE);
        battle.emptyHeal();
        attacking.assertHoldingItem(ItemNamesies.WATER_STONE);
        defending.assertNotHoldingItem();

        // Bestow -- confirm item transferred
        battle.attackingFight(AttackNamesies.BESTOW);
        attacking.assertNotHoldingItem();
        defending.assertHoldingItem(ItemNamesies.WATER_STONE);

        // Magician -- steal item back
        attacking.withAbility(AbilityNamesies.MAGICIAN);
        battle.fight(AttackNamesies.SWIFT, AttackNamesies.ENDURE);
        battle.emptyHeal();
        attacking.assertHoldingItem(ItemNamesies.WATER_STONE);
        defending.assertNotHoldingItem();

        // Trick -- swap item back
        battle.emptyHeal();
        battle.attackingFight(AttackNamesies.TRICK);
        attacking.assertNotHoldingItem();
        defending.assertHoldingItem(ItemNamesies.WATER_STONE);

        // Sticky Hold -- Item can't be swapped
        battle.emptyHeal();
        defending.withAbility(AbilityNamesies.STICKY_HOLD);
        battle.attackingFight(AttackNamesies.SWITCHEROO);
        attacking.assertNotHoldingItem();
        defending.assertHoldingItem(ItemNamesies.WATER_STONE);
        battle.fight(AttackNamesies.KNOCK_OFF, AttackNamesies.ENDURE);
        battle.emptyHeal();
        attacking.assertNotHoldingItem();
        defending.assertHoldingItem(ItemNamesies.WATER_STONE);
        attacking.withAbility(AbilityNamesies.PICKPOCKET);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.TACKLE);
        battle.emptyHeal();
        attacking.assertNotHoldingItem();
        defending.assertHoldingItem(ItemNamesies.WATER_STONE);

        // Mold breaker overrules Sticky Hold
        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        battle.attackingFight(AttackNamesies.TRICK);
        attacking.assertHoldingItem(ItemNamesies.WATER_STONE);
        defending.assertNotHoldingItem();

        // Pickpocket -- steal item on contact
        // TODO: Look at this -- it looks like there's nothing to steal since it's done in the wrong order
        attacking.withAbility(AbilityNamesies.PICKPOCKET);
        defending.withAbility(AbilityNamesies.NO_ABILITY);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.TACKLE);
        battle.emptyHeal();
        attacking.assertHoldingItem(ItemNamesies.WATER_STONE);
        defending.assertNotHoldingItem();
    }

    @Test
    public void stickyBarbTest() {
        // Swapping items works differently for wild battles vs trainer battles
        stickyBarbTest(false);
        stickyBarbTest(true);
    }

    private void stickyBarbTest(boolean isTrainerBattle) {
        TestBattle battle = TestBattle.create(isTrainerBattle, PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // So I don't have to worry about dying from Sticky Barb's end turn effect
        attacking.withAbility(AbilityNamesies.MAGIC_GUARD);
        defending.withAbility(AbilityNamesies.MAGIC_GUARD);

        // Give Sticky Barb
        attacking.giveItem(ItemNamesies.WATER_STONE);
        defending.giveItem(ItemNamesies.STICKY_BARB);
        attacking.assertHoldingItem(ItemNamesies.WATER_STONE);
        defending.assertHoldingItem(ItemNamesies.STICKY_BARB);

        // Switcheroo -- swap items
        battle.attackingFight(AttackNamesies.SWITCHEROO);
        attacking.assertHoldingItem(ItemNamesies.STICKY_BARB);
        defending.assertHoldingItem(ItemNamesies.WATER_STONE);

        // Knock off -- remove defending item
        battle.fight(AttackNamesies.KNOCK_OFF, AttackNamesies.ENDURE);
        battle.emptyHeal();
        attacking.assertHoldingItem(ItemNamesies.STICKY_BARB);
        defending.assertNotHoldingItem();

        // Bestow -- transfer Sticky Barb
        Assert.assertTrue(attacking.canGiftItem(defending));
        battle.attackingFight(AttackNamesies.BESTOW);
        attacking.assertNotHoldingItem();
        defending.assertHoldingItem(ItemNamesies.STICKY_BARB);

        // Sticky Barb -- transfer on contact
        battle.fight(AttackNamesies.TACKLE, AttackNamesies.ENDURE);
        battle.emptyHeal();
        attacking.assertHoldingItem(ItemNamesies.STICKY_BARB);
        defending.assertNotHoldingItem();
    }

    @Test
    public void mentalHerbTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withGender(Gender.FEMALE);
        TestPokemon defending = battle.getDefending().withGender(Gender.MALE);

        battle.attackingFight(AttackNamesies.ATTRACT);
        defending.assertHasEffect(PokemonEffectNamesies.INFATUATION);

        attacking.giveItem(ItemNamesies.MENTAL_HERB);
        battle.attackingFight(AttackNamesies.FLING);
        defending.assertNoEffect(PokemonEffectNamesies.INFATUATION);
        attacking.assertNotHoldingItem();

        battle.defendingFight(AttackNamesies.CONFUSE_RAY);
        attacking.assertHasEffect(PokemonEffectNamesies.CONFUSION);

        // Mental Herb cures at the end of the turn
        attacking.giveItem(ItemNamesies.MENTAL_HERB);
        battle.attackingFight(AttackNamesies.SPLASH);
        attacking.assertNoEffect(PokemonEffectNamesies.CONFUSION);
    }

    @Test
    public void itemBlockerTest() {
        itemBlockerTest(true, new TestInfo());

        // Klutz only affects the user
        itemBlockerTest(true, new TestInfo().attacking(AbilityNamesies.KLUTZ));
        itemBlockerTest(false, new TestInfo().defending(AbilityNamesies.KLUTZ));

        // Embargo only affects the victim
        itemBlockerTest(false, new TestInfo().attackingFight(AttackNamesies.EMBARGO));
        itemBlockerTest(true, new TestInfo().defendingFight(AttackNamesies.EMBARGO));

        // Magic Room affects everyone
        itemBlockerTest(false, new TestInfo().attackingFight(AttackNamesies.MAGIC_ROOM));
        itemBlockerTest(false, new TestInfo().defendingFight(AttackNamesies.MAGIC_ROOM));
    }

    private void itemBlockerTest(boolean success, TestInfo testInfo) {
        TestBattle battle = testInfo.defending(PokemonNamesies.SQUIRTLE).createBattle();
        TestPokemon defending = battle.getDefending().withItem(ItemNamesies.ABSORB_BULB);

        testInfo.manipulate(battle);

        battle.fight(AttackNamesies.WATER_GUN, AttackNamesies.ENDURE);

        // Should not be holding an item (for battle purposes) for either case
        // If success, item is consumed and is no longer holding anything
        // If not success, item is suppressed but should not be consumed
        defending.assertNotHoldingItem();

        Assert.assertEquals(success, defending.getActualHeldItem().namesies() == ItemNamesies.NO_ITEM);
        defending.assertEffect(success, PokemonEffectNamesies.CONSUMED_ITEM);

        // If successful, should increase Sp. Attack by one
        defending.assertStages(new TestStages().set(success ? 1 : 0, Stat.SP_ATTACK));
    }

    @Test
    public void powerHerbTest() {
        PokemonManipulator notFullHealth = (battle, attacking, defending) -> {
            attacking.assertFullHealth();
            defending.assertNotFullHealth();
            attacking.assertNoStages();
            defending.assertNoStages();

            // Additionally fully heals the defending so this can be used in subsequent turns and still be meaningful
            defending.fullyHeal();
        };

        PokemonManipulator charging = (battle, attacking, defending) -> {
            MultiTurnMove multiTurnMove = (MultiTurnMove)attacking.getAttack();
            Assert.assertTrue(multiTurnMove.isCharging());

            attacking.assertFullHealth();
            defending.assertFullHealth();

            attacking.assertNoStages();
            defending.assertNoStages();
        };

        // Power Herb is consumed and damage is dealt first turn, charges on the second
        powerHerbTest(AttackNamesies.SOLAR_BEAM, true, notFullHealth, charging);

        // Power Herb is consumed and damage is dealt first turn -- defense is NOT raised
        // Charges on the second turn and defense is raised
        powerHerbTest(AttackNamesies.SKULL_BASH, true, notFullHealth, (battle, attacking, defending) -> {
            MultiTurnMove multiTurnMove = (MultiTurnMove)attacking.getAttack();
            Assert.assertTrue(multiTurnMove.isCharging());
            attacking.assertStages(new TestStages().set(1, Stat.DEFENSE));
            defending.assertNoStages();
        });

        // Power Herb is consumed and stats are raised
        powerHerbTest(AttackNamesies.GEOMANCY, true, (battle, attacking, defending) -> {
            attacking.assertFullHealth();
            defending.assertFullHealth();
            attacking.assertStages(new TestStages().set(2, Stat.SP_ATTACK, Stat.SP_DEFENSE, Stat.SPEED));
            defending.assertNoStages();

            // Reset stages so next check can be more meaningful
            attacking.getStages().reset();
        }, charging);

        // Power Herb does not work with semi-invulnerable moves
        powerHerbTest(AttackNamesies.FLY, false, (battle, attacking, defending) -> {
            Assert.assertTrue(attacking.isSemiInvulnerable());
            Assert.assertTrue(attacking.isSemiInvulnerableFlying());
            attacking.assertFullHealth();
            defending.assertFullHealth();
        }, notFullHealth);

        // Don't consume item or anything for non-multiturn moves
        powerHerbTest(AttackNamesies.TACKLE, false, notFullHealth, notFullHealth);

        // Should not consume Power Herb when it is Sunny
        powerHerbTest(
                AttackNamesies.SOLAR_BEAM,
                true, false,
                PokemonManipulator.attackingAttack(AttackNamesies.SUNNY_DAY),
                notFullHealth, notFullHealth
        );
    }

    private void powerHerbTest(AttackNamesies attackingMove,
                               boolean consumeItem,
                               PokemonManipulator afterFirstTurn,
                               PokemonManipulator afterSecondTurn) {
        this.powerHerbTest(attackingMove, false, consumeItem, PokemonManipulator.empty(), afterFirstTurn, afterSecondTurn);
    }

    private void powerHerbTest(AttackNamesies attackingMove,
                               boolean skipCharge,
                               boolean consumeItem,
                               PokemonManipulator beforeFirstTurn,
                               PokemonManipulator afterFirstTurn,
                               PokemonManipulator afterSecondTurn) {
        Assert.assertFalse(skipCharge && consumeItem);

        TestBattle battle = TestBattle.create(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE);
        TestPokemon attacking = battle.getAttacking().withItem(ItemNamesies.POWER_HERB);
        TestPokemon defending = battle.getDefending();

        beforeFirstTurn.manipulate(battle);

        attacking.setMove(new Move(attackingMove));
        defending.setMove(new Move(AttackNamesies.SPLASH));

        int attackingPP = attacking.getMove().getPP();
        int defendingPP = defending.getMove().getPP();

        battle.fight();

        attacking.assertExpectedConsumedItem(consumeItem);

        boolean isMultiTurn = attacking.getAttack() instanceof MultiTurnMove;
        boolean fullyExecuted = !isMultiTurn || skipCharge || consumeItem;

        // If it is a Multi-turn move and the item was not consumed, then it should still be charging
        Assert.assertEquals(attackingPP - (fullyExecuted ? 1 : 0), attacking.getMove().getPP());
        Assert.assertEquals(defendingPP - 1, defending.getMove().getPP());

        afterFirstTurn.manipulate(battle);

        battle.fight();

        attacking.assertExpectedConsumedItem(consumeItem);

        Assert.assertEquals(attackingPP - (!isMultiTurn || skipCharge ? 2 : 1), attacking.getMove().getPP());
        Assert.assertEquals(defendingPP - 2, defending.getMove().getPP());

        afterSecondTurn.manipulate(battle);
    }

    @Test
    public void swapConsumeItemTest() {
        // Swapping items works differently for wild battles vs trainer battles
        swapConsumeItemTest(false);
        swapConsumeItemTest(true);
    }

    private void swapConsumeItemTest(boolean isTrainerBattle) {
        TestBattle battle = TestBattle.create(isTrainerBattle, PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
        TestPokemon attacking = battle.getAttacking().withItem(ItemNamesies.LUM_BERRY);
        TestPokemon defending = battle.getDefending().withItem(ItemNamesies.RAWST_BERRY);

        // Lum Berry should activate to remove the burn
        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        defending.assertLastMoveSucceeded(true);
        attacking.assertNoStatus();

        // Lum Berry has already been consumed, so the burn should remain
        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        attacking.assertHasStatus(StatusNamesies.BURNED);

        // Swap items to retrieve the Rawst Berry, which should activate to remove the burn
        battle.attackingFight(AttackNamesies.TRICK);
        attacking.assertNoStatus();
        attacking.assertNotHoldingItem();
        defending.assertNotHoldingItem();

        // Rawst Berry has already been consumed, so the burn should remain
        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        attacking.assertHasStatus(StatusNamesies.BURNED);
    }

    @Test
    public void destinyKnotTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withGender(Gender.FEMALE);
        TestPokemon defending = battle.getDefending().withGender(Gender.MALE);

        battle.attackingFight(AttackNamesies.ATTRACT);
        attacking.assertNoEffect(PokemonEffectNamesies.INFATUATION);
        defending.assertHasEffect(PokemonEffectNamesies.INFATUATION);

        battle.clearAllEffects();
        attacking.assertNoEffect(PokemonEffectNamesies.INFATUATION);
        defending.assertNoEffect(PokemonEffectNamesies.INFATUATION);

        // Destiny Knot causes the caster to be infatuated as well
        defending.withItem(ItemNamesies.DESTINY_KNOT);
        battle.attackingFight(AttackNamesies.ATTRACT);
        attacking.assertHasEffect(PokemonEffectNamesies.INFATUATION);
        defending.assertHasEffect(PokemonEffectNamesies.INFATUATION);
    }

    @Test
    public void healBlockTest() {
        TestBattle battle = TestBattle.create(PokemonNamesies.HAPPINY, PokemonNamesies.KARTANA);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        battle.fight(AttackNamesies.BELLY_DRUM, AttackNamesies.HEAL_BLOCK);
        attacking.assertHealthRatio(.5);
        defending.assertFullHealth();
        attacking.assertHasEffect(PokemonEffectNamesies.HEAL_BLOCK);
        defending.assertNoEffect(PokemonEffectNamesies.HEAL_BLOCK);

        // Heal Block doesn't affect Use Items -- (THEY STILL WORK)
        PokemonManipulator.useItem(ItemNamesies.SITRUS_BERRY).manipulate(battle);
        attacking.assertHealthRatio(.75, 1);
        attacking.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
        attacking.assertNoEffect(PokemonEffectNamesies.EATEN_BERRY);
        attacking.assertHasEffect(PokemonEffectNamesies.HEAL_BLOCK);

        attacking.giveItem(ItemNamesies.SITRUS_BERRY);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.LEAF_BLADE);
        attacking.assertHp(1);
        attacking.assertNotConsumedItem();
        attacking.assertHasEffect(PokemonEffectNamesies.HEAL_BLOCK);
    }

    @Test
    public void lifeOrbTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withItem(ItemNamesies.LIFE_ORB);
        TestPokemon defending = battle.getDefending();

        attacking.setExpectedDamageModifier(5324/4096.0);
        battle.attackingFight(AttackNamesies.CONSTRICT);
        attacking.assertHealthRatio(.9);
        defending.assertNotFullHealth();

        battle.emptyHeal();
        attacking.assertFullHealth();
        defending.assertFullHealth();

        // TODO: Life Orb doesn't currently work with multi strike moves
//        battle.attackingFight(AttackNamesies.FURY_SWIPES);
//        attacking.assertHealthRatio(.9);
//        defending.assertNotFullHealth();

        // Fixed damage moves do not effect Life Orb (do not increase power or take damage)
        battle.attackingFight(AttackNamesies.SONIC_BOOM);
        attacking.assertFullHealth();
        defending.assertMissingHp(20);

        battle.emptyHeal();
        attacking.assertFullHealth();
        defending.assertFullHealth();

        // Magic Guard prevents damage from Life Orb
        attacking.withAbility(AbilityNamesies.MAGIC_GUARD);
        battle.attackingFight(AttackNamesies.CONSTRICT);
        attacking.assertFullHealth();
        defending.assertNotFullHealth();
    }

    @Test
    public void weatherExtenderTest() {
        weatherExtenderTest(WeatherNamesies.RAINING, ItemNamesies.DAMP_ROCK, AttackNamesies.RAIN_DANCE, AbilityNamesies.DRIZZLE);
        weatherExtenderTest(WeatherNamesies.SUNNY, ItemNamesies.HEAT_ROCK, AttackNamesies.SUNNY_DAY, AbilityNamesies.DROUGHT);
        weatherExtenderTest(WeatherNamesies.HAILING, ItemNamesies.ICY_ROCK, AttackNamesies.HAIL, AbilityNamesies.SNOW_WARNING);
        weatherExtenderTest(WeatherNamesies.SANDSTORM, ItemNamesies.SMOOTH_ROCK, AttackNamesies.SANDSTORM, AbilityNamesies.SAND_STREAM);

        // Sand Spit starts a Sandstorm when hit by an attack
        weatherExtenderTest(
                WeatherNamesies.SANDSTORM, ItemNamesies.SMOOTH_ROCK,
                new TestInfo().attacking(AbilityNamesies.SAND_SPIT).defendingFight(AttackNamesies.SWIFT)
        );
    }

    private void weatherExtenderTest(WeatherNamesies weather, ItemNamesies extender, AttackNamesies weatherAttack, AbilityNamesies weatherAbility) {
        // Adds weather by playing the standard start weather attack (like Rain Dance)
        weatherExtenderTest(weather, extender, new TestInfo().attackingFight(weatherAttack));

        // Adds weather by switching to entry ability (like Sand Stream)
        weatherExtenderTest(
                weather, extender,
                new TestInfo().asTrainerBattle()
                              .with((battle, attacking, defending) -> {
                                  // Adds another Pokemon with weather entry ability to switch to
                                  // and swaps the item (either no item or the extender item) with it
                                  TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.SQUIRTLE);
                                  attacking2.withAbility(weatherAbility);
                                  attacking2.withItem(attacking.getHeldItem().namesies());
                                  attacking.removeItem();

                                  battle.assertFront(attacking);
                                  battle.assertWeather(WeatherNamesies.CLEAR_SKIES);

                                  // Bye bye blue sky
                                  battle.defendingFight(AttackNamesies.WHIRLWIND);
                                  battle.assertFront(attacking2);
                                  battle.assertWeather(weather);
                              })
        );
    }

    private void weatherExtenderTest(WeatherNamesies weather, ItemNamesies extender, TestInfo testInfo) {
        testInfo.with((battle, attacking, defending) -> {
            // Should already have the desired weather effect in play and should not have any turns since it started
            battle.assertWeather(weather);

            // Just do nothing and let the weather resolve itself
            battle.splashFight();
            battle.splashFight();
            battle.splashFight();
            battle.assertWeather(weather);

            // Okay one last time before Clear Skies (unless holding the extender)
            battle.splashFight();
        });

        // Without and with the attacker holding the extender item
        testInfo.doubleTake(
                PokemonManipulator.giveAttackingItem(extender),
                (battle, attacking, defending) -> battle.assertWeather(WeatherNamesies.CLEAR_SKIES),
                (battle, attacking, defending) -> {
                    // Weather still in play since it was extended
                    battle.assertWeather(weather);
                    battle.splashFight();
                    battle.splashFight();
                    battle.assertWeather(weather);

                    // Gotta go away sometime though
                    battle.splashFight();
                    battle.assertWeather(WeatherNamesies.CLEAR_SKIES);
                }
        );
    }

    @Test
    public void blunderPolicyTest() {
        // Blunder Policy sharply raises Speed when an attack naturally misses
        blunderPolicyTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER),
                (battle, attacking, defending) -> defending.assertNotFullHealth(),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Failing via type advantage does not trigger Blunder Policy
        // Attack fails because Tackle does not affect Gastly
        blunderPolicyTest(
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.GASTLY),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Failing via semi-invulnerable does not trigger Blunder Policy
        blunderPolicyTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .with((battle, attacking, defending) -> {
                            attacking.setMove(new Move(AttackNamesies.SPLASH));
                            defending.setMove(new Move(AttackNamesies.FLY));

                            // Defending with fly up in the air and next attack will be a forced miss
                            battle.fight();
                            Assert.assertTrue(defending.isSemiInvulnerableFlying());
                            attacking.setExpectedAccuracyBypass(false);
                        }),
                (battle, attacking, defending) -> {
                    attacking.assertNotFullHealth();
                    defending.assertFullHealth();
                    Assert.assertFalse(defending.isSemiInvulnerable());
                }
        );
    }

    private void blunderPolicyTest(TestInfo testInfo, PokemonManipulator samesies) {
        blunderPolicyTest(testInfo, samesies, samesies);
    }

    private void blunderPolicyTest(TestInfo testInfo, PokemonManipulator withoutMiss, PokemonManipulator withMiss) {
        blunderPolicyTest(false, testInfo, withoutMiss);
        blunderPolicyTest(true, testInfo, withMiss);
    }

    private void blunderPolicyTest(boolean naturalMiss, TestInfo testInfo, PokemonManipulator postCheck) {
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking().withItem(ItemNamesies.BLUNDER_POLICY);

        testInfo.manipulate(battle);

        // False Swipe will miss because I am mad with power
        // Note: Setting manually instead of attackingFight for the semi-invulnerable test (needs to finish Fly move)
        attacking.setFailAccuracy(naturalMiss);
        attacking.setMove(new Move(AttackNamesies.FALSE_SWIPE));
        battle.fight();

        // Blunder Policy should be consumed when naturally missing (no accuracy bypass)
        boolean shouldConsume = naturalMiss && attacking.getExpectedAccuracyBypass() == null;
        Assert.assertEquals(shouldConsume, attacking.getMoveData().isNaturalMiss());

        if (shouldConsume) {
            attacking.assertConsumedItem();
            attacking.assertStages(new TestStages().set(2, Stat.SPEED));
        } else {
            attacking.assertHoldingItem(ItemNamesies.BLUNDER_POLICY);
            attacking.assertStages(new TestStages());
        }

        postCheck.manipulate(battle);
    }

    @Test
    public void swapPokemonTest() {
        // Eject Pack causes the holder to switch out when any of its stats are lowered
        ejectPackTest(true, new TestInfo().attackingFight(AttackNamesies.GROWL));

        // Make sure works with multiple stat reductions
        ejectPackTest(true, new TestInfo().attackingFight(AttackNamesies.TICKLE));

        // Ejects even for self-inflicted stat lowers
        ejectPackTest(true, new TestInfo().fight(AttackNamesies.ENDURE, AttackNamesies.LEAF_STORM));

        // Sticky Hold will not prevent ejection because its the holder
        ejectPackTest(true, new TestInfo().defending(AbilityNamesies.STICKY_HOLD)
                                          .attackingFight(AttackNamesies.GROWL));

        // Nothing happens when lowering the other Pokemon's stats
        ejectPackTest(false, new TestInfo().defendingFight(AttackNamesies.GROWL)
                                           .with((battle, attacking, defending) -> attacking.assertStages(new TestStages().set(-1, Stat.ATTACK))));

        // Eject Button causes the holder to switch out when hit by an attack
        ejectButtonTest(true, new TestInfo().attackingFight(AttackNamesies.TACKLE));
        ejectButtonTest(true, new TestInfo().attackingFight(AttackNamesies.SWIFT));
        ejectButtonTest(true, new TestInfo().fight(AttackNamesies.EXPLOSION, AttackNamesies.ENDURE));

        // Eject Button should not trigger when damage is absorbed
        ejectButtonTest(false, new TestInfo().defendingFight(AttackNamesies.SUBSTITUTE)
                                             .attackingFight(AttackNamesies.TACKLE));
    }

    private void ejectPackTest(boolean swapDefending, TestInfo testInfo) {
        testInfo.setup(PokemonManipulator.giveDefendingItem(ItemNamesies.EJECT_PACK));
        testInfo.with((battle, attacking, defending) -> {
            if (swapDefending) {
                TestPokemon front = battle.getDefending();
                Assert.assertNotEquals(front, defending);
                front.assertSpecies(PokemonNamesies.PIKACHU);
                front.assertStages(new TestStages());
                front.assertNotHoldingItem();
            } else {
                battle.assertFront(defending);
                defending.assertStages(new TestStages());
                defending.assertHoldingItem(ItemNamesies.EJECT_PACK);
            }
        });
        swapPokemonTest(swapDefending, testInfo);
    }

    private void ejectButtonTest(boolean swapDefending, TestInfo testInfo) {
        testInfo.setup(PokemonManipulator.giveDefendingItem(ItemNamesies.EJECT_BUTTON));
        testInfo.with((battle, attacking, defending) -> {
            if (swapDefending) {
                TestPokemon front = battle.getDefending();
                Assert.assertNotEquals(front, defending);
                front.assertSpecies(PokemonNamesies.PIKACHU);
                front.assertNotHoldingItem();
                defending.assertNotFullHealth();
            } else {
                battle.assertFront(defending);
                defending.assertStages(new TestStages());
                defending.assertHoldingItem(ItemNamesies.EJECT_BUTTON);
            }
        });
        swapPokemonTest(swapDefending, testInfo);
    }

    private void swapPokemonTest(boolean swapDefending, TestInfo testInfo) {
        TestBattle battle = testInfo.asTrainerBattle().createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();
        TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.SQUIRTLE);
        TestPokemon defending2 = battle.addDefending(PokemonNamesies.PIKACHU);

        // Exactly one front Pokemon should be holding an item
        boolean attackingItem = attacking.isHoldingItem();
        boolean defendingItem = defending.isHoldingItem();
        Assert.assertNotEquals(attackingItem, defendingItem);

        TestPokemon itemHolder = attackingItem ? attacking : defending;
        ItemNamesies itemNamesies = itemHolder.getHeldItem().namesies();
        Assert.assertNotEquals(itemNamesies, ItemNamesies.NO_ITEM);

        battle.assertFront(attacking);
        battle.assertFront(defending);

        testInfo.manipulate(battle);

        battle.assertFront(attacking);
        battle.assertFront(swapDefending ? defending2 : defending);

        if (swapDefending) {
            itemHolder.assertConsumedItem();
        } else {
            itemHolder.assertHoldingItem(itemNamesies);
        }
    }

    // More like an Entry Hazard test disguised as a Heavy-Duty Boots test am I right??
    @Test
    public void heavyDutyBootsTest() {
        // Spikes takes 1/8 damage on entry with a single layer
        heavyDutyBootsTest(
                PokemonNamesies.SQUIRTLE,
                AttackNamesies.SPIKES,
                new TestInfo(),
                (battle, attacking, defending) -> defending.assertHealthRatio(7/8.0),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Spikes takes 1/6 damage on entry with a double layer
        heavyDutyBootsTest(
                PokemonNamesies.SQUIRTLE,
                AttackNamesies.SPIKES,
                new TestInfo().attackingFight(AttackNamesies.SPIKES),
                (battle, attacking, defending) -> defending.assertHealthRatio(5/6.0),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Spikes takes 1/4 damage on entry with a triple layer or more
        heavyDutyBootsTest(
                PokemonNamesies.SQUIRTLE,
                AttackNamesies.SPIKES,
                new TestInfo().attackingFight(AttackNamesies.SPIKES)
                              .attackingFight(AttackNamesies.SPIKES),
                (battle, attacking, defending) -> defending.assertHealthRatio(3/4.0),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );
        heavyDutyBootsTest(
                PokemonNamesies.SQUIRTLE,
                AttackNamesies.SPIKES,
                new TestInfo().attackingFight(AttackNamesies.SPIKES)
                              .attackingFight(AttackNamesies.SPIKES)
                              .attackingFight(AttackNamesies.SPIKES)
                              .attackingFight(AttackNamesies.SPIKES)
                              .attackingFight(AttackNamesies.SPIKES),
                (battle, attacking, defending) -> defending.assertHealthRatio(3/4.0),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Flying-type Pokemon are immune to the Spikes
        heavyDutyBootsTest(
                PokemonNamesies.PIDGEOT,
                AttackNamesies.SPIKES,
                new TestInfo(),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Pokemon with Levitate are immune to the Spikes
        heavyDutyBootsTest(
                PokemonNamesies.SQUIRTLE,
                AttackNamesies.SPIKES,
                new TestInfo().with((battle, attacking, defending) -> battle.getOtherDefending().withAbility(AbilityNamesies.LEVITATE)),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Toxic Spikes poisons the Pokemon on entry with a single layer
        heavyDutyBootsTest(
                PokemonNamesies.SQUIRTLE,
                AttackNamesies.TOXIC_SPIKES,
                new TestInfo(),
                (battle, attacking, defending) -> {
                    defending.assertRegularPoison();
                    defending.assertHealthRatio(7/8.0);
                },
                (battle, attacking, defending) -> {
                    defending.assertNoStages();
                    defending.assertFullHealth();
                }
        );

        // Toxic Spikes badly poisons the Pokemon on entry with a double layer
        heavyDutyBootsTest(
                PokemonNamesies.SQUIRTLE,
                AttackNamesies.TOXIC_SPIKES,
                new TestInfo().attackingFight(AttackNamesies.TOXIC_SPIKES),
                (battle, attacking, defending) -> {
                    defending.assertBadPoison();
                    defending.assertHealthRatio(15/16.0);
                    battle.assertHasEffect(defending, TeamEffectNamesies.TOXIC_SPIKES);
                },
                (battle, attacking, defending) -> {
                    defending.assertNoStages();
                    defending.assertFullHealth();
                    battle.assertHasEffect(defending, TeamEffectNamesies.TOXIC_SPIKES);
                }
        );

        // Toxic Spikes will be removed if a Poison Pokemon encounters them even when wearing the boots
        heavyDutyBootsTest(
                true,
                PokemonNamesies.GRIMER,
                AttackNamesies.TOXIC_SPIKES,
                new TestInfo().attackingFight(AttackNamesies.TOXIC_SPIKES),
                (battle, attacking, defending) -> {
                    defending.assertNoStatus();
                    defending.assertFullHealth();
                    battle.assertNoEffect(defending, TeamEffectNamesies.TOXIC_SPIKES);
                }
        );

        // Toxic Spikes will not be removed if a Poison Pokemon is levitating though
        heavyDutyBootsTest(
                PokemonNamesies.ZUBAT,
                AttackNamesies.TOXIC_SPIKES,
                new TestInfo().attackingFight(AttackNamesies.TOXIC_SPIKES),
                (battle, attacking, defending) -> {
                    defending.assertNoStatus();
                    defending.assertFullHealth();
                    battle.assertHasEffect(defending, TeamEffectNamesies.TOXIC_SPIKES);
                }
        );

        // Sticky Web lowers Speed by 1 stage when entering
        heavyDutyBootsTest(
                PokemonNamesies.GRIMER,
                AttackNamesies.STICKY_WEB,
                new TestInfo(),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(-1, Stat.SPEED)),
                (battle, attacking, defending) -> defending.assertStages(new TestStages())
        );

        // Contrary will raise Speed instead
        heavyDutyBootsTest(
                PokemonNamesies.GRIMER,
                AttackNamesies.STICKY_WEB,
                new TestInfo().with((battle, attacking, defending) -> battle.getOtherDefending().withAbility(AbilityNamesies.CONTRARY)),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(1, Stat.SPEED)),
                (battle, attacking, defending) -> defending.assertStages(new TestStages())
        );

        // Sticky Web doesn't fuck with flyers
        heavyDutyBootsTest(
                PokemonNamesies.DRAGONITE,
                AttackNamesies.STICKY_WEB,
                new TestInfo(),
                (battle, attacking, defending) -> defending.assertStages(new TestStages())
        );

        // Stealth Rock does care about Levitation but still cares about boots
        heavyDutyBootsTest(
                PokemonNamesies.DRAGONITE,
                AttackNamesies.STEALTH_ROCK,
                new TestInfo(),
                (battle, attacking, defending) -> defending.assertHealthRatio(3/4.0),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );
    }

    private void heavyDutyBootsTest(PokemonNamesies pokes, AttackNamesies hazardSetup, TestInfo testInfo, PokemonManipulator samesies) {
        heavyDutyBootsTest(false, pokes, hazardSetup, testInfo, samesies);
    }

    private void heavyDutyBootsTest(boolean absorbed, PokemonNamesies pokes, AttackNamesies hazardSetup, TestInfo testInfo, PokemonManipulator samesies) {
        heavyDutyBootsTest(absorbed, pokes, hazardSetup, testInfo, samesies, samesies);
    }

    private void heavyDutyBootsTest(PokemonNamesies pokes, AttackNamesies hazardSetup, TestInfo testInfo, PokemonManipulator withoutBoots, PokemonManipulator withBoots) {
        heavyDutyBootsTest(false, pokes, hazardSetup, testInfo, withoutBoots, withBoots);
    }

    private void heavyDutyBootsTest(boolean absorbed, PokemonNamesies pokes, AttackNamesies hazardAttack, TestInfo testInfo, PokemonManipulator withoutBoots, PokemonManipulator withBoots) {
        TeamEffectNamesies hazardEffect = (TeamEffectNamesies)hazardAttack.getNewAttack().getEffect();
        Assert.assertTrue(hazardEffect.getEffect() instanceof EntryHazard);

        // Add additional Pokemon to swap to in setup
        testInfo.asTrainerBattle().setup((battle, attacking, defending) -> battle.addDefending(pokes));

        testInfo.with((battle, attacking, defending) -> {
            // Set up entry hazard
            battle.attackingFight(hazardAttack);
            battle.assertHasEffect(defending, hazardEffect);

            // Use Whirlwind to draw out other Pokemon to handle the entry hazard effects
            battle.attackingFight(AttackNamesies.WHIRLWIND);
            battle.assertEffect(!absorbed, defending, hazardEffect);
        });

        testInfo.doubleTake(
                (battle, attacking, defending) -> battle.getOtherDefending().withItem(ItemNamesies.HEAVY_DUTY_BOOTS),
                withoutBoots,
                withBoots
        );
    }

    @Test
    public void throatSprayTest() {
        // Throat Spray increases Sp. Attack by 1 stages when using a sound-based move (like Growl)
        throatSprayTest(
                true, AttackNamesies.GROWL, new TestInfo(),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(-1, Stat.ATTACK))
        );

        // If move fails for something like sound-immunity, Throat Spray should not activate
        throatSprayTest(
                false, AttackNamesies.GROWL, new TestInfo().defending(AbilityNamesies.SOUNDPROOF),
                (battle, attacking, defending) -> defending.assertStages(new TestStages())
        );

        // Currently does not count as a failure if individual effects fail like unable to decrease stats
        throatSprayTest(
                true, AttackNamesies.GROWL, new TestInfo().defending(AbilityNamesies.HYPER_CUTTER),
                (battle, attacking, defending) -> defending.assertStages(new TestStages())
        );

        // Throat Spray will fail if cannot execute due to Throat Chop
        throatSprayTest(
                false, AttackNamesies.GROWL, new TestInfo().fight(AttackNamesies.ENDURE, AttackNamesies.THROAT_CHOP),
                (battle, attacking, defending) -> defending.assertStages(new TestStages())
        );

        // Throat Spray will not be consumed if Sp. Attack is already maximized
        throatSprayTest(
                false, AttackNamesies.GROWL, new TestInfo().attackingFight(AttackNamesies.NASTY_PLOT)
                                                           .attackingFight(AttackNamesies.NASTY_PLOT)
                                                           .attackingFight(AttackNamesies.NASTY_PLOT),
                new TestStages().set(6, Stat.SP_ATTACK),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(-1, Stat.ATTACK))
        );

        // Or if minimized with Contrary
        throatSprayTest(
                false, AttackNamesies.GROWL, new TestInfo().attacking(AbilityNamesies.CONTRARY)
                                                           .attackingFight(AttackNamesies.NASTY_PLOT)
                                                           .attackingFight(AttackNamesies.NASTY_PLOT)
                                                           .attackingFight(AttackNamesies.NASTY_PLOT),
                new TestStages().set(-6, Stat.SP_ATTACK),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(-1, Stat.ATTACK))
        );

        // Throat Spray only works with sound-based moves
        throatSprayTest(
                false, AttackNamesies.TAIL_WHIP, new TestInfo(),
                (battle, attacking, defending) -> defending.assertStages(new TestStages().set(-1, Stat.DEFENSE))
        );

        // Throat Spray works with damaging moves as well
        throatSprayTest(
                true, AttackNamesies.BOOMBURST, new TestInfo(),
                (battle, attacking, defending) -> defending.assertNotFullHealth()
        );

        // But will not activate if uneffective for type-advantage
        throatSprayTest(
                false, AttackNamesies.BOOMBURST, new TestInfo().defending(PokemonNamesies.GASTLY),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Soundproof blocks damaging moves as well
        throatSprayTest(
                false, AttackNamesies.BOOMBURST, new TestInfo().defending(AbilityNamesies.SOUNDPROOF),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Throat Spray fails for Water Absorb + Liquid Voice + Boomburst
        throatSprayTest(
                false, AttackNamesies.BOOMBURST,
                new TestInfo().attacking(AbilityNamesies.LIQUID_VOICE)
                              .defending(AbilityNamesies.WATER_ABSORB)
                              .defendingFight(AttackNamesies.BELLY_DRUM),
                (battle, attacking, defending) -> defending.assertHealthRatio(.75, 1)
        );

        // Throat Spray will activate from self-target moves like Clangorous Soul
        throatSprayTest(
                true, AttackNamesies.CLANGOROUS_SOUL, new TestInfo(),
                new TestStages().set(1, Stat.ATTACK, Stat.DEFENSE, Stat.SP_DEFENSE, Stat.SPEED).set(2, Stat.SP_ATTACK),
                (battle, attacking, defending) -> attacking.assertHealthRatio(2/3.0)
        );

        // Soundproof should not block self-target attacks
        throatSprayTest(
                true, AttackNamesies.CLANGOROUS_SOUL, new TestInfo().attacking(AbilityNamesies.SOUNDPROOF),
                new TestStages().set(1, Stat.ATTACK, Stat.DEFENSE, Stat.SP_DEFENSE, Stat.SPEED).set(2, Stat.SP_ATTACK),
                (battle, attacking, defending) -> attacking.assertHealthRatio(2/3.0)
        );

        // Perish Song will activate Throat Spray
        throatSprayTest(
                true, AttackNamesies.PERISH_SONG, new TestInfo(),
                (battle, attacking, defending) -> {
                    attacking.assertHasEffect(PokemonEffectNamesies.PERISH_SONG);
                    defending.assertHasEffect(PokemonEffectNamesies.PERISH_SONG);
                }
        );

        // Even when neither target is affected
        throatSprayTest(
                true, AttackNamesies.PERISH_SONG, new TestInfo().attacking(AbilityNamesies.SOUNDPROOF).defending(AbilityNamesies.SOUNDPROOF),
                (battle, attacking, defending) -> {
                    attacking.assertNoEffect(PokemonEffectNamesies.PERISH_SONG);
                    defending.assertNoEffect(PokemonEffectNamesies.PERISH_SONG);
                }
        );

        // Grass Whistle is affected by Throat Spray
        throatSprayTest(
                true, AttackNamesies.GRASS_WHISTLE, new TestInfo(),
                (battle, attacking, defending) -> defending.assertHasStatus(StatusNamesies.ASLEEP)
        );

        // Even if target cannot be put to sleep
        throatSprayTest(
                true, AttackNamesies.GRASS_WHISTLE, new TestInfo().defending(AbilityNamesies.INSOMNIA),
                (battle, attacking, defending) -> defending.assertNoStatus()
        );
    }

    private void throatSprayTest(boolean consumed, AttackNamesies attack, TestInfo testInfo, PokemonManipulator afterCheck) {
        TestStages stages = consumed ? new TestStages().set(1, Stat.SP_ATTACK) : new TestStages();
        throatSprayTest(consumed, attack, testInfo, stages, afterCheck);
    }

    private void throatSprayTest(boolean consumed, AttackNamesies attack, TestInfo testInfo, TestStages stages, PokemonManipulator afterCheck) {
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking();

        testInfo.manipulate(battle);
        attacking.assertNotHoldingItem();

        attacking.withItem(ItemNamesies.THROAT_SPRAY);
        battle.fight(attack, AttackNamesies.ENDURE);

        if (consumed) {
            attacking.assertConsumedItem();
            attacking.assertNotHoldingItem();
        } else {
            attacking.assertHoldingItem(ItemNamesies.THROAT_SPRAY);
            attacking.assertNotConsumedItem();
        }

        attacking.assertStages(stages);
        afterCheck.manipulate(battle);
    }
}
