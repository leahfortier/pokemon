package test;

import main.Game;
import trainer.player.Player;

class GameTest extends Game {
    static void setNewPlayer(Player player) {
        GameTest newGame = new GameTest();
        newGame.setPlayer(player);
        newGame.setGameData(Game.getData());

        newInstance(newGame);
    }
}
