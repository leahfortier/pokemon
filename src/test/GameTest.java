package test;

import main.Game;
import trainer.Player;

class GameTest extends Game {
    static void setPlayer(Player player) {
        GameTest newGame = new GameTest();
        newGame.setPlayer(player);
        newGame.setGameData(Game.getData());

        newInstance(newGame);
    }
}
