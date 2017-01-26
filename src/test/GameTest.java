package test;

import main.Game;
import trainer.CharacterData;

class GameTest extends Game {
    static void setPlayer(CharacterData characterData) {
        GameTest newGame = new GameTest();
        newGame.setCharacterData(characterData);
        newInstance(newGame);
    }
}
