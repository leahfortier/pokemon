package test;

import main.Game;
import map.MapName;
import pattern.map.MapTransitionMatcher;
import trainer.player.Player;

class TestGame extends Game {
    static void setNewPlayer(Player player) {
        TestGame newGame = new TestGame();
        newGame.setPlayer(player);
        newGame.setGameData(Game.getData());

        newInstance(newGame);

        MapName startingMap = new MapName("Depth First Search Town", "PlayersHouseUp");
        MapTransitionMatcher startTransition = Game.getData().getMap(startingMap).getEntrance("startTransition");
        player.setMap(startTransition);
        player.setPokeCenter(startTransition);
    }
}
