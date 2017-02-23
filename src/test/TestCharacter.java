package test;

import pokemon.ActivePokemon;
import trainer.Player;

class TestCharacter extends Player {
    TestCharacter(ActivePokemon mahBoiiiiiii) {
        super();
        GameTest.setPlayer(this);
        this.addPokemon(mahBoiiiiiii);
    }
}
