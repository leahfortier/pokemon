package test.battle;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.effect.attack.MultiTurnMove;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import item.Item;
import item.ItemNamesies;
import item.bag.BagCategory;
import item.berry.Berry;
import item.berry.CategoryBerry;
import item.use.BallItem;
import item.use.BattleUseItem;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.species.PokemonNamesies;
import test.BaseTest;
import test.TestPokemon;

import java.util.EnumSet;
import java.util.Set;

public class ItemTest extends BaseTest {
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
        defending.assertHasEffect(PokemonEffectNamesies.INFATUATION);

        attacking.giveItem(ItemNamesies.MENTAL_HERB);
        battle.attackingFight(AttackNamesies.FLING);
        defending.assertNoEffect(PokemonEffectNamesies.INFATUATION);
        Assert.assertFalse(attacking.isHoldingItem(battle));

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
        Assert.assertFalse(defending.isHoldingItem(battle));

        Assert.assertEquals(success, defending.getActualHeldItem().namesies() == ItemNamesies.NO_ITEM);
        defending.assertEffect(success, PokemonEffectNamesies.CONSUMED_ITEM);

        // If successful, should increase Sp. Attack by one
        defending.assertStages(new TestStages().set(Stat.SP_ATTACK, success ? 1 : 0));
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
            attacking.assertStages(new TestStages().set(Stat.DEFENSE, 1));
            defending.assertNoStages();
        });

        // Power Herb is consumed and stats are raised
        powerHerbTest(AttackNamesies.GEOMANCY, true, (battle, attacking, defending) -> {
            attacking.assertFullHealth();
            defending.assertFullHealth();
            attacking.assertStages(new TestStages().set(Stat.SP_ATTACK, 2)
                                                   .set(Stat.SP_DEFENSE, 2)
                                                   .set(Stat.SPEED, 2));
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

        attacking.assertExpectedConsumedItem(battle, consumeItem);

        boolean isMultiTurn = attacking.getAttack() instanceof MultiTurnMove;
        boolean fullyExecuted = !isMultiTurn || skipCharge || consumeItem;

        // If it is a Multi-turn move and the item was not consumed, then it should still be charging
        Assert.assertEquals(attackingPP - (fullyExecuted ? 1 : 0), attacking.getMove().getPP());
        Assert.assertEquals(defendingPP - 1, defending.getMove().getPP());

        afterFirstTurn.manipulate(battle);

        battle.fight();

        attacking.assertExpectedConsumedItem(battle, consumeItem);

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
        Assert.assertTrue(defending.lastMoveSucceeded());
        attacking.assertNoStatus();

        // Lum Berry has already been consumed, so the burn should remain
        battle.defendingFight(AttackNamesies.WILL_O_WISP);
        attacking.assertHasStatus(StatusNamesies.BURNED);

        // Swap items to retrieve the Rawst Berry, which should activate to remove the burn
        battle.attackingFight(AttackNamesies.TRICK);
        attacking.assertNoStatus();
        Assert.assertFalse(attacking.isHoldingItem(battle));
        Assert.assertFalse(defending.isHoldingItem(battle));

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
        Assert.assertEquals(attacking.getHpString(), 1, attacking.getHP());
        attacking.assertNotConsumedItem(battle);
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
}
