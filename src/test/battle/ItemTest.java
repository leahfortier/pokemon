package test.battle;

import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import test.BaseTest;
import test.TestPokemon;

public class ItemTest extends BaseTest {
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
