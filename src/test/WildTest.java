package test;

import item.ItemNamesies;
import main.Game;
import map.overworld.WildEncounter;
import map.overworld.WildHoldItem;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import trainer.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WildTest {
    @Test
    public void compoundEyesTest() {
        compoundEyesTest(PokemonNamesies.VULPIX, true, true);
        compoundEyesTest(PokemonNamesies.LINOONE, false, true);
        compoundEyesTest(PokemonNamesies.CLEFAIRY, false, false);
    }

    private void compoundEyesTest(PokemonNamesies pokemonNamesies, boolean alwaysItem, boolean compoundEyesAlwaysItem) {
        WildEncounter wildEncounter = new WildEncounter(pokemonNamesies, 5);

        Player player = new TestCharacter(new TestPokemon(PokemonNamesies.BULBASAUR));
        Assert.assertTrue(Game.getPlayer() == player);

        Set<ItemNamesies> wildHoldItems = PokemonInfo.getPokemonInfo(pokemonNamesies)
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
            player.front().setAbility(AbilityNamesies.NO_ABILITY);
            ActivePokemon wildPokemon = wildEncounter.getWildPokemon().front();
            ItemNamesies holdItem = wildPokemon.getActualHeldItem().namesies();
            noAbilitySet.remove(holdItem);
            Assert.assertTrue(
                    holdItem.name(),
                    wildHoldItems.contains(holdItem) || (!alwaysItem && holdItem == ItemNamesies.NO_ITEM)
            );

            player.front().setAbility(AbilityNamesies.COMPOUNDEYES);
            wildPokemon = wildEncounter.getWildPokemon().front();
            holdItem = wildPokemon.getActualHeldItem().namesies();
            compoundEyesSet.remove(holdItem);
            Assert.assertTrue(
                    holdItem.name(),
                    wildHoldItems.contains(holdItem) || (!compoundEyesAlwaysItem && holdItem == ItemNamesies.NO_ITEM)
            );
        }

        Assert.assertTrue(noAbilitySet.toString(), noAbilitySet.isEmpty());
        Assert.assertTrue(compoundEyesSet.toString(), compoundEyesSet.isEmpty());
    }
}
