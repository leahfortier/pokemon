package test.general;

import battle.ActivePokemon;
import gui.view.PartyView;
import gui.view.PokedexView;
import gui.view.ViewMode;
import main.Game;
import map.MapData;
import map.MapName;
import org.junit.Assert;
import pattern.map.MapTransitionMatcher;
import pokemon.species.PokemonNamesies;
import test.pokemon.TestPokemon;
import trainer.player.Player;

public class TestGame extends Game {
    public static TestGame instance() {
        Game instance = Game.instance();
        Assert.assertTrue(instance instanceof TestGame);
        return (TestGame)instance;
    }

    public PokedexView getPokedexView() {
        return (PokedexView)super.getView(ViewMode.POKEDEX_VIEW);
    }

    public PartyView getPartyView() {
        return (PartyView)super.getView(ViewMode.PARTY_VIEW);
    }

    static void setNewPlayer() {
        setNewPlayer(new Player(), TestPokemon.newPlayerPokemon(PokemonNamesies.BULBASAUR));
    }

    static void setNewPlayer(Player player, ActivePokemon mahBoiiiiiii) {
        TestGame newGame = new TestGame();
        newGame.setPlayer(player);
        newGame.setGameData(Game.getData());

        newInstance(newGame);
        player.addPokemon(mahBoiiiiiii);
        newGame.setViews();

        MapName startingMapName = new MapName("Depth First Search Town", "PlayersHouseUp");
        MapData startingMap = Game.getData().getMap(startingMapName);
        MapTransitionMatcher startTransition = startingMap.getEntrance("startTransition");
        player.setMap(startTransition);
        player.setPokeCenter(startTransition);
        player.setArea(startingMapName, startingMap.getArea(player.getLocation()));
    }
}
