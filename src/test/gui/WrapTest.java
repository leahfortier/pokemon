package test.gui;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import draw.panel.LearnMovePanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.view.MoveRelearnerView;
import gui.view.NewPokemonView;
import gui.view.PartyView;
import gui.view.PokedexView;
import gui.view.battle.handler.BagState;
import gui.view.battle.handler.FightState;
import gui.view.item.BagLayout;
import item.Item;
import item.ItemNamesies;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ability.AbilityNamesies;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonList;
import pokemon.species.PokemonNamesies;
import test.general.BaseTest;
import test.general.TestGame;
import test.pokemon.TestPokemon;

import java.awt.Graphics;

public class WrapTest extends BaseTest {
    @Test
    public void flavorTextTest() {
        // Tests the flavor text inside the Pokedex view and inside the new Pokemon view
        PokedexView pokedexView = TestGame.instance().getPokedexView();
        NewPokemonView newPokemonView = TestGame.instance().getNewPokemonView();

        TestMetrics pokedexMetrics = new TestMetrics();
        TestMetrics newPokemonMetrics = new TestMetrics();

        Graphics g = new TestGraphics();
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            String name = pokemonInfo.getName();
            pokedexMetrics.checkMetrics(name, pokedexView.drawFlavorText(g, pokemonInfo));
            newPokemonMetrics.checkMetrics(name, newPokemonView.drawInfoLabels(g, pokemonInfo));
        }

        pokedexMetrics.confirmFontSize(16);
        newPokemonMetrics.confirmFontSize(22);
    }

    @Test
    public void partyAbilityTextTest() {
        PartyView partyView = TestGame.instance().getPartyView();
        TestMetrics metrics = new TestMetrics();

        Graphics g = new TestGraphics();
        for (AbilityNamesies abilityNamesies : AbilityNamesies.values()) {
            metrics.checkMetrics(abilityNamesies.getName(), partyView.drawAbility(g, abilityNamesies.getNewAbility()));
        }

        metrics.confirmFontSizes(12, 16);
    }

    @Test
    public void itemDescriptionTest() {
        BagLayout bagLayout = new BagLayout(true);
        BagState battleBagState = new BagState();

        TestMetrics bagMetrics = new TestMetrics();
        TestMetrics battleMetrics = new TestMetrics();

        Graphics g = new TestGraphics();
        for (ItemNamesies itemNamesies : ItemNamesies.values()) {
            Item item = itemNamesies.getItem();
            String name = item.getName();

            // Bag, Mart, Sell, and (almost) Berry views use this
            WrapMetrics metrics = bagLayout.drawSelectedItem(g, itemNamesies);
            if (itemNamesies == ItemNamesies.NO_ITEM) {
                Assert.assertNull(name, metrics);
            } else {
                bagMetrics.checkMetrics(name, metrics);
            }


            // Battle bag item description
            if (item.hasBattleBagCategories()) {
                battleMetrics.checkMetrics(name, battleBagState.drawItemDescription(g, itemNamesies));
            }
        }

        bagMetrics.confirmFontSizes(11, 14);
        battleMetrics.confirmFontSize(13);
    }

    @Test
    public void movePanelTest() {
        FightState fightState = new FightState();
        PartyView partyView = TestGame.instance().getPartyView();
        MoveRelearnerView moveRelearnerView = TestGame.instance().getMoveRelearnerView();

        TestMetrics fightMetrics = new TestMetrics();
        TestMetrics partyMetrics = new TestMetrics();
        TestMetrics moveRelearnerMetrics = new TestMetrics();
        TestMetrics learnMoveMetrics = new TestMetrics();

        Graphics g = new TestGraphics();
        for (AttackNamesies attackNamesies : AttackNamesies.values()) {
            Attack attack = attackNamesies.getNewAttack();
            String name = attack.getName();

            // Selected move details on fight screen in battle
            fightMetrics.checkMetrics(name, fightState.drawMoveDetails(g, attack));

            // Selected move details when viewing Pokemon in party
            partyMetrics.checkMetrics(name, partyView.drawMoveDetails(g, attack));

            // Selected move details when relearning a move
            moveRelearnerMetrics.checkMetrics(name, moveRelearnerView.drawMoveDetails(g, attack));

            // Selected move details when learning a move
            TestPokemon pokemon = TestPokemon.newPlayerPokemon(PokemonNamesies.BULBASAUR);
            LearnMovePanel learnMovePanel = new LearnMovePanel(pokemon, new Move(attackNamesies));
            learnMoveMetrics.checkMetrics(name, learnMovePanel.drawMoveDetails(g, attack));
        }

        fightMetrics.confirmFontSizes(13, 16);
        partyMetrics.confirmFontSizes(14, 16);
        moveRelearnerMetrics.confirmFontSizes(15, 16);
        learnMoveMetrics.confirmFontSizes(13, 16);
    }

    private static class TestMetrics {
        private int minFontSize;
        private int maxFontSize;

        public TestMetrics() {
            this.minFontSize = 100;
            this.maxFontSize = 0;
        }

        public void checkMetrics(String message, WrapMetrics metrics) {
            int fontSize = metrics.getFontSize();
            this.minFontSize = Math.min(this.minFontSize, fontSize);
            this.maxFontSize = Math.max(this.maxFontSize, fontSize);
            Assert.assertTrue(message, metrics.fits());
        }

        public void confirmFontSize(int expectedSize) {
            this.confirmFontSizes(expectedSize, expectedSize);
        }

        public void confirmFontSizes(int expectedMin, int expectedMax) {
            String message = this.minFontSize + " " + this.maxFontSize;
            Assert.assertEquals(message, expectedMin, this.minFontSize);
            Assert.assertEquals(message, expectedMax, this.maxFontSize);
        }
    }
}
