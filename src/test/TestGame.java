package test;

import main.Game;
import trainer.player.Player;

class TestGame extends Game {
    static void setNewPlayer(Player player) {
        TestGame newGame = new TestGame();
        newGame.setPlayer(player);
        newGame.setGameData(Game.getData());
        
        newInstance(newGame);
    }
}
