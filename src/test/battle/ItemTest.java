package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import generator.update.ItemUpdater;
import generator.update.ItemUpdater.ItemParser;
import item.Item;
import item.ItemNamesies;
import item.bag.BagCategory;
import item.berry.Berry;
import item.hold.HoldItem;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.EvolutionItem;
import item.use.TechnicalMachine;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import test.BaseTest;
import test.TestPokemon;
import type.Type;

import java.util.EnumSet;
import java.util.Set;

public class ItemTest extends BaseTest {
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
        stickyBarbTest(false);

        swapItemsTest(true);
        stickyBarbTest(true);
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
        Assert.assertTrue(defending.hasEffect(EffectNamesies.INFATUATED));

        attacking.giveItem(ItemNamesies.MENTAL_HERB);
        battle.attackingFight(AttackNamesies.FLING);
        Assert.assertFalse(defending.hasEffect(EffectNamesies.INFATUATED));
        Assert.assertFalse(attacking.isHoldingItem(battle));

        battle.defendingFight(AttackNamesies.CONFUSE_RAY);
        Assert.assertTrue(attacking.hasEffect(EffectNamesies.CONFUSION));

        // Mental Herb cures at the end of the turn
        attacking.giveItem(ItemNamesies.MENTAL_HERB);
        battle.attackingFight(AttackNamesies.SPLASH);
        Assert.assertFalse(attacking.hasEffect(EffectNamesies.CONFUSION));
    }
}
