package test.battle;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.effect.attack.MultiTurnMove;
import battle.effect.generic.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusCondition;
import generator.update.ItemUpdater;
import generator.update.ItemUpdater.ItemParser;
import item.Item;
import item.ItemNamesies;
import item.bag.BagCategory;
import item.berry.Berry;
import item.berry.CategoryBerry;
import item.hold.HoldItem;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.EvolutionItem;
import item.use.TechnicalMachine;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import test.BaseTest;
import test.TestPokemon;
import type.Type;

import java.util.EnumSet;
import java.util.Set;

public class ItemTest extends BaseTest {
    @Test
    public void categoryTest() {
        for (ItemNamesies itemNamesies : ItemNamesies.values()) {
            Item item = itemNamesies.getItem();

            // If it has battle catagories, then it must be a BattleUseItem
            Assert.assertEquals(
                    item.getName(),
                    item instanceof BattleUseItem || item instanceof BallItem,
                    item.getBattleBagCategories().iterator().hasNext()
            );

            // Category berries can't be for status moves
            if (item instanceof CategoryBerry) {
                MoveCategory category = ((CategoryBerry)item).getCategory();
                Assert.assertNotNull(item.getName(), category);
                Assert.assertNotEquals(item.getName(), MoveCategory.STATUS, category);
            }
        }
    }

