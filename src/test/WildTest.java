package test;

import battle.ActivePokemon;
import item.ItemNamesies;
import main.Game;
import map.overworld.WildEncounter;
import map.overworld.WildHoldItem;
import org.junit.Assert;
import org.junit.Test;
import pokemon.species.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import trainer.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WildTest extends BaseTest {
    @Test
    public void compoundEyesTest() {
        compoundEyesTest(PokemonNamesies.VULPIX, true, true);
        compoundEyesTest(PokemonNamesies.LINOONE, false, true);
        compoundEyesTest(PokemonNamesies.CLEFAIRY, false, false);
    }

    private void compoundEyesTest(PokemonNamesies pokemon, boolean alwaysItem, boolean compoundEyesAlwaysItem) {
        Player player = new TestCharacter(TestPokemon.newPlayerPokemon(PokemonNamesies.BULBASAUR));
        Assert.assertTrue(Game.getPlayer() == player);

        Set<ItemNamesies> wildHoldItems = pokemon
                .getInfo()
                .getWildItems()
                .stream()
                .map(WildHoldItem::getItem)
                .collect(Collectors.toSet());

        Set<ItemNamesies> noAbilitySet = new HashSet<>(wildHoldItems);
        if (!alwaysItem) {
            noAbilitySet.add(ItemNamesies.NO_ITEM);
        }

        Set<ItemNamesies> compoundEyesSet = new HashSet<>(wildHoldItems);
        if (!compoundEyesAlwaysItem) {
            compoundEyesSet.add(ItemNamesies.NO_ITEM);
        }

        for (int i = 0; i < 1000; i++) {
            checkItem(player, pokemon, AbilityNamesies.NO_ABILITY, wildHoldItems, noAbilitySet, alwaysItem);
            checkItem(player, pokemon, AbilityNamesies.COMPOUNDEYES, wildHoldItems, compoundEyesSet, compoundEyesAlwaysItem);
        }

        Assert.assertTrue(noAbilitySet.toString(), noAbilitySet.isEmpty());
        Assert.assertTrue(compoundEyesSet.toString(), compoundEyesSet.isEmpty());
    }

    private void checkItem(Player player,
                           PokemonNamesies pokemon,
                           AbilityNamesies ability,
                           Set<ItemNamesies> allItems,
                           Set<ItemNamesies> unseen,
                           boolean alwaysItem) {
        player.front().setAbility(ability);

        WildEncounter wildEncounter = new WildEncounter(pokemon, 5);
        ActivePokemon wildPokemon = wildEncounter.getWildPokemon().front();

        ItemNamesies holdItem = wildPokemon.getActualHeldItem().namesies();
        unseen.remove(holdItem);
        Assert.assertTrue(
                holdItem.name(),
                allItems.contains(holdItem) || (!alwaysItem && holdItem == ItemNamesies.NO_ITEM)
        );
    }
}
