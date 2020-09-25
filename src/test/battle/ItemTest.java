package test.battle;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.effect.EffectInterfaces.EntryHazard;
import battle.effect.attack.MultiTurnMove;
import battle.effect.battle.StandardBattleEffectNamesies;
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
import type.Type;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

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
        TestBattle battle = TestBattle.createTrainerBattle(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER);
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
        Assert.assertTrue(attacking.canGiftItem(battle, defending));
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

        // Embargo and Corrosive Gas only affect the victim
        itemBlockerTest(false, new TestInfo().attackingFight(AttackNamesies.EMBARGO));
        itemBlockerTest(true, new TestInfo().defendingFight(AttackNamesies.EMBARGO));
        itemBlockerTest(false, new TestInfo().attackingFight(AttackNamesies.CORROSIVE_GAS));
        itemBlockerTest(true, new TestInfo().defendingFight(AttackNamesies.CORROSIVE_GAS));

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
    public void stickyBarbChangeItemTest() {
        // Defending Pokemon holds Sticky Barb, attacking uses Tackle and the barb is transferred
        stickyBarbChangeItemTest(true, new TestInfo());

        // Should only transfer from physical contact moves
        stickyBarbChangeItemTest(false, AttackNamesies.SWIFT, new TestInfo());
        stickyBarbChangeItemTest(false, new TestInfo().attacking(AbilityNamesies.LONG_REACH));

        // Sticky Barb will not transfer to a Pokemon behind a substitute
        stickyBarbChangeItemTest(
                false,
                new TestInfo().defendingFight(AttackNamesies.SUBSTITUTE)
                              .after((battle, attacking, defending) -> defending.hasEffect(PokemonEffectNamesies.SUBSTITUTE))
        );
    }

    private void stickyBarbChangeItemTest(boolean transfer, TestInfo testInfo) {
        stickyBarbChangeItemTest(transfer, AttackNamesies.TACKLE, testInfo);
    }

    private void stickyBarbChangeItemTest(boolean transfer, AttackNamesies attack, TestInfo testInfo) {
        changedItemTrainerTest(
                attack, AttackNamesies.ENDURE,
                transfer ? ItemNamesies.STICKY_BARB : ItemNamesies.NO_ITEM,
                transfer ? ItemNamesies.NO_ITEM : ItemNamesies.STICKY_BARB,
                testInfo.copy(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .defending(ItemNamesies.STICKY_BARB)
                        .preAfter((battle, attacking, defending) -> {
                            attacking.assertEffect(transfer, PokemonEffectNamesies.CHANGE_ITEM);
                            defending.assertEffect(transfer, PokemonEffectNamesies.CHANGE_ITEM);
                        })
        );
    }

    @Test
    public void corrosiveGasTest() {
        if (true) { return; }

        // Base test will give Water Stone to the defending and make sure it's unusable after Corrosive Gas
        corrosiveGasTest(true, new TestInfo());

        // Corrosive Gas effect should only effect the defending Pokemon even though it's a battle effect
        corrosiveGasTest(false, true, ItemNamesies.POTION, ItemNamesies.WATER_STONE, new TestInfo());

        // Sticky Hold should block Corrosive Gas
        // TODO Fix this it doesn't work
        corrosiveGasTest(false, new TestInfo().defending(AbilityNamesies.STICKY_HOLD));

        // Magic Bounce should melt the user's item
        corrosiveGasTest(
                true, false, ItemNamesies.POTION, ItemNamesies.WATER_STONE,
                new TestInfo().defending(AbilityNamesies.MAGIC_BOUNCE)
        );

        // Corrosive Gas status should persist even after switched out
        corrosiveGasTest(
                true,
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.CHARMANDER)
                        .asTrainerBattle()
                        .with((battle, attacking, defending) -> battle.addDefending(PokemonNamesies.SQUIRTLE)
                                                                      .withItem(ItemNamesies.THUNDER_STONE))
                        .after((battle, attacking, defending) -> {
                            // Confirm Charmander is out and item is already melted
                            defending.assertSpecies(PokemonNamesies.CHARMANDER);
                            defending.assertActualHeldItem(ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM);
                            battle.assertHasEffect(StandardBattleEffectNamesies.CORROSIVE_GAS);

                            // Switch to Squirtle -- item should not be melted
                            battle.attackingFight(AttackNamesies.WHIRLWIND);
                            TestPokemon defending2 = battle.getDefending();
                            defending2.assertSpecies(PokemonNamesies.SQUIRTLE);
                            defending2.assertActualHeldItem(ItemNamesies.THUNDER_STONE);
                            battle.assertHasEffect(StandardBattleEffectNamesies.CORROSIVE_GAS);

                            // Switch back to Charmander -- item should still be melted
                            battle.attackingFight(AttackNamesies.WHIRLWIND);
                            battle.assertFront(defending);
                            battle.assertHasEffect(StandardBattleEffectNamesies.CORROSIVE_GAS);
                            defending.assertActualHeldItem(ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM);
                        })
        );

        // Cannot receive items with Corrosive Gas
        corrosiveGasTest(
                false, true, ItemNamesies.POTION, ItemNamesies.WATER_STONE,
                new TestInfo().after((battle, attacking, defending) -> {
                    attacking.assertActualHeldItem(ItemNamesies.POTION);
                    defending.assertActualHeldItem(ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM);

                    battle.attackingFight(AttackNamesies.BESTOW);
                    attacking.assertLastMoveSucceeded(false);
                    attacking.assertActualHeldItem(ItemNamesies.POTION);
                    defending.assertActualHeldItem(ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM);

                    battle.attackingFight(AttackNamesies.TRICK);
                    attacking.assertLastMoveSucceeded(false);
                    attacking.assertActualHeldItem(ItemNamesies.POTION);
                    defending.assertActualHeldItem(ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM);
                })
        );

        // Cannot recycle items with Corrosive Gas
        // TODO Fix this
        corrosiveGasTest(
                false, true, ItemNamesies.POTION, ItemNamesies.ORAN_BERRY,
                new TestInfo().with((battle, attacking, defending) -> {
                    defending.assertHoldingItem(ItemNamesies.ORAN_BERRY);

                    // Consume the Oran Berry
                    battle.falseSwipePalooza(true);
                    defending.assertConsumedBerry();
                    defending.assertHp(1);

                    // Then Recycle it
                    battle.defendingFight(AttackNamesies.RECYCLE);
                    defending.assertLastMoveSucceeded(true);
                    defending.assertHoldingItem(ItemNamesies.ORAN_BERRY);
                })
                              .after((battle, attacking, defending) -> {
                                  defending.assertActualHeldItem(ItemNamesies.ORAN_BERRY, ItemNamesies.NO_ITEM);

                                  // Recycle should fail now when items are blocked
                                  battle.defendingFight(AttackNamesies.RECYCLE);
                                  attacking.assertLastMoveSucceeded(false);
                                  defending.assertActualHeldItem(ItemNamesies.ORAN_BERRY, ItemNamesies.NO_ITEM);
                              })
        );

        // Similar recycle test as above, but double take test seems more appropriate here
        new TestInfo().setup((battle, attacking, defending) -> {
            defending.withItem(ItemNamesies.ORAN_BERRY);

            // Consume the Oran Berry
            battle.falseSwipePalooza(true);
            defending.assertConsumedBerry();
            defending.assertHp(1);
        })
                      .defendingFight(AttackNamesies.RECYCLE)
                      .doubleTake(
                              (battle, attacking, defending) -> battle.attackingFight(AttackNamesies.CORROSIVE_GAS),
                              (battle, attacking, defending) -> {
                                  defending.assertLastMoveSucceeded(true);
                                  defending.assertActualHeldItem(ItemNamesies.ORAN_BERRY);
                              },
                              (battle, attacking, defending) -> {
                                  defending.assertLastMoveSucceeded(false);
                                  defending.assertActualHeldItem(ItemNamesies.NO_ITEM);
                              }
                      );

        // TODO: should have a changedItemEnemyTypeTest and make sure behavior is the same for wild and trainer
    }

    // By default, nothing happens to attacking Pokemon, and the defending Pokemon is given a Water Stone
    // If success, the defending Pokemon should be holding and unusable Water Stone
    private void corrosiveGasTest(boolean success, TestInfo testInfo) {
        corrosiveGasTest(false, success, ItemNamesies.NO_ITEM, ItemNamesies.WATER_STONE, testInfo);
    }

    // Sets up the test info to include giving the items passed in
    // Checks after corrosive gas is used that the appropriate items are melted
    private void corrosiveGasTest(boolean attackingMelted, boolean defendingMelted,
                                  ItemNamesies attackingActual, ItemNamesies defendingActual,
                                  TestInfo testInfo) {
        boolean success = attackingMelted || defendingMelted;
        ItemNamesies attackingItem = attackingMelted ? ItemNamesies.NO_ITEM : attackingActual;
        ItemNamesies defendingItem = defendingMelted ? ItemNamesies.NO_ITEM : defendingActual;

        testInfo.setup((battle, attacking, defending) -> {
            attacking.withItem(attackingActual);
            defending.withItem(defendingActual);
        });

        testInfo.with((battle, attacking, defending) -> {
            attacking.assertActualHeldItem(attackingActual);
            defending.assertActualHeldItem(defendingActual);
        });

        testInfo.preAfter((battle, attacking, defending) -> {
            attacking.assertLastMoveSucceeded(success);
            battle.assertEffect(success, StandardBattleEffectNamesies.CORROSIVE_GAS);

            attacking.assertNoEffect(PokemonEffectNamesies.CHANGE_ITEM);
            defending.assertNoEffect(PokemonEffectNamesies.CHANGE_ITEM);

            attacking.assertActualHeldItem(attackingActual, attackingItem);
            defending.assertActualHeldItem(defendingActual, defendingItem);
        });

        changedItemTrainerTest(AttackNamesies.CORROSIVE_GAS, AttackNamesies.SPLASH, attackingItem, defendingItem, testInfo);
    }

    @Test
    public void pickpocketTest() {
        // Pickpocket only triggers on contact
        pickpocketBasicTest(true, AttackNamesies.TACKLE, new TestInfo());
        pickpocketBasicTest(false, AttackNamesies.SWIFT, new TestInfo());
        pickpocketBasicTest(false, AttackNamesies.TACKLE, new TestInfo().attacking(AbilityNamesies.LONG_REACH));

        // Can't steal if the attacker has Sticky Hold
        pickpocketBasicTest(false, AttackNamesies.TACKLE, new TestInfo().attacking(AbilityNamesies.STICKY_HOLD));

        // Cannot steal when already holding an item
        pickpocketTest(
                false, AttackNamesies.TACKLE, ItemNamesies.POTION, ItemNamesies.WATER_STONE,
                new TestInfo().attacking(ItemNamesies.POTION).defending(ItemNamesies.WATER_STONE)
        );

        // Magician and Thief will activate first and then Pickpocket just steals the item back
        // Note that Pickpocket is successful here -- changedItem effect is applied even
        // though holding its original item
        pickpocketTest(
                true, AttackNamesies.TACKLE, ItemNamesies.NO_ITEM, ItemNamesies.WATER_STONE,
                new TestInfo().attacking(AbilityNamesies.MAGICIAN)
                              .defending(ItemNamesies.WATER_STONE)
        );
        pickpocketTest(
                true, AttackNamesies.THIEF, ItemNamesies.NO_ITEM, ItemNamesies.WATER_STONE,
                new TestInfo().defending(ItemNamesies.WATER_STONE)
        );

        // Pickpocket should not steal when the attack knocks out the user
        pickpocketTest(
                false, AttackNamesies.TACKLE, AttackNamesies.SPLASH,
                ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().falseSwipePalooza(true)
                              .attacking(ItemNamesies.POTION)
                              .after((battle, attacking, defending) -> defending.assertDead())
        );
    }

    // Gives the attacking Pokemon a Potion, and checks if the defending steals it when true
    private void pickpocketBasicTest(boolean stolen, AttackNamesies attackingAttack, TestInfo testInfo) {
        testInfo.setup((battle, attacking, defending) -> attacking.withItem(ItemNamesies.POTION));
        testInfo.with((battle, attacking, defending) -> {
            attacking.assertHoldingItem(ItemNamesies.POTION);
            defending.assertNotHoldingItem();
        });

        ItemNamesies attackingItem = stolen ? ItemNamesies.NO_ITEM : ItemNamesies.POTION;
        ItemNamesies defendingItem = stolen ? ItemNamesies.POTION : ItemNamesies.NO_ITEM;
        pickpocketTest(stolen, attackingAttack, attackingItem, defendingItem, testInfo);
    }

    private void pickpocketTest(boolean stolen, AttackNamesies attackingAttack, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        pickpocketTest(stolen, attackingAttack, AttackNamesies.ENDURE, attackingItem, defendingItem, testInfo);
    }

    // Stealing changes both Pokemon's items
    private void pickpocketTest(boolean stolen, AttackNamesies attackingAttack, AttackNamesies defendingAttack, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        pickpocketTest(stolen, stolen, attackingAttack, defendingAttack, attackingItem, defendingItem, testInfo);
    }

    // Defending Pokemon has Pickpocket
    // Checks each Pokemon's item to see if it was changed after the attack
    private void pickpocketTest(boolean attackingChanged, boolean defendingChanged,
                                AttackNamesies attackingAttack, AttackNamesies defendingAttack,
                                ItemNamesies attackingItem, ItemNamesies defendingItem,
                                TestInfo testInfo) {
        testInfo.setup((battle, attacking, defending) -> defending.withAbility(AbilityNamesies.PICKPOCKET));
        testInfo.with((battle, attacking, defending) -> defending.assertAbility(AbilityNamesies.PICKPOCKET));

        testInfo.preAfter((battle, attacking, defending) -> {
            attacking.assertLastMoveSucceeded(true);
            defending.assertNotFullHealth();
            attacking.assertEffect(attackingChanged, PokemonEffectNamesies.CHANGE_ITEM);
            defending.assertEffect(defendingChanged, PokemonEffectNamesies.CHANGE_ITEM);
        });

        changedItemTrainerTest(attackingAttack, defendingAttack, attackingItem, defendingItem, testInfo);
    }

    @Test
    public void magicianTest() {
        // Magician triggers when applying damage, regardless if makes contact
        magicianBasicTest(true, AttackNamesies.TACKLE, new TestInfo());
        magicianBasicTest(true, AttackNamesies.SWIFT, new TestInfo());

        // Cannot steal from a Pokemon with Sticky Hold
        magicianBasicTest(false, AttackNamesies.TACKLE, new TestInfo().defending(AbilityNamesies.STICKY_HOLD));

        // Flinging a Potion does not steal the item since the Potion removal is too late
        magicianBasicTest(false, AttackNamesies.FLING, new TestInfo().attacking(ItemNamesies.POTION));

        // Cannot steal if user is already holding an item
        magicianTest(
                false, AttackNamesies.TACKLE, ItemNamesies.POTION, ItemNamesies.WATER_STONE,
                new TestInfo().attacking(ItemNamesies.POTION).defending(ItemNamesies.WATER_STONE)
        );

        // Knock Off removes the target's item before Magician can steal it
        magicianTest(
                false, true,
                AttackNamesies.KNOCK_OFF,
                ItemNamesies.NO_ITEM, ItemNamesies.NO_ITEM,
                new TestInfo().defending(ItemNamesies.WATER_STONE)
        );

        // Pluck should eat the victim's berry before Magician can steal it
        magicianTest(
                false,
                AttackNamesies.PLUCK,
                ItemNamesies.NO_ITEM, ItemNamesies.NO_ITEM,
                new TestInfo().defending(ItemNamesies.LANSAT_BERRY)
                              .after((battle, attacking, defending) -> {
                                  attacking.assertHasEffect(PokemonEffectNamesies.EATEN_BERRY);
                                  attacking.assertHasEffect(PokemonEffectNamesies.RAISE_CRITS);
                                  defending.assertConsumedItem();
                              })
        );

        // Power Herb and gem items will be consumed before dealing damage, allowing Magician to activate
        magicianTest(
                true, AttackNamesies.SOLAR_BEAM,
                ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POWER_HERB)
                              .defending(ItemNamesies.WATER_STONE)
                              .after((battle, attacking, defending) -> attacking.assertHasEffect(PokemonEffectNamesies.CONSUMED_ITEM))
        );
        magicianTest(
                true, AttackNamesies.EMBER,
                ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.FIRE_GEM)
                              .defending(ItemNamesies.WATER_STONE)
                              .with((battle, attacking, defending) -> attacking.setExpectedDamageModifier(1.5))
                              .after((battle, attacking, defending) -> attacking.assertHasEffect(PokemonEffectNamesies.CONSUMED_ITEM))
        );
    }

    // Gives the defending Pokemon a Water Stone, and checks if the user has the Water Stone when success is true
    private void magicianBasicTest(boolean stolen, AttackNamesies attackingAttack, TestInfo testInfo) {
        testInfo.setup((battle, attacking, defending) -> defending.withItem(ItemNamesies.WATER_STONE));
        testInfo.with((battle, attacking, defending) -> defending.assertHoldingItem(ItemNamesies.WATER_STONE));

        ItemNamesies attackingItem = stolen ? ItemNamesies.WATER_STONE : ItemNamesies.NO_ITEM;
        ItemNamesies defendingItem = stolen ? ItemNamesies.NO_ITEM : ItemNamesies.WATER_STONE;
        magicianTest(stolen, attackingAttack, attackingItem, defendingItem, testInfo);
    }

    private void magicianTest(boolean stolen, AttackNamesies attackingAttack, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        magicianTest(stolen, stolen, attackingAttack, attackingItem, defendingItem, testInfo);
    }

    private void magicianTest(boolean attackingChanged, boolean defendingChanged,
                              AttackNamesies attackingAttack,
                              ItemNamesies attackingItem, ItemNamesies defendingItem,
                              TestInfo testInfo) {
        testInfo.setup((battle, attacking, defending) -> attacking.withAbility(AbilityNamesies.MAGICIAN));
        testInfo.with((battle, attacking, defending) -> attacking.assertAbility(AbilityNamesies.MAGICIAN));

        testInfo.preAfter((battle, attacking, defending) -> {
            attacking.assertLastMoveSucceeded(true);
            defending.assertNotFullHealth();
            attacking.assertEffect(attackingChanged, PokemonEffectNamesies.CHANGE_ITEM);
            defending.assertEffect(defendingChanged, PokemonEffectNamesies.CHANGE_ITEM);
        });

        changedItemTrainerTest(attackingAttack, AttackNamesies.ENDURE, attackingItem, defendingItem, testInfo);
    }

    @Test
    public void thiefTest() {
        // Thief should steal the opponent's Water Stone
        thiefTest(
                true, ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM,
                new TestInfo().defending(ItemNamesies.WATER_STONE)
        );

        // Can't steal item if user is holding an item already
        thiefTest(
                false, ItemNamesies.POTION, ItemNamesies.WATER_STONE,
                new TestInfo().attacking(ItemNamesies.POTION).defending(ItemNamesies.WATER_STONE)
        );

        // Cannot steal if the victim has Sticky Hold
        thiefTest(
                false, ItemNamesies.NO_ITEM, ItemNamesies.WATER_STONE,
                new TestInfo().defending(ItemNamesies.WATER_STONE).defending(AbilityNamesies.STICKY_HOLD)
        );

        // Can steal from Sticky Hold if Thief causes a kill
        thiefTest(
                true, AttackNamesies.SPLASH, ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM,
                new TestInfo().defending(ItemNamesies.WATER_STONE)
                              .defending(AbilityNamesies.STICKY_HOLD)
                              .falseSwipePalooza(true)
        );

        // Thief cannot steal from a substitute even if it breaks the substitute
        thiefTest(
                false, ItemNamesies.NO_ITEM, ItemNamesies.WATER_STONE,
                new TestInfo(PokemonNamesies.TYRANITAR, PokemonNamesies.ABRA)
                        .defending(ItemNamesies.WATER_STONE)
                        .defendingFight(AttackNamesies.SUBSTITUTE)
                        .after((battle, attacking, defending) -> {
                            defending.assertNoEffect(PokemonEffectNamesies.SUBSTITUTE);
                            defending.assertHealthRatio(.75);
                        })
        );

        // Colbur Berry will activate before it can be stolen if Thief is super-effective
        thiefTest(
                false, ItemNamesies.NO_ITEM, ItemNamesies.NO_ITEM,
                new TestInfo(PokemonNamesies.THIEVUL, PokemonNamesies.CHIMECHO)
                        .defending(ItemNamesies.COLBUR_BERRY)
                        .with((battle, attacking, defending) -> attacking.setExpectedDamageModifier(.5))
                        .after((battle, attacking, defending) -> defending.assertConsumedBerry())
        );

        // Enemy type is irrelevant since only enemy is holding item
        swapItemsEnemyTypeTest(true, false, false, AttackNamesies.THIEF, ItemNamesies.NO_ITEM, ItemNamesies.WATER_STONE);

        // Wild Pokemon cannot steal items from the trainer, but trainer's can temporarily steal
        swapItemsEnemyTypeTest(false, false, true, AttackNamesies.THIEF, ItemNamesies.POTION, ItemNamesies.NO_ITEM);
    }

    private void thiefTest(boolean stolen, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        thiefTest(stolen, AttackNamesies.ENDURE, attackingItem, defendingItem, testInfo);
    }

    private void thiefTest(boolean stolen, AttackNamesies defendingAttack, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        testInfo.preAfter((battle, attacking, defending) -> {
            attacking.assertLastMoveSucceeded(true);
            defending.assertNotFullHealth();
            attacking.assertEffect(stolen, PokemonEffectNamesies.CHANGE_ITEM);
            defending.assertEffect(stolen, PokemonEffectNamesies.CHANGE_ITEM);
        });

        changedItemTrainerTest(AttackNamesies.THIEF, defendingAttack, attackingItem, defendingItem, testInfo);
    }

    @Test
    public void knockOffTest() {
        // Knock Off should remove the opponent's Water Stone
        knockOffTest(
                true, ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION).defending(ItemNamesies.WATER_STONE)
        );

        // But not if they have Sticky Hold -- still receives damage boost even though it can't knock off though
        knockOffTest(
                false, 1.5, ItemNamesies.POTION, ItemNamesies.WATER_STONE,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(ItemNamesies.WATER_STONE)
                              .defending(AbilityNamesies.STICKY_HOLD)
        );

        // Unless the user also has Mold Breaker
        knockOffTest(
                true, ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(ItemNamesies.WATER_STONE)
                              .attacking(AbilityNamesies.MOLD_BREAKER)
                              .defending(AbilityNamesies.STICKY_HOLD)
        );

        // Nothing to knock off -- item is not changed but attack should still succeed
        knockOffTest(false, ItemNamesies.NO_ITEM, ItemNamesies.NO_ITEM, new TestInfo());
        knockOffTest(
                false, ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
        );

        // If Magic Room is in effect, neither Pokemon should be holding and item shouldn't be consumed either
        knockOffTest(
                false, ItemNamesies.NO_ITEM, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(ItemNamesies.WATER_STONE)
                              .attackingFight(AttackNamesies.MAGIC_ROOM)
                              .after((battle, attacking, defending) -> {
                                  battle.assertHasEffect(StandardBattleEffectNamesies.MAGIC_ROOM);

                                  battle.attackingFight(AttackNamesies.MAGIC_ROOM);
                                  battle.assertNoEffect(StandardBattleEffectNamesies.MAGIC_ROOM);

                                  attacking.assertHoldingItem(ItemNamesies.POTION);
                                  defending.assertHoldingItem(ItemNamesies.WATER_STONE);

                                  attacking.setExpectedDamageModifier(1.5);
                                  battle.fight(AttackNamesies.KNOCK_OFF, AttackNamesies.ENDURE);
                                  attacking.assertHoldingItem(ItemNamesies.POTION);
                                  defending.assertHoldingItem(ItemNamesies.NO_ITEM);

                                  defending.assertHasEffect(PokemonEffectNamesies.CHANGE_ITEM);
                              })
        );

        // Cannot knock off if item is unusable from embargo/corrosive gas
        knockOffTest(
                false, ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(ItemNamesies.WATER_STONE)
                              .attackingFight(AttackNamesies.EMBARGO)
        );
        knockOffTest(
                false, ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(ItemNamesies.WATER_STONE)
                              .attackingFight(AttackNamesies.CORROSIVE_GAS)
        );

        // Can't knock an item off of a substitute (still receives boost though)
        knockOffTest(
                false, 1.5, ItemNamesies.POTION, ItemNamesies.WATER_STONE,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(ItemNamesies.WATER_STONE)
                              .defendingFight(AttackNamesies.SUBSTITUTE)
        );

        // User's item situation should not affect victim
        knockOffTest(
                true, ItemNamesies.NO_ITEM, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(ItemNamesies.WATER_STONE)
                              .defendingFight(AttackNamesies.EMBARGO)
        );

        // Colbur Berry activates before getting knocked off and will reduce the power by 50%
        // Knock Off stills retains it's 50% boost though
        // (Make Ghost-type with Trick-or-Treat so Dark-type Knock Off will be super-effective)
        knockOffTest(
                false, 1.5*.5, ItemNamesies.NO_ITEM, ItemNamesies.NO_ITEM,
                new TestInfo().defending(ItemNamesies.COLBUR_BERRY)
                              .attackingFight(AttackNamesies.TRICK_OR_TREAT)
                              .after((battle, attacking, defending) -> defending.assertConsumedBerry())
        );

        // If the user faints from something like Rough Skin, the item will still be knocked off
        knockOffTest(
                true, 1.5, AttackNamesies.SPLASH, ItemNamesies.NO_ITEM, ItemNamesies.NO_ITEM,
                new TestInfo(PokemonNamesies.BULBASAUR, PokemonNamesies.SHUCKLE)
                        .addAttacking(PokemonNamesies.SQUIRTLE)
                        .falseSwipePalooza(false)
                        .defending(ItemNamesies.WATER_STONE)
                        .defending(AbilityNamesies.ROUGH_SKIN)
                        .after((battle, attacking, defending) -> attacking.assertDead())
        );

        // Recycle cannot bring back an item that was knocked off
        knockOffTest(
                true, ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(ItemNamesies.WATER_STONE)
                              .after((battle, attacking, defending) -> {
                                  battle.defendingFight(AttackNamesies.RECYCLE);
                                  defending.assertLastMoveSucceeded(false);
                                  defending.assertNotHoldingItem();
                              })
        );

        // Can receive another item with Bestow after item is knocked off
        knockOffTest(
                true, ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(ItemNamesies.WATER_STONE)
                              .after((battle, attacking, defending) -> {
                                  battle.attackingFight(AttackNamesies.BESTOW);
                                  attacking.assertLastMoveSucceeded(true);
                                  attacking.assertNotHoldingItem();
                                  defending.assertHoldingItem(ItemNamesies.POTION);
                              })
        );
        knockOffTest(
                true, ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(ItemNamesies.WATER_STONE)
                              .after((battle, attacking, defending) -> {
                                  battle.defendingFight(AttackNamesies.TRICK);
                                  defending.assertLastMoveSucceeded(true);
                                  attacking.assertNotHoldingItem();
                                  defending.assertHoldingItem(ItemNamesies.POTION);
                              })
        );

        // Wild battles actually remove the item, and trainer battles just add an effect
        changedItemEnemyTypeTest(
                new TestInfo().with((battle, attacking, defending) -> {
                    attacking.withItem(ItemNamesies.POTION);
                    defending.withItem(ItemNamesies.WATER_STONE);

                    attacking.setExpectedDamageModifier(1.5);
                    battle.fight(AttackNamesies.KNOCK_OFF, AttackNamesies.ENDURE);
                    attacking.assertActualHeldItem(ItemNamesies.POTION);
                    defending.assertNotHoldingItem();
                }),
                (battle, attacking, defending) -> defending.assertActualHeldItem(ItemNamesies.NO_ITEM),
                (battle, attacking, defending) -> defending.assertActualHeldItem(ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM)
        );

        // Wild Pokemon cannot remove items from the player
        changedItemEnemyTypeTest(
                new TestInfo().with((battle, attacking, defending) -> {
                    attacking.withItem(ItemNamesies.POTION);
                    defending.withItem(ItemNamesies.WATER_STONE);

                    defending.setExpectedDamageModifier(1.5);
                    battle.fight(AttackNamesies.ENDURE, AttackNamesies.KNOCK_OFF);
                    defending.assertActualHeldItem(ItemNamesies.WATER_STONE);
                }),
                (battle, attacking, defending) -> attacking.assertActualHeldItem(ItemNamesies.POTION),
                (battle, attacking, defending) -> attacking.assertActualHeldItem(ItemNamesies.POTION, ItemNamesies.NO_ITEM)
        );
    }

    private void knockOffTest(boolean changedItem, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        knockOffTest(changedItem, changedItem ? 1.5 : 1, attackingItem, defendingItem, testInfo);
    }

    private void knockOffTest(boolean changedItem, double boost, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        knockOffTest(changedItem, boost, AttackNamesies.ENDURE, attackingItem, defendingItem, testInfo);
    }

    private void knockOffTest(boolean changedItem, double boost, AttackNamesies defendingAttack, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        testInfo.with((battle, attacking, defending) -> attacking.setExpectedDamageModifier(boost))
                .preAfter((battle, attacking, defending) -> {
                    attacking.assertLastMoveSucceeded(true);
                    defending.assertNotFullHealth();
                    defending.assertEffect(changedItem, PokemonEffectNamesies.CHANGE_ITEM);
                });

        changedItemTrainerTest(AttackNamesies.KNOCK_OFF, defendingAttack, attackingItem, defendingItem, testInfo);
    }

    @Test
    public void bestowTest() {
        // Bestow gifts the user's item to the defending Pokemon
        bestowTest(
                true, ItemNamesies.NO_ITEM, ItemNamesies.POTION,
                new TestInfo().attacking(ItemNamesies.POTION)
        );

        // Bestow should fail if defending pokemon is holding an item or the user is not holding an item
        bestowTest(
                false, ItemNamesies.POTION, ItemNamesies.WATER_STONE,
                new TestInfo().attacking(ItemNamesies.POTION).defending(ItemNamesies.WATER_STONE)
        );
        bestowTest(
                false, ItemNamesies.NO_ITEM, ItemNamesies.WATER_STONE,
                new TestInfo().defending(ItemNamesies.WATER_STONE)
        );
        bestowTest(false, ItemNamesies.NO_ITEM, ItemNamesies.NO_ITEM, new TestInfo());

        // Bestow is blocked by protect
        bestowTest(
                false, AttackNamesies.PROTECT,
                ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
        );

        // Bestow should fail against a substitute
        bestowTest(
                false, ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defendingFight(AttackNamesies.SUBSTITUTE)
        );

        // Wild battles actually gift item, and trainer battles just add an effect
        // This means if you use Bestow on a wild Pokemon you will NOT get your item back after battle!!!
        changedItemEnemyTypeTest(
                new TestInfo().with((battle, attacking, defending) -> {
                    attacking.withItem(ItemNamesies.POTION);

                    battle.attackingFight(AttackNamesies.BESTOW);
                    attacking.assertNotHoldingItem();
                    defending.assertHoldingItem(ItemNamesies.POTION);
                }),
                (battle, attacking, defending) -> {
                    attacking.assertActualHeldItem(ItemNamesies.NO_ITEM);
                    defending.assertActualHeldItem(ItemNamesies.POTION);
                },
                (battle, attacking, defending) -> {
                    attacking.assertActualHeldItem(ItemNamesies.POTION, ItemNamesies.NO_ITEM);
                    defending.assertActualHeldItem(ItemNamesies.NO_ITEM, ItemNamesies.POTION);
                }
        );

        // Wild Pokemon can gift their item permanently to the player
        changedItemEnemyTypeTest(
                new TestInfo().with((battle, attacking, defending) -> {
                    defending.withItem(ItemNamesies.WATER_STONE);
                    battle.defendingFight(AttackNamesies.BESTOW);
                    attacking.assertHoldingItem(ItemNamesies.WATER_STONE);
                    defending.assertNotHoldingItem();
                }),
                (battle, attacking, defending) -> {
                    attacking.assertActualHeldItem(ItemNamesies.WATER_STONE);
                    defending.assertActualHeldItem(ItemNamesies.NO_ITEM);
                },
                (battle, attacking, defending) -> {
                    attacking.assertActualHeldItem(ItemNamesies.NO_ITEM, ItemNamesies.WATER_STONE);
                    defending.assertActualHeldItem(ItemNamesies.WATER_STONE, ItemNamesies.NO_ITEM);
                }
        );

        // Enemy type is irrelevant because not holding an item -- only player is successful
        swapItemsEnemyTypeTest(true, false, false, AttackNamesies.BESTOW, ItemNamesies.POTION, ItemNamesies.NO_ITEM);

        // Wild Pokemon are capable of permanently gifting an item to the player
        swapItemsEnemyTypeTest(false, true, true, AttackNamesies.BESTOW, ItemNamesies.NO_ITEM, ItemNamesies.WATER_STONE);
    }

    private void bestowTest(boolean success, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        bestowTest(success, AttackNamesies.SPLASH, attackingItem, defendingItem, testInfo);
    }

    private void bestowTest(boolean success, AttackNamesies defendingAttack, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        if (success) {
            Assert.assertEquals(attackingItem, ItemNamesies.NO_ITEM);
            Assert.assertNotEquals(defendingItem, ItemNamesies.NO_ITEM);
        }

        changeBothItemsTest(success, AttackNamesies.BESTOW, defendingAttack, attackingItem, defendingItem, testInfo);
    }

    @Test
    public void switcherooTest() {
        // Switcheroo/Trick swaps the user's item with the victim
        switcherooBasicTest(true, new TestInfo());

        // Opponent's Sticky Hold will cause Switcheroo to fail
        switcherooBasicTest(false, new TestInfo().defending(AbilityNamesies.STICKY_HOLD));

        // Unless the user also has Mold Breaker
        switcherooBasicTest(
                true, new TestInfo().attacking(AbilityNamesies.MOLD_BREAKER)
                                    .defending(AbilityNamesies.STICKY_HOLD)
        );

        // User having Sticky Hold should still allow the items to swap
        switcherooBasicTest(
                true, new TestInfo().attacking(AbilityNamesies.STICKY_HOLD)
        );

        // Enemy Sticky Hold will still prevent swap when they're not holding an item
        switcherooTest(
                false, ItemNamesies.POTION, ItemNamesies.NO_ITEM,
                new TestInfo().attacking(ItemNamesies.POTION)
                              .defending(AbilityNamesies.STICKY_HOLD)
        );

        // Can't trick a substitute!!
        switcherooBasicTest(false, new TestInfo().defendingFight(AttackNamesies.SUBSTITUTE));

        // Protect blocks Switcheroo
        switcherooTest(
                false, AttackNamesies.PROTECT, ItemNamesies.POTION, ItemNamesies.WATER_STONE,
                new TestInfo().attacking(ItemNamesies.POTION).defending(ItemNamesies.WATER_STONE)
        );

        switcherooEnemyTypeTest(true, ItemNamesies.POTION, ItemNamesies.WATER_STONE);
        switcherooEnemyTypeTest(true, ItemNamesies.POTION, ItemNamesies.NO_ITEM);
        switcherooEnemyTypeTest(true, ItemNamesies.NO_ITEM, ItemNamesies.WATER_STONE);
        switcherooEnemyTypeTest(false, ItemNamesies.NO_ITEM, ItemNamesies.NO_ITEM);
    }

    // Switcheroo always fails when used by a wild pokemon
    // When used by an enemy trainer, behavior should be the same as the player
    // Success is the same when used on a wild Pokemon as a trainer (though change is permanent for wild)
    private void switcherooEnemyTypeTest(boolean success, ItemNamesies attackingBefore, ItemNamesies defendingBefore) {
        swapItemsEnemyTypeTest(success, false, success, AttackNamesies.SWITCHEROO, attackingBefore, defendingBefore);
        swapItemsEnemyTypeTest(success, false, success, AttackNamesies.TRICK, attackingBefore, defendingBefore);
    }

    // Give attacking Potion, give defending Water Stone -- if success, items will swap
    private void switcherooBasicTest(boolean success, TestInfo testInfo) {
        testInfo.setup((battle, attacking, defending) -> {
            attacking.withItem(ItemNamesies.POTION);
            defending.withItem(ItemNamesies.WATER_STONE);
        });
        testInfo.with((battle, attacking, defending) -> {
            attacking.assertHoldingItem(ItemNamesies.POTION);
            defending.assertHoldingItem(ItemNamesies.WATER_STONE);
        });

        ItemNamesies attackingItem = success ? ItemNamesies.WATER_STONE : ItemNamesies.POTION;
        ItemNamesies defendingItem = success ? ItemNamesies.POTION : ItemNamesies.WATER_STONE;
        switcherooTest(success, attackingItem, defendingItem, testInfo);
    }

    private void switcherooTest(boolean success, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        switcherooTest(success, AttackNamesies.SPLASH, attackingItem, defendingItem, testInfo);
    }

    private void switcherooTest(boolean success, AttackNamesies defendingAttack, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        changeBothItemsTest(success, AttackNamesies.SWITCHEROO, defendingAttack, attackingItem, defendingItem, testInfo);
    }

    // If the move is successful, both the attacking and defending Pokemon should have the changed item effect
    private void changeBothItemsTest(boolean success, AttackNamesies itemAttack, AttackNamesies defendingAttack, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        testInfo.preAfter((battle, attacking, defending) -> {
            attacking.assertLastMoveSucceeded(success);
            attacking.assertEffect(success, PokemonEffectNamesies.CHANGE_ITEM);
            defending.assertEffect(success, PokemonEffectNamesies.CHANGE_ITEM);
        });

        changedItemTrainerTest(itemAttack, defendingAttack, attackingItem, defendingItem, testInfo);
    }

    // Fairly generic item test method that can work for using items, blocking items, swapping items, etc
    // Just basically performs the test info and checks the input hold items
    // Note: This method treats all tests as trainer battles
    private void changedItemTrainerTest(AttackNamesies itemAttack, AttackNamesies defendingAttack, ItemNamesies attackingItem, ItemNamesies defendingItem, TestInfo testInfo) {
        TestBattle battle = testInfo.asTrainerBattle().createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        testInfo.manipulate(battle);
        battle.fight(itemAttack, defendingAttack);

        attacking.assertHoldingItem(attackingItem);
        defending.assertHoldingItem(defendingItem);

        testInfo.performAfterCheck(battle);
    }

    private void swapItemsEnemyTypeTest(boolean playerSuccess, boolean wildSuccess, boolean trainerSuccess,
                                        AttackNamesies swapItems,
                                        ItemNamesies attackingBefore, ItemNamesies defendingBefore) {
        // Can never be successful when used by a wild Pokemon but not when used by a trainer
        Assert.assertFalse(wildSuccess && !trainerSuccess);

        // Player success is the same when used against wild pokemon and trainers (item permanence may vary)
        swapItemsEnemyTypeTest(playerSuccess, playerSuccess, swapItems, AttackNamesies.SPLASH, attackingBefore, defendingBefore);

        // Use the move against the player -- success may vary depending on if the user is wild or trained
        swapItemsEnemyTypeTest(wildSuccess, trainerSuccess, AttackNamesies.SPLASH, swapItems, attackingBefore, defendingBefore);
    }

    // Expected that one move is a move that swaps items and other move is mostly irrelevant
    // wild/trainerSuccess represent if the swap is successful in these types of battles,
    // REGARDLESS of if the move is used by the player or the enemy
    private void swapItemsEnemyTypeTest(boolean wildSuccess, boolean trainerSuccess,
                                        AttackNamesies attackingAttack, AttackNamesies defendingAttack,
                                        ItemNamesies attackingBefore, ItemNamesies defendingBefore) {
        changedItemEnemyTypeTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .with((battle, attacking, defending) -> {
                            attacking.withItem(attackingBefore);
                            defending.withItem(defendingBefore);

                            battle.fight(attackingAttack, defendingAttack);

                            // wildSuccess can only be true if trainerSuccess is also true
                            // Note that here is just checking the battle item and not the actual items
                            // which are checked later
                            if (wildSuccess) {
                                attacking.assertHoldingItem(defendingBefore);
                                defending.assertHoldingItem(attackingBefore);
                            }
                        }),
                // In wild battles, items are actually swapped
                (battle, attacking, defending) -> {
                    attacking.assertActualHeldItem(wildSuccess ? defendingBefore : attackingBefore);
                    defending.assertActualHeldItem(wildSuccess ? attackingBefore : defendingBefore);
                },
                // In trainer battles, items just appear to be swapped, but is only a temporary effect
                (battle, attacking, defending) -> {
                    attacking.assertActualHeldItem(attackingBefore, trainerSuccess ? defendingBefore : attackingBefore);
                    defending.assertActualHeldItem(defendingBefore, trainerSuccess ? attackingBefore : defendingBefore);
                }
        );
    }

    private void changedItemEnemyTypeTest(TestInfo testInfo, PokemonManipulator asWild, PokemonManipulator asTrainer) {
        testInfo.enemyTypeDoubleTake(asWild, asTrainer);
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

        // Make sure things like Air Lock don't prevent weather from changing
        weatherExtenderTest(
                WeatherNamesies.RAINING, ItemNamesies.DAMP_ROCK,
                new TestInfo().attacking(AbilityNamesies.AIR_LOCK).attackingFight(AttackNamesies.RAIN_DANCE)
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
                new TestInfo().defending(PokemonNamesies.EEVEE),
                (battle, attacking, defending) -> defending.assertNotFullHealth(),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Failing via type advantage does not trigger Blunder Policy
        // Attack fails because False Swipe does not affect Gastly
        blunderPolicyTest(
                new TestInfo().defending(PokemonNamesies.GASTLY),
                (battle, attacking, defending) -> defending.assertFullHealth()
        );

        // Failing via semi-invulnerable does not trigger Blunder Policy
        blunderPolicyTest(
                new TestInfo(PokemonNamesies.SHUCKLE, PokemonNamesies.SHUCKLE)
                        .with((battle, attacking, defending) -> {
                            // Defending with fly up in the air and next attack will be a forced miss
                            battle.defendingFight(AttackNamesies.FLY);
                            Assert.assertTrue(defending.isSemiInvulnerableFlying());
                            attacking.setExpectedAccuracyBypass(false);
                        }),
                (battle, attacking, defending) -> {
                    // First False Swipe misses (does not trigger), then Fly finishes executing and hits attacking
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
        ejectPackTest(
                true, new TestStages().set(-1, Stat.ATTACK),
                new TestInfo().attackingFight(AttackNamesies.GROWL)
        );

        // Make sure works with multiple stat reductions -- Tickle lowers Attack and Defense
        // Will eject after only lowering Attack -- Defense lower will be skipped entirely
        ejectPackTest(
                true, new TestStages().set(-1, Stat.ATTACK),
                new TestInfo().attackingFight(AttackNamesies.TICKLE)
        );

        // Ejects even for self-inflicted stat lowers
        ejectPackTest(
                true, new TestStages().set(-2, Stat.SP_ATTACK),
                new TestInfo().fight(AttackNamesies.ENDURE, AttackNamesies.LEAF_STORM)
        );

        // Suction Cups will not prevent ejection because its the holder
        ejectPackTest(
                true, new TestStages().set(-1, Stat.ATTACK),
                new TestInfo().defending(AbilityNamesies.SUCTION_CUPS).attackingFight(AttackNamesies.GROWL)
        );

        // Eject Pack will not trigger if stats are reflected with Mirror Armor or Magic Bounce
        // Eject Pack will trigger if stats are reflected back onto the holder with Mirror Armor or Magic Bounce
        ejectPackReflectionTest(AbilityNamesies.MIRROR_ARMOR);
        ejectPackReflectionTest(AbilityNamesies.MAGIC_BOUNCE);

        // Nothing happens when raising the ejecter's stats with Contrary
        ejectPackTest(
                false, new TestStages().set(1, Stat.ATTACK),
                new TestInfo().defending(AbilityNamesies.CONTRARY).attackingFight(AttackNamesies.GROWL)
        );

        // Will still eject if lowered stat is still positive
        ejectPackTest(
                true, new TestStages().set(1, Stat.ATTACK),
                new TestInfo().defendingFight(AttackNamesies.SWORDS_DANCE)
                              .defending(new TestStages().set(2, Stat.ATTACK))
                              .attackingFight(AttackNamesies.GROWL)
        );

        // So in game Parting Shot will not activate Eject Pack, but I don't really see why not so they can both swap
        ejectPackTest(
                true, true, new TestStages().set(-1, Stat.ATTACK),
                new TestInfo().attackingFight(AttackNamesies.PARTING_SHOT)
        );

        // Eject Pack does not trigger for taking damage -- only stat decreases
        ejectPackTest(
                false, new TestStages(),
                new TestInfo().attackingFight(AttackNamesies.FALSE_SWIPE)
                              .with((battle, attacking, defending) -> defending.assertNotFullHealth())
        );

        // Eject Button causes the holder to switch out when hit by an attack
        // Red Card causes the attacker to switch out when the holder is hit by an attack
        ejectButtonRedCardTest(true, new TestInfo().attackingFight(AttackNamesies.TACKLE));
        ejectButtonRedCardTest(true, new TestInfo().attackingFight(AttackNamesies.SWIFT));
        ejectButtonRedCardTest(true, new TestInfo().fight(AttackNamesies.EXPLOSION, AttackNamesies.ENDURE));

        // Eject Button/Red Card does not trigger for stat decreases
        ejectButtonRedCardTest(false, new TestInfo().attackingFight(AttackNamesies.GROWL)
                                                    .defending(new TestStages().set(-1, Stat.ATTACK)));

        // Eject Button/Red Card should not trigger when damage is absorbed
        // Technically this is also testing not swapping for indirect damage like substitute quartering
        ejectButtonRedCardTest(false, new TestInfo().defendingFight(AttackNamesies.SUBSTITUTE)
                                                    .defendingFight(AttackNamesies.RECOVER)
                                                    .attackingFight(AttackNamesies.TACKLE));

        // Eject Button + defending Suctions Cups will not prevent ejection because its the holder
        // Red Card + defending Suction Cups will not prevent ejection because its the wrong Pokemon...
        ejectButtonRedCardTest(true, new TestInfo().defending(AbilityNamesies.SUCTION_CUPS)
                                                   .attackingFight(AttackNamesies.TACKLE));

        // Eject Button + attacking Suction Cups will not prevent ejection because wrong Pokemon
        // Red Card + attacking Suction Cups will prevent ejection and item will not be consumed
        ejectButtonRedCardTest(true, false, new TestInfo().attacking(AbilityNamesies.SUCTION_CUPS)
                                                          .attackingFight(AttackNamesies.TACKLE));

        // Red Card + Suction Cups should not be affected by Mold Breaker effects (since not defending ability)
        ejectButtonRedCardTest(true, false, new TestInfo().attacking(AbilityNamesies.SUCTION_CUPS)
                                                          .attackingFight(AttackNamesies.SUNSTEEL_STRIKE));

        // Magician steals the defending item when it attacks
        // User steals Eject Button/Red Card before it has a chance to activate
        ejectButtonRedCardTest(false, false, true, new TestInfo().attacking(AbilityNamesies.MAGICIAN)
                                                                 .attackingFight(AttackNamesies.TACKLE));

        // Thief steals the defending item when it attacks
        // User steals Eject Button/Red Card before it has a chance to activate
        ejectButtonRedCardTest(false, false, true, new TestInfo().attackingFight(AttackNamesies.THIEF));

        // U-Turn + Eject Button will swap both Pokemon
        // U-Turn + Red Card will swap attacking by Red Card before U-Turn
        ejectButtonTest(true, true, false, new TestInfo().attackingFight(AttackNamesies.U_TURN));
        redCardTest(true, false, false, new TestInfo().attackingFight(AttackNamesies.U_TURN));

        // Circle Throw + Eject Button will swap defending by Eject Button before Circle Throw
        // U-Turn + Red Card will swap both Pokemon
        ejectButtonTest(true, false, false, new TestInfo().attackingFight(AttackNamesies.CIRCLE_THROW));
        redCardTest(true, true, false, new TestInfo().attackingFight(AttackNamesies.CIRCLE_THROW));

        // TODO: Eject Button/Red Card should activate from fixed damage moves
//        ejectButtonRedCardTest(true, new TestInfo().attackingFight(AttackNamesies.SONIC_BOOM));
    }

    private void ejectPackReflectionTest(AbilityNamesies reflectionAbility) {
        // Do not trigger Eject Pack if stats get reflected
        ejectPackTest(
                false, new TestStages(),
                new TestInfo().defending(reflectionAbility)
                              .attackingFight(AttackNamesies.GROWL)
                              .attacking(new TestStages().set(-1, Stat.ATTACK))
        );

        // Should trigger Eject Pack if reflected back onto the holder though
        ejectPackTest(
                true, new TestStages().set(-1, Stat.ATTACK),
                new TestInfo().attacking(reflectionAbility)
                              .defendingFight(AttackNamesies.GROWL)
                              .attacking(new TestStages())
        );
    }

    private void ejectPackTest(boolean swapDefending, TestStages stages, TestInfo testInfo) {
        ejectPackTest(false, swapDefending, stages, testInfo);
    }

    private void ejectPackTest(boolean swapAttacking, boolean swapDefending, TestStages stages, TestInfo testInfo) {
        // Stages don't actually get reset until the Pokemon enters battle again, which is actually kind of
        // convenient here because we can see what they were even if they're not out front anymore
        testInfo.defending(stages);
        swapPokemonTest(swapAttacking, swapDefending, swapDefending, false, ItemNamesies.EJECT_PACK, testInfo);
    }

    private void ejectButtonRedCardTest(boolean swapPokemon, TestInfo testInfo) {
        ejectButtonRedCardTest(swapPokemon, swapPokemon, testInfo);
    }

    private void ejectButtonRedCardTest(boolean swapEjectButton, boolean swapRedCard, TestInfo testInfo) {
        ejectButtonRedCardTest(swapEjectButton, swapRedCard, false, testInfo);
    }

    private void ejectButtonRedCardTest(boolean swapEjectButton, boolean swapRedCard, boolean itemStolen, TestInfo testInfo) {
        // Cannot activate an item and steal it
        Assert.assertFalse(swapEjectButton && itemStolen);
        Assert.assertFalse(swapRedCard && itemStolen);

        // Mostly just because it changes the full health assumptions in the next line
        Assert.assertFalse(!swapEjectButton && swapRedCard);

        // Eject Button requires the target to be hit, so should be impossible to swap without taking damage
        // (I mean sure you could have a berry or something but that's not what I'm talking about...)
        // It is possible to take damage without swapping (such as any indirect damage but not testing that here)
        // Also only relevant to test stolen item when dealing direct damage
        boolean takenDamage = swapEjectButton || itemStolen;
        testInfo.with((battle, attacking, defending) -> defending.assertHasFullHealth(!takenDamage));

        // Eject Button and Red Card work similar, but Red Card swaps the attacking, instead of defending
        ejectButtonTest(swapEjectButton, false, itemStolen, testInfo);
        redCardTest(swapRedCard, false, itemStolen, testInfo);
    }

    private void ejectButtonTest(boolean swapEjectButton, boolean swapOther, boolean itemStolen, TestInfo testInfo) {
        swapPokemonTest(swapOther, swapEjectButton, swapEjectButton, itemStolen, ItemNamesies.EJECT_BUTTON, testInfo);
    }

    private void redCardTest(boolean swapRedCard, boolean swapOther, boolean itemStolen, TestInfo testInfo) {
        swapPokemonTest(swapRedCard, swapOther, swapRedCard, itemStolen, ItemNamesies.RED_CARD, testInfo);
    }

    private void swapPokemonTest(boolean swapAttacking, boolean swapDefending, boolean itemConsumed, boolean itemStolen, ItemNamesies swapItem, TestInfo testInfo) {
        TestBattle battle = testInfo.asTrainerBattle().createBattle();
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();
        TestPokemon attacking2 = battle.addAttacking(PokemonNamesies.SQUIRTLE);
        TestPokemon defending2 = battle.addDefending(PokemonNamesies.PIKACHU);

        defending.withItem(swapItem);

        battle.assertFront(attacking);
        battle.assertFront(defending);

        testInfo.manipulate(battle);

        battle.assertFront(swapAttacking ? attacking2 : attacking);
        battle.assertFront(swapDefending ? defending2 : defending);

        // These Pokemon should be fresh and healthy regardless of if they were switched to front
        attacking2.assertFullHealth();
        defending2.assertFullHealth();
        attacking2.assertStages(new TestStages());
        defending2.assertStages(new TestStages());

        // Can't consume your item and steal it too!
        Assert.assertFalse(itemConsumed && itemStolen);
        if (itemConsumed) {
            attacking.assertNotHoldingItem();
            defending.assertConsumedItem();
        } else if (itemStolen) {
            defending.assertNotHoldingItem();
            defending.assertNoEffect(PokemonEffectNamesies.CONSUMED_ITEM);
            attacking.assertHoldingItem(swapItem);
        } else {
            attacking.assertNotHoldingItem();
            defending.assertHoldingItem(swapItem);
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
                new TestInfo().attackingFight(AttackNamesies.SPIKES).attackingFight(AttackNamesies.SPIKES),
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
                    defending.assertNoStatus();
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
                    defending.assertNoStatus();
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
            Assert.assertNotEquals(defending, battle.getDefending());
        });

        testInfo.doubleTake(
                (battle, attacking, defending) -> battle.getOtherDefending().withItem(ItemNamesies.HEAVY_DUTY_BOOTS),
                withoutBoots, withBoots
        );
    }

    @Test
    public void throatSprayTest() {
        // Throat Spray increases Sp. Attack by 1 stage when using a sound-based move (like Growl)
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

    @Test
    public void utilityUmbrellaTest() {
        // Utility Umbrella will not give boost to Fire-moves or decrease to Water-moves in Sunny weather
        // Similarly no boosts/decreases in the rain
        utilityUmbrellaBoostTest(1.5, .5, AttackNamesies.SUNNY_DAY);
        utilityUmbrellaBoostTest(.5, 1.5, AttackNamesies.RAIN_DANCE);

        // Weather Ball will always be Normal-type and will not deal double damage with Utility Umbrella
        utilityUmbrellaTest(
                new TestInfo().attackingFight(AttackNamesies.HAIL)
                              .with(AttackNamesies.WEATHER_BALL),
                (battle, attacking, defending) -> {
                    attacking.setExpectedDamageModifier(2.0);
                    attacking.assertAttackType(Type.ICE);
                },
                (battle, attacking, defending) -> {
                    attacking.setExpectedDamageModifier(1.0);
                    attacking.assertAttackType(Type.NORMAL);
                },
                (battle, attacking, defending) -> battle.fight()
        );

        // Utility Umbrella has no effect when receiving weather ball damage
        utilityUmbrellaTest(
                new TestInfo().attackingFight(AttackNamesies.HAIL),
                (battle, attacking, defending) -> {
                    defending.setupMove(AttackNamesies.WEATHER_BALL, battle);
                    defending.setExpectedDamageModifier(2.0);
                    defending.assertAttackType(Type.ICE);
                    battle.fight();
                }
        );

        // Utility Umbrella will prevent Forecast from changing Castform's type
        utilityUmbrellaTest(
                new TestInfo().attacking(PokemonNamesies.CASTFORM, AbilityNamesies.FORECAST)
                              .attackingFight(AttackNamesies.RAIN_DANCE),
                (battle, attacking, defending) -> attacking.assertType(battle, Type.WATER),
                (battle, attacking, defending) -> attacking.assertType(battle, Type.NORMAL)
        );

        // Rock-type Pokemon will not get a Sp. Def boost during Sandstorm while holding Utility Umbrella
        utilityUmbrellaTest(
                new TestInfo().attacking(PokemonNamesies.GEODUDE).attackingFight(AttackNamesies.SANDSTORM),
                (battle, attacking, defending) -> attacking.assertStatModifier(1.5, Stat.SP_DEFENSE, battle),
                (battle, attacking, defending) -> attacking.assertStatModifier(1, Stat.SP_DEFENSE, battle)
        );

        // Utility umbrella holder should not take buffet damage
        utilityUmbrellaTest(
                new TestInfo().attacking(PokemonNamesies.EEVEE).attackingFight(AttackNamesies.SANDSTORM),
                (battle, attacking, defending) -> attacking.assertHealthRatio(15/16.0),
                (battle, attacking, defending) -> attacking.assertFullHealth()
        );

        // Growth will double stat growth in the Sunlight
        utilityUmbrellaTest(
                new TestInfo().attackingFight(AttackNamesies.SUNNY_DAY).attackingFight(AttackNamesies.GROWTH),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(2, Stat.ATTACK, Stat.SP_ATTACK)),
                (battle, attacking, defending) -> attacking.assertStages(new TestStages().set(1, Stat.ATTACK, Stat.SP_ATTACK))
        );

        // Utility Umbrella will cause Solar Beam to take its charging turn in the harsh sunlight
        // Solar Beam does not undergo a power change during harsh sunlight
        utilityUmbrellaTest(
                new TestInfo().attackingFight(AttackNamesies.SUNNY_DAY)
                              .with((battle, attacking, defending) -> attacking.setExpectedDamageModifier(1.0))
                              .attackingFight(AttackNamesies.SOLAR_BEAM),
                (battle, attacking, defending) -> {
                    MultiTurnMove solarBeam = (MultiTurnMove)attacking.getAttack();
                    Assert.assertFalse(solarBeam.isCharging());
                    defending.assertNotFullHealth();
                },
                (battle, attacking, defending) -> {
                    MultiTurnMove solarBeam = (MultiTurnMove)attacking.getAttack();
                    Assert.assertTrue(solarBeam.isCharging());
                    defending.assertFullHealth();

                    // Execute again and it should finish
                    battle.fight();
                    Assert.assertFalse(solarBeam.isCharging());
                    defending.assertNotFullHealth();
                }
        );

        // Solar Beam has it's damage halved in other weather conditions -- blocked by Utility Umbrella
        utilityUmbrellaTest(
                new TestInfo().defending(AbilityNamesies.OVERCOAT)
                              .attackingFight(AttackNamesies.HAIL)
                              .attackingFight(AttackNamesies.SOLAR_BEAM),
                (battle, attacking, defending) -> attacking.setExpectedDamageModifier(.5),
                (battle, attacking, defending) -> attacking.setExpectedDamageModifier(1.0),
                (battle, attacking, defending) -> {
                    // First turn was just the charge
                    // Note: Cannot use Power Herb to simplify test because would replace Utility Umbrella
                    MultiTurnMove solarBeam = (MultiTurnMove)attacking.getAttack();
                    Assert.assertTrue(solarBeam.isCharging());
                    defending.assertFullHealth();

                    // Execute again and it should finish
                    battle.fight();
                    Assert.assertFalse(solarBeam.isCharging());
                    defending.assertNotFullHealth();
                }
        );

        // Recovery moves that heal more in sunlight and less in rain, hail, and sandstorm
        // Utility Umbrella will always heal the standard amount
        utilityUmbrellaHealTest(
                2/3.0, .25, .25, .25, AttackNamesies.MOONLIGHT,
                AttackNamesies.MORNING_SUN, AttackNamesies.SYNTHESIS
        );

        // Shore Up heals more in a sandstorm -- blocked by Utility Umbrella
        utilityUmbrellaHealTest(.5, .5, 2/3.0, .5, AttackNamesies.SHORE_UP);

        // Recovery moves unaffected by weather -- mostly just here because the setup is here and because I can
        utilityUmbrellaHealTest(
                .5, .5, .5, .5, AttackNamesies.HEAL_ORDER, AttackNamesies.LIFE_DEW, AttackNamesies.MILK_DRINK,
                AttackNamesies.RECOVER, AttackNamesies.ROOST, AttackNamesies.SLACK_OFF, AttackNamesies.SOFT_BOILED
        );

        // Holding a Utility Umbrella will allow you to be frozen during intense sunlight
        utilityUmbrellaTest(
                new TestInfo().attacking(PokemonNamesies.EEVEE)
                              .attackingFight(AttackNamesies.SUNNY_DAY)
                              .defendingFight(AttackNamesies.FAKE_FREEZER),
                (battle, attacking, defending) -> attacking.assertNoStatus(),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.FROZEN)
        );

        // Hurricane and Thunder always hit in Rain, but accuracy is reduced to 50% in harsh sunlight
        utilityUmbrellaAccuracyTest(AttackNamesies.RAIN_DANCE, AttackNamesies.SUNNY_DAY, AttackNamesies.HURRICANE);
        utilityUmbrellaAccuracyTest(AttackNamesies.RAIN_DANCE, AttackNamesies.SUNNY_DAY, AttackNamesies.THUNDER);

        // Blizzard always hits in Hail (never reduced accuracy in other conditions)
        utilityUmbrellaAccuracyTest(AttackNamesies.HAIL, null, AttackNamesies.BLIZZARD);

        // Aurora Veil fails without Hail (or if holding a certain umbrella...)
        utilityUmbrellaTest(
                new TestInfo().attackingFight(AttackNamesies.HAIL).attackingFight(AttackNamesies.AURORA_VEIL),
                (battle, attacking, defending) -> battle.assertHasEffect(attacking, TeamEffectNamesies.AURORA_VEIL),
                (battle, attacking, defending) -> battle.assertNoEffect(attacking, TeamEffectNamesies.AURORA_VEIL)
        );

        // Dry Skin loses health in the sun
        utilityUmbrellaTest(
                new TestInfo().attacking(AbilityNamesies.DRY_SKIN).attackingFight(AttackNamesies.SUNNY_DAY),
                (battle, attacking, defending) -> attacking.assertHealthRatio(7/8.0),
                (battle, attacking, defending) -> attacking.assertFullHealth()
        );

        // Dry Skin gains health in the rain
        utilityUmbrellaTest(
                new TestInfo().attacking(AbilityNamesies.DRY_SKIN)
                              .falseSwipePalooza(false)
                              .attackingFight(AttackNamesies.RAIN_DANCE),
                (battle, attacking, defending) -> attacking.assertHealthRatioDiff(1, -1/8.0),
                (battle, attacking, defending) -> attacking.assertHp(1)
        );

        // Abilities that double speed in specific weather conditions
        utilityUmbrellaSpeedTest(AttackNamesies.SUNNY_DAY, AbilityNamesies.CHLOROPHYLL);
        utilityUmbrellaSpeedTest(AttackNamesies.RAIN_DANCE, AbilityNamesies.SWIFT_SWIM);
        utilityUmbrellaSpeedTest(AttackNamesies.SANDSTORM, AbilityNamesies.SAND_RUSH);
        utilityUmbrellaSpeedTest(AttackNamesies.HAIL, AbilityNamesies.SLUSH_RUSH);

        // Abilities which boost evasion is specific weather conditions
        utilityUmbrellaEvasionTest(AttackNamesies.SANDSTORM, AbilityNamesies.SAND_VEIL);
        utilityUmbrellaEvasionTest(AttackNamesies.HAIL, AbilityNamesies.SNOW_CLOAK);

        // Flower Gift boosts Attack and Sp. Def in the sun
        utilityUmbrellaTest(
                new TestInfo().attacking(AbilityNamesies.FLOWER_GIFT).attackingFight(AttackNamesies.SUNNY_DAY),
                (battle, attacking, defending) -> {
                    attacking.assertStatModifier(1.5, Stat.ATTACK, battle);
                    attacking.assertStatModifier(1.5, Stat.SP_DEFENSE, battle);
                },
                (battle, attacking, defending) -> {
                    attacking.assertStatModifier(1, Stat.ATTACK, battle);
                    attacking.assertStatModifier(1, Stat.SP_DEFENSE, battle);
                }
        );

        // Leaf Guard prevents status conditions in the sun (gonna get burned without that sunbrella though!)
        utilityUmbrellaTest(
                new TestInfo().attacking(PokemonNamesies.EEVEE, AbilityNamesies.LEAF_GUARD)
                              .attackingFight(AttackNamesies.SUNNY_DAY)
                              .defendingFight(AttackNamesies.WILL_O_WISP),
                (battle, attacking, defending) -> attacking.assertNoStatus(),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.BURNED)
        );

        // Solar Power loses health in the sun and boosts special attacks in the sun
        utilityUmbrellaTest(
                new TestInfo().attacking(AbilityNamesies.SOLAR_POWER).attackingFight(AttackNamesies.SUNNY_DAY),
                (battle, attacking, defending) -> {
                    attacking.assertHealthRatio(7/8.0);
                    attacking.setExpectedDamageModifier(1.5);
                },
                (battle, attacking, defending) -> {
                    attacking.assertFullHealth();
                    attacking.setExpectedDamageModifier(1.0);
                },
                (battle, attacking, defending) -> battle.attackingFight(AttackNamesies.SWIFT)
        );

        // Hydration will heal status conditions in the rain
        utilityUmbrellaTest(
                new TestInfo().attacking(PokemonNamesies.EEVEE, AbilityNamesies.HYDRATION)
                              .defendingFight(AttackNamesies.WILL_O_WISP)
                              .attackingFight(AttackNamesies.RAIN_DANCE),
                (battle, attacking, defending) -> attacking.assertNoStatus(),
                (battle, attacking, defending) -> attacking.assertHasStatus(StatusNamesies.BURNED)
        );

        // Abilities that heal 1/16 Max HP at the end of turn in specific weather conditions
        utilityUmbrellaHealTest(AttackNamesies.RAIN_DANCE, AbilityNamesies.RAIN_DISH);
        utilityUmbrellaHealTest(AttackNamesies.HAIL, AbilityNamesies.ICE_BODY);

        // Sand Force boosts Rock, Ground, and Steel moves in Sandstorm
        utilityUmbrellaTest(
                new TestInfo().attacking(AbilityNamesies.SAND_FORCE).attackingFight(AttackNamesies.SANDSTORM),
                (battle, attacking, defending) -> attacking.setExpectedDamageModifier(1.3),
                (battle, attacking, defending) -> attacking.setExpectedDamageModifier(1.0),
                (battle, attacking, defending) -> battle.attackingFight(AttackNamesies.IRON_HEAD)
        );

        // Ice Face will not restore form when hail starts when holding Utility Umbrella
        // Note: This is true because Utility Umbrella was changed from just Sun/Rain to all weather effects
        // TODO: Change to Eiscue once in the game
        utilityUmbrellaTest(
                new TestInfo().attacking(PokemonNamesies.GLACEON, AbilityNamesies.ICE_FACE)
                              .with((battle, attacking, defending) -> {
                                  // False Swipe will be absorbed by the Ice Face and should be transformed
                                  battle.defendingFight(AttackNamesies.FALSE_SWIPE);
                                  attacking.assertFullHealth();
                                  Assert.assertTrue(attacking.hasAbsorbedDamage());

                                  // No more Ice Face -- False Swipe should be able to hit now
                                  battle.defendingFight(AttackNamesies.FALSE_SWIPE);
                                  attacking.assertNotFullHealth();
                                  Assert.assertFalse(attacking.hasAbsorbedDamage());

                                  // Heal back to full (for ease of checking absorbed)
                                  battle.fight(AttackNamesies.RECOVER, AttackNamesies.HEAL_PULSE);
                                  attacking.assertFullHealth();

                                  // Hail should restore the Ice Face without Utility Umbrella
                                  battle.attackingFight(AttackNamesies.HAIL);
                                  battle.defendingFight(AttackNamesies.FALSE_SWIPE);
                              }),
                (battle, attacking, defending) -> {
                    // Ice Face restored and damage can be absorbed again
                    attacking.assertFullHealth();
                    Assert.assertTrue(attacking.hasAbsorbedDamage());
                },
                (battle, attacking, defending) -> {
                    // You're on your own, umbrella boy
                    attacking.assertNotFullHealth();
                    Assert.assertFalse(attacking.hasAbsorbedDamage());
                }
        );
    }

    private void utilityUmbrellaHealTest(AttackNamesies healWeatherAttack, AbilityNamesies healAbility) {
        // Heal at end of turn in specified weather
        // Confirm does not heal in other weather conditions
        // Note: Need to use something like Swinub here to prevent buffet damage from killing
        utilityUmbrellaTest(
                healWeatherAttack,
                weatherAttack -> new TestInfo().attacking(PokemonNamesies.SWINUB, healAbility)
                                               .falseSwipePalooza(false)
                                               .attackingFight(weatherAttack),
                (battle, attacking, defending) -> attacking.assertHealthRatioDiff(1, -1/16.0),
                (battle, attacking, defending) -> attacking.assertHp(1)
        );
    }

    private void utilityUmbrellaEvasionTest(AttackNamesies evasionWeatherAttack, AbilityNamesies evasionAbility) {
        // Technically these are stage modifying abilities, not stat modifying, but this checks the same thing
        utilityUmbrellaStatTest(4/3.0, Stat.EVASION, evasionWeatherAttack, evasionAbility);
    }

    private void utilityUmbrellaSpeedTest(AttackNamesies speedWeatherAttack, AbilityNamesies speedAbility) {
        // Speed should double for the specified weather condition and ability -- umbrella blocks boost
        // Confirm speed is unmodified for other weather conditions
        utilityUmbrellaStatTest(2, Stat.SPEED, speedWeatherAttack, speedAbility);
    }

    private void utilityUmbrellaStatTest(double boost, Stat stat, AttackNamesies statWeatherAttack, AbilityNamesies statAbility) {
        TestUtils.assertGreater(boost, 1);

        // Stat should increase for the specified weather condition and ability -- umbrella blocks boost
        // Confirm stat is unmodified for other weather conditions
        utilityUmbrellaTest(
                statWeatherAttack,
                weatherAttack -> new TestInfo().attacking(statAbility).attackingFight(weatherAttack),
                (battle, attacking, defending) -> attacking.assertStatModifier(boost, stat, battle),
                (battle, attacking, defending) -> attacking.assertStatModifier(1, stat, battle)
        );
    }

    private void utilityUmbrellaAccuracyTest(AttackNamesies bypassWeatherAttack, AttackNamesies decreaseWeatherAttack, AttackNamesies accuracyAttack) {
        Assert.assertNotEquals(bypassWeatherAttack, decreaseWeatherAttack);

        // Case where attack should bypass accuracy in weather -- blocked by Utility Umbrella
        // Confirm only bypasses for this specific weather condition
        utilityUmbrellaTest(
                bypassWeatherAttack, weatherAttack -> new TestInfo().attackingFight(weatherAttack),
                (battle, attacking, defending) -> attacking.setExpectedAccuracyBypass(true),
                (battle, attacking, defending) -> attacking.setExpectedAccuracyBypass(null),
                (battle, attacking, defending) -> battle.attackingFight(accuracyAttack)
        );

        int baseAccuracy = 70;
        int decreaseWeatherAccuracy = decreaseWeatherAttack == null ? baseAccuracy : 50;

        // Compares the expected accuracy with the actual accuracy of the set up attack
        Function<Integer, PokemonManipulator> assertAccuracy = expectedAccuracy -> (battle, attacking, defending) -> {
            int actualAccuracy = attacking.getAttack().getAccuracy(battle, attacking, defending);
            Assert.assertEquals((int)expectedAccuracy, actualAccuracy);
        };

        // Confirm that the accuracy of this attack is decreased in the current weather condition
        // For other weather conditions, should always just have the base accuracy
        // Note: Okay for decreaseWeatherAttack to be null (will just confirm always base accuracy)
        utilityUmbrellaTest(
                decreaseWeatherAttack,
                weatherAttack -> new TestInfo().attackingFight(weatherAttack).with(accuracyAttack),
                assertAccuracy.apply(decreaseWeatherAccuracy),
                assertAccuracy.apply(baseAccuracy)
        );
    }

    private void utilityUmbrellaTest(AttackNamesies specifiedWeatherAttack,
                                     Function<AttackNamesies, TestInfo> testInfoFunction,
                                     PokemonManipulator withoutUmbrella,
                                     PokemonManipulator withUmbrella) {
        utilityUmbrellaTest(specifiedWeatherAttack, testInfoFunction, withoutUmbrella, withUmbrella, PokemonManipulator.empty());
    }

    // Checks all weather conditions with the testInfoFunction
    // Only the conditionWeatherAttack should be relevant and will have a different effect with and without umbrella
    // All other weather conditions will be as if the weather is irrelevant -- same as holding umbrella
    private void utilityUmbrellaTest(AttackNamesies conditionWeatherAttack,
                                     Function<AttackNamesies, TestInfo> testInfoFunction,
                                     PokemonManipulator withoutUmbrella,
                                     PokemonManipulator withUmbrella,
                                     PokemonManipulator afterBoth) {
        List<AttackNamesies> weatherAttacks = List.of(
                AttackNamesies.SUNNY_DAY,
                AttackNamesies.RAIN_DANCE,
                AttackNamesies.SANDSTORM,
                AttackNamesies.HAIL
        );

        for (AttackNamesies weatherAttack : weatherAttacks) {
            TestInfo testInfo = testInfoFunction.apply(weatherAttack);
            if (weatherAttack == conditionWeatherAttack) {
                utilityUmbrellaTest(testInfo, withoutUmbrella, withUmbrella, afterBoth);
            } else {
                utilityUmbrellaTest(testInfo, withUmbrella.add(afterBoth));
            }
        }
    }

    private void utilityUmbrellaHealTest(double sunHeal, double rainHeal, double sandHeal, double hailHeal, AttackNamesies... healAttacks) {
        double clearHeal = .5;
        for (AttackNamesies healAttack : healAttacks) {
            utilityUmbrellaHealTest(clearHeal, sunHeal, healAttack, AttackNamesies.SUNNY_DAY);
            utilityUmbrellaHealTest(clearHeal, rainHeal, healAttack, AttackNamesies.RAIN_DANCE);
            utilityUmbrellaHealTest(clearHeal, sandHeal, healAttack, AttackNamesies.SANDSTORM);
            utilityUmbrellaHealTest(clearHeal, hailHeal, healAttack, AttackNamesies.HAIL);
            utilityUmbrellaHealTest(clearHeal, clearHeal, healAttack, AttackNamesies.SPLASH);
        }
    }

    private void utilityUmbrellaHealTest(double clearHeal, double weatherHeal, AttackNamesies healAttack, AttackNamesies weatherAttack) {
        utilityUmbrellaTest(
                new TestInfo().attacking(AbilityNamesies.OVERCOAT)
                              .attackingFight(weatherAttack)
                              .falseSwipePalooza(false)
                              .attackingFight(healAttack),
                (battle, attacking, defending) -> attacking.assertHealthRatioDiff(1, -weatherHeal),
                (battle, attacking, defending) -> attacking.assertHealthRatioDiff(1, -clearHeal)
        );
    }

    private void utilityUmbrellaBoostTest(double fireBoost, double waterBoost, AttackNamesies weatherAttack) {
        utilityUmbrellaBoostTest(fireBoost, weatherAttack, AttackNamesies.EMBER);
        utilityUmbrellaBoostTest(waterBoost, weatherAttack, AttackNamesies.WATER_GUN);
        utilityUmbrellaBoostTest(1, weatherAttack, AttackNamesies.THUNDER_SHOCK);
    }

    private void utilityUmbrellaBoostTest(double weatherBoost, AttackNamesies weatherAttack, AttackNamesies boostAttack) {
        // Utility Umbrella will not boost/decrease power for the moves the user is using
        utilityUmbrellaTest(
                new TestInfo().attackingFight(weatherAttack),
                (battle, attacking, defending) -> attacking.setExpectedDamageModifier(weatherBoost),
                (battle, attacking, defending) -> attacking.setExpectedDamageModifier(1.0),
                (battle, attacking, defending) -> battle.attackingFight(boostAttack)
        );

        // However, Utility Umbrella will not change power when holder is receiving the attack
        // Note: This behavior is intentionally reversed on what occurs in game
        utilityUmbrellaTest(
                new TestInfo().attackingFight(weatherAttack),
                (battle, attacking, defending) -> {
                    defending.setExpectedDamageModifier(weatherBoost);
                    battle.defendingFight(boostAttack);
                }
        );
    }

    private void utilityUmbrellaTest(TestInfo testInfo, PokemonManipulator samesies) {
        utilityUmbrellaTest(testInfo, samesies, samesies);
    }

    private void utilityUmbrellaTest(TestInfo testInfo, PokemonManipulator withoutUmbrella, PokemonManipulator withUmbrella) {
        utilityUmbrellaTest(testInfo, withoutUmbrella, withUmbrella, PokemonManipulator.empty());
    }

    private void utilityUmbrellaTest(TestInfo testInfo, PokemonManipulator withoutUmbrella, PokemonManipulator withUmbrella, PokemonManipulator afterBoth) {
        testInfo.doubleTake(
                (battle, attacking, defending) -> attacking.withItem(ItemNamesies.UTILITY_UMBRELLA),
                (battle, attacking, defending) -> {
                    attacking.assertNotHoldingItem();
                    withoutUmbrella.manipulate(battle);
                    afterBoth.manipulate(battle);
                },
                (battle, attacking, defending) -> {
                    attacking.assertHoldingItem(ItemNamesies.UTILITY_UMBRELLA);
                    withUmbrella.manipulate(battle);
                    afterBoth.manipulate(battle);
                }
        );
    }

    @Test
    public void roomServiceTest() {
        // Room Service lowers Speed by 1 stage when Trick Room Effect starts
        roomServiceTest(new TestInfo());

        // Competitive is not triggered by Room Service
        roomServiceTest(new TestInfo().attacking(AbilityNamesies.COMPETITIVE));

        // Mirror Armor will not reflect stat drops
        roomServiceTest(new TestInfo().attacking(AbilityNamesies.MIRROR_ARMOR));

        // Contrary will raise Speed with Room Service
        roomServiceTest(new TestStages().set(1, Stat.SPEED), new TestInfo().attacking(AbilityNamesies.CONTRARY));

        // Room Service will trigger regardless of which Pokemon used Trick Room
        // Both Pokemon may consume Room Service in the same turn
        roomServiceTest(
                new TestInfo().defending(ItemNamesies.ROOM_SERVICE),
                (battle, attacking, defending) -> {
                    defending.assertConsumedItem();
                    defending.assertStages(new TestStages().set(-1, Stat.SPEED));
                }
        );

        // Removing Trick Room by using a second time should not trigger Room Service
        roomServiceTest(
                new TestInfo(),
                (battle, attacking, defending) -> {
                    // Confirm Trick Room already in play
                    battle.assertHasEffect(StandardBattleEffectNamesies.TRICK_ROOM);
                    attacking.assertConsumedItem();

                    // Give Room Service while Trick Room already in effect
                    defending.giveItem(ItemNamesies.ROOM_SERVICE);
                    defending.assertNotConsumedItem();
                    defending.assertNoStages();

                    // Using Trick Room while already in effect will immediately remove the Trick Room effect
                    // Room Service should NOT trigger from this action
                    battle.attackingFight(AttackNamesies.TRICK_ROOM);
                    battle.assertNoEffect(StandardBattleEffectNamesies.TRICK_ROOM);
                    defending.assertNotConsumedItem();
                    defending.assertNoStages();

                    // Go back to the Trick Room and make sure Room Service is consumed
                    battle.attackingFight(AttackNamesies.TRICK_ROOM);
                    battle.assertHasEffect(StandardBattleEffectNamesies.TRICK_ROOM);
                    defending.assertConsumedItem();
                    defending.assertStages(new TestStages().set(-1, Stat.SPEED));
                }
        );
    }

    private void roomServiceTest(TestInfo testInfo) {
        roomServiceTest(testInfo, PokemonManipulator.empty());
    }

    private void roomServiceTest(TestInfo testInfo, PokemonManipulator afterCheck) {
        roomServiceTest(new TestStages().set(-1, Stat.SPEED), testInfo, afterCheck);
    }

    private void roomServiceTest(TestStages stages, TestInfo testInfo) {
        roomServiceTest(stages, testInfo, PokemonManipulator.empty());
    }

    private void roomServiceTest(TestStages stages, TestInfo testInfo, PokemonManipulator afterCheck) {
        TestBattle battle = testInfo.createBattle();
        TestPokemon attacking = battle.getAttacking().withItem(ItemNamesies.ROOM_SERVICE);

        testInfo.manipulate(battle);
        battle.attackingFight(AttackNamesies.TRICK_ROOM);

        attacking.assertConsumedItem();
        attacking.assertStages(stages);

        afterCheck.manipulate(battle);
    }
}