    @Test
    public void parserTest() {
        Set<ItemNamesies> toParse = EnumSet.allOf(ItemNamesies.class);
        toParse.remove(ItemNamesies.NO_ITEM);
        toParse.remove(ItemNamesies.SYRUP);
        toParse.remove(ItemNamesies.SURFBOARD);
        toParse.remove(ItemNamesies.RUBY);
        toParse.removeIf(itemNamesies -> itemNamesies.getItem() instanceof TechnicalMachine);

        for (ItemParser itemParser : new ItemUpdater().getParseItems()) {
            ItemNamesies itemNamesies = itemParser.itemNamesies;
            String itemType = itemParser.itemType;

            int fling = itemParser.fling;
            int price = itemParser.price;

            Type naturalGiftType = itemParser.naturalGiftType;
            int naturalGiftPower = itemParser.naturalGiftPower;

            Item item = itemNamesies.getItem();
            if (item.isHoldable() && fling != 0) {
                HoldItem holdItem = (HoldItem)item;
                Assert.assertEquals(item.getName(), fling, holdItem.flingDamage());
            } else if (!(item instanceof BallItem)) {
                // Ball items are not holdable in this game
                Assert.assertEquals(item.getName(), 0, fling);
            }

            if (item.getBagCategory() != BagCategory.KEY_ITEM) {
                // Serebii has the wrong values for these, and I manually looked up in Bulbapedia instead (which is more accurate but way harder to parse)
                // Just gonna leave this commented out since it's annoying and who cares
//                TestUtils.semiAssertTrue(StringUtils.spaceSeparated(item.getName(), price, item.getPrice()), price == item.getPrice());

                if (itemNamesies == ItemNamesies.MASTER_BALL || itemNamesies == ItemNamesies.SAFARI_BALL) {
                    Assert.assertEquals(item.getName(), 0, item.getPrice());
                } else {
                    Assert.assertTrue(item.getName(), item.getPrice() > 0);
                }
            } else {
                Assert.assertEquals(item.getName(), 0, price);
                Assert.assertEquals(item.getName(), -1, item.getPrice());
            }

            if (item instanceof Berry) {
                Berry berry = (Berry)item;
                Assert.assertEquals(item.getName(), naturalGiftType, berry.naturalGiftType());
                Assert.assertEquals(item.getName(), naturalGiftPower, berry.naturalGiftPower());
                Assert.assertNotEquals(item.getName(), Type.NO_TYPE, naturalGiftType);
                Assert.assertNotEquals(item.getName(), 0, naturalGiftPower);
            } else {
                Assert.assertEquals(item.getName(), Type.NO_TYPE, naturalGiftType);
                Assert.assertEquals(item.getName(), 0, naturalGiftPower);
            }

            switch (itemType) {
                case "Battle Effect":
                    Assert.assertTrue(item.getName(), item instanceof BattleUseItem);
                    break;
                case "Miscellaneous":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.MISC);
                    break;
                case "Evolutionary":
                    Assert.assertTrue(item.getName(), item instanceof EvolutionItem);
                    break;
                case "Berry":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.BERRY);
                    break;
                case "Key Item":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.KEY_ITEM);
                    break;
                case "Hold Item":
                    Assert.assertTrue(item.getName(), item instanceof HoldItem);
                    break;
                case "Recovery":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.MEDICINE);
                    break;
                case "Pokeball":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.BALL);
                    break;
                case "Vitamins":
                    Assert.assertTrue(item.getName(), item.getBagCategory() == BagCategory.STAT);
                    break;
                default:
                    Assert.fail(item.getName() + ": " + itemType);
                    break;
            }

            toParse.remove(itemNamesies);
        }

        Assert.assertTrue(toParse.isEmpty());
    }

    @Test
    public void swapItemsTest() {
        // Swapping items works differently for wild battles vs trainer battles
        swapItemsTest(false);
        swapItemsTest(true);
    }

    private void swapItemsTest(boolean trainerBattle) {
        PokemonNamesies attackingPokemon = PokemonNamesies.BULBASAUR;
        PokemonNamesies defendingPokemon = PokemonNamesies.CHARMANDER;
        TestBattle battle = trainerBattle
                ? TestBattle.createTrainerBattle(attackingPokemon, defendingPokemon)
                : TestBattle.create(attackingPokemon, defendingPokemon);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        defending.giveItem(ItemNamesies.WATER_STONE);

        // Thief -- confirm stolen item
        battle.fight(AttackNamesies.THIEF, AttackNamesies.ENDURE);
        battle.emptyHeal();
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.WATER_STONE));
        Assert.assertFalse(defending.isHoldingItem(battle));

        // Bestow -- confirm item transferred
        battle.attackingFight(AttackNamesies.BESTOW);
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.WATER_STONE));

        // Magician -- steal item back
        attacking.withAbility(AbilityNamesies.MAGICIAN);
        battle.fight(AttackNamesies.SWIFT, AttackNamesies.ENDURE);
        battle.emptyHeal();
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.WATER_STONE));
        Assert.assertFalse(defending.isHoldingItem(battle));

        // Trick -- swap item back
        battle.emptyHeal();
        battle.attackingFight(AttackNamesies.TRICK);
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.WATER_STONE));

        // Sticky Hold -- Item can't be swapped
        battle.emptyHeal();
        defending.withAbility(AbilityNamesies.STICKY_HOLD);
        battle.attackingFight(AttackNamesies.SWITCHEROO);
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.WATER_STONE));
        battle.fight(AttackNamesies.KNOCK_OFF, AttackNamesies.ENDURE);
        battle.emptyHeal();
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.WATER_STONE));
        attacking.withAbility(AbilityNamesies.PICKPOCKET);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.TACKLE);
        battle.emptyHeal();
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.WATER_STONE));

        // Mold breaker overrules Sticky Hold
        attacking.withAbility(AbilityNamesies.MOLD_BREAKER);
        battle.attackingFight(AttackNamesies.TRICK);
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.WATER_STONE));
        Assert.assertFalse(defending.isHoldingItem(battle));

        // Pickpocket -- steal item on contact
        attacking.withAbility(AbilityNamesies.PICKPOCKET);
        defending.withAbility(AbilityNamesies.NO_ABILITY);
        battle.fight(AttackNamesies.ENDURE, AttackNamesies.TACKLE);
        battle.emptyHeal();
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.WATER_STONE));
        Assert.assertFalse(defending.isHoldingItem(battle));
    }

    @Test
    public void stickyBarbTest() {
        // Swapping items works differently for wild battles vs trainer battles
        stickyBarbTest(false);
        stickyBarbTest(true);
    }

    private void stickyBarbTest(boolean trainerBattle) {
        PokemonNamesies attackingPokemon = PokemonNamesies.BULBASAUR;
        PokemonNamesies defendingPokemon = PokemonNamesies.CHARMANDER;
        TestBattle battle = trainerBattle
                ? TestBattle.createTrainerBattle(attackingPokemon, defendingPokemon)
                : TestBattle.create(attackingPokemon, defendingPokemon);
        TestPokemon attacking = battle.getAttacking();
        TestPokemon defending = battle.getDefending();

        // So I don't have to worry about dying from Sticky Barb's end turn effect
        attacking.withAbility(AbilityNamesies.MAGIC_GUARD);
        defending.withAbility(AbilityNamesies.MAGIC_GUARD);

        // Give Sticky Barb
        attacking.giveItem(ItemNamesies.WATER_STONE);
        defending.giveItem(ItemNamesies.STICKY_BARB);
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.WATER_STONE));
        Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.STICKY_BARB));

        // Switcheroo -- swap items
        battle.attackingFight(AttackNamesies.SWITCHEROO);
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.STICKY_BARB));
        Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.WATER_STONE));

        // Knock off -- remove defending item
        battle.fight(AttackNamesies.KNOCK_OFF, AttackNamesies.ENDURE);
        battle.emptyHeal();
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.STICKY_BARB));
        Assert.assertFalse(defending.isHoldingItem(battle));

        // Bestow -- transfer Sticky Barb
        Assert.assertTrue(attacking.canGiftItem(battle, defending));
        battle.attackingFight(AttackNamesies.BESTOW);
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertTrue(defending.isHoldingItem(battle, ItemNamesies.STICKY_BARB));

        // Sticky Barb -- transfer on contact
        battle.fight(AttackNamesies.TACKLE, AttackNamesies.ENDURE);
        battle.emptyHeal();
        Assert.assertTrue(attacking.isHoldingItem(battle, ItemNamesies.STICKY_BARB));
        Assert.assertFalse(defending.isHoldingItem(battle));
    }

    @Test
    public void mentalHerbTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withGender(Gender.FEMALE);
        TestPokemon defending = battle.getDefending().withGender(Gender.MALE);

        battle.attackingFight(AttackNamesies.ATTRACT);
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.INFATUATED));

        attacking.giveItem(ItemNamesies.MENTAL_HERB);
        battle.attackingFight(AttackNamesies.FLING);
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.INFATUATED));
        Assert.assertFalse(attacking.isHoldingItem(battle));

        battle.defendingFight(AttackNamesies.CONFUSE_RAY);
        Assert.assertTrue(attacking.hasEffect(PokemonEffectNamesies.CONFUSION));

        // Mental Herb cures at the end of the turn
        attacking.giveItem(ItemNamesies.MENTAL_HERB);
        battle.attackingFight(AttackNamesies.SPLASH);
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.CONFUSION));
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
        Assert.assertFalse(defending.isHoldingItem(battle));

        Assert.assertEquals(success, defending.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));
        Assert.assertEquals(success, defending.getActualHeldItem().namesies() == ItemNamesies.NO_ITEM);

        // If successful, should increase Sp. Attack by one
        new TestStages().set(Stat.SP_ATTACK, success ? 1 : 0).test(defending);
    }

    @Test
    public void powerHerbTest() {
        PokemonManipulator notFullHealth = (battle, attacking, defending) -> {
            attacking.assertFullHealth();
            defending.assertNotFullHealth();
            new TestStages().test(attacking);
            new TestStages().test(defending);

            // Additionally fully heals the defending so this can be used in subsequent turns and still be meaningful
            defending.fullyHeal();
        };

        PokemonManipulator charging = (battle, attacking, defending) -> {
            MultiTurnMove multiTurnMove = (MultiTurnMove)attacking.getAttack();
            Assert.assertTrue(multiTurnMove.isCharging());

            attacking.assertFullHealth();
            defending.assertFullHealth();

            new TestStages().test(attacking);
            new TestStages().test(defending);
        };

        // Power Herb is consumed and damage is dealt first turn, charges on the second
        powerHerbTest(AttackNamesies.SOLAR_BEAM, true, notFullHealth, charging);

        // Power Herb is consumed and damage is dealt first turn -- defense is NOT raised
        // Charges on the second turn and defense is raised
        powerHerbTest(AttackNamesies.SKULL_BASH, true, notFullHealth, (battle, attacking, defending) -> {
            MultiTurnMove multiTurnMove = (MultiTurnMove)attacking.getAttack();
            Assert.assertTrue(multiTurnMove.isCharging());
            new TestStages().set(Stat.DEFENSE, 1)
                            .test(attacking);
            new TestStages().test(defending);
        });

        // Power Herb is consumed and stats are raised
        powerHerbTest(AttackNamesies.GEOMANCY, true, (battle, attacking, defending) -> {
            attacking.assertFullHealth();
            defending.assertFullHealth();
            new TestStages().set(Stat.SP_ATTACK, 2)
                            .set(Stat.SP_DEFENSE, 2)
                            .set(Stat.SPEED, 2)
                            .test(attacking);
            new TestStages().test(defending);

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

        beforeFirstTurn.manipulate(battle, attacking, defending);

        attacking.setMove(new Move(attackingMove));
        defending.setMove(new Move(AttackNamesies.SPLASH));

        int attackingPP = attacking.getMove().getPP();
        int defendingPP = defending.getMove().getPP();

        battle.fight();

        Assert.assertNotEquals(consumeItem, attacking.isHoldingItem(battle));
        Assert.assertEquals(consumeItem, attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));

        boolean isMultiTurn = attacking.getAttack() instanceof MultiTurnMove;
        boolean fullyExecuted = !isMultiTurn || skipCharge || consumeItem;

        // If it is a Multi-turn move and the item was not consumed, then it should still be charging
        Assert.assertEquals(attackingPP - (fullyExecuted ? 1 : 0), attacking.getMove().getPP());
        Assert.assertEquals(defendingPP - 1, defending.getMove().getPP());

        afterFirstTurn.manipulate(battle, attacking, defending);

        battle.fight();

        Assert.assertNotEquals(consumeItem, attacking.isHoldingItem(battle));
        Assert.assertEquals(consumeItem, attacking.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM));

        Assert.assertEquals(attackingPP - (!isMultiTurn || skipCharge ? 2 : 1), attacking.getMove().getPP());
        Assert.assertEquals(defendingPP - 2, defending.getMove().getPP());

        afterSecondTurn.manipulate(battle, attacking, defending);
    }

    @Test
    public void swapConsumeItemTest() {
        // Swapping items works differently for wild battles vs trainer battles
        swapConsumeItemTest(false);
        swapConsumeItemTest(true);
    }

    private void swapConsumeItemTest(boolean trainerBattle) {
        PokemonNamesies attackingPokemon = PokemonNamesies.BULBASAUR;
        PokemonNamesies defendingPokemon = PokemonNamesies.CHARMANDER;
        TestBattle battle = trainerBattle
                ? TestBattle.createTrainerBattle(attackingPokemon, defendingPokemon)
                : TestBattle.create(attackingPokemon, defendingPokemon);
        TestPokemon attacking = battle.getAttacking().withItem(ItemNamesies.LUM_BERRY);
        TestPokemon defending = battle.getDefending().withItem(ItemNamesies.RAWST_BERRY);

        // Lum Berry should activate to remove the burn
        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertTrue(defending.lastMoveSucceeded());
        Assert.assertFalse(attacking.hasStatus());

        // Lum Berry has already been consumed, so the burn should remain
        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.BURNED));

        // Swap items to retrieve the Rawst Berry, which should activate to remove the burn
        battle.attackingFight(AttackNamesies.TRICK);
        Assert.assertFalse(attacking.hasStatus());
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));

        // Rawst Berry has already been consumed, so the burn should remain
        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        Assert.assertTrue(attacking.hasStatus(StatusCondition.BURNED));
    }

    @Test
    public void destinyKnotTest() {
        TestBattle battle = TestBattle.create();
        TestPokemon attacking = battle.getAttacking().withGender(Gender.FEMALE);
        TestPokemon defending = battle.getDefending().withGender(Gender.MALE);

        battle.attackingFight(AttackNamesies.ATTRACT);
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.INFATUATED));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.INFATUATED));

        battle.clearAllEffects();
        Assert.assertFalse(attacking.hasEffect(PokemonEffectNamesies.INFATUATED));
        Assert.assertFalse(defending.hasEffect(PokemonEffectNamesies.INFATUATED));

        // Destiny Knot causes the caster to be infatuated as well
        defending.withItem(ItemNamesies.DESTINY_KNOT);
        battle.attackingFight(AttackNamesies.ATTRACT);
        Assert.assertTrue(attacking.hasEffect(PokemonEffectNamesies.INFATUATED));
        Assert.assertTrue(defending.hasEffect(PokemonEffectNamesies.INFATUATED));
    }
}
