package test.gui;

import gui.view.PartyView;
import gui.view.PokedexView;
import gui.view.battle.handler.BagState;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonList;
import test.general.BaseTest;
import test.general.TestGame;

import java.awt.Graphics;

public class WrapTest extends BaseTest {
    @Test
    public void pokedexFlavorTextTest() {
        PokedexView pokedexView = TestGame.instance().getPokedexView();
        Assert.assertNotNull(pokedexView);

        Graphics g = new TestGraphics();
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            Assert.assertTrue(pokemonInfo.getName(), pokedexView.drawFlavorText(g, pokemonInfo));
        }
    }

    @Test
    public void partyAbilityTextTest() {
        PartyView partyView = TestGame.instance().getPartyView();
        Assert.assertNotNull(partyView);

        Graphics g = new TestGraphics();
        for (AbilityNamesies abilityNamesies : AbilityNamesies.values()) {
            Assert.assertTrue(abilityNamesies.getName(), partyView.drawAbility(g, abilityNamesies.getNewAbility()));
        }
    }

    @Test
    public void battleItemDescriptionTest() {
        BagState bagState = new BagState();
        Graphics g = new TestGraphics();
        for (ItemNamesies itemNamesies : ItemNamesies.values()) {
            if (itemNamesies.getItem().hasBattleBagCategories()) {
                Assert.assertTrue(itemNamesies.getName(), bagState.drawItemDescription(g, itemNamesies));
            }
        }
    }
}
