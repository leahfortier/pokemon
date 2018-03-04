package test;

import main.Game;
import map.MapData;
import map.MapName;
import pattern.map.MapTransitionMatcher;
import trainer.player.Player;

class TestGame extends Game {
    static void setNewPlayer(Player player) {
        TestGame newGame = new TestGame();
        newGame.setPlayer(player);
        newGame.setGameData(Game.getData());

        newInstance(newGame);

        MapName startingMapName = new MapName("Depth First Search Town", "PlayersHouseUp");
        MapData startingMap = Game.getData().getMap(startingMapName);
        MapTransitionMatcher startTransition = startingMap.getEntrance("startTransition");
        player.setMap(startTransition);
        player.setPokeCenter(startTransition);
        player.setArea(startingMapName, startingMap.getArea(player.getLocation()));
    }
}
