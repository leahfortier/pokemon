package test;

import pokemon.ActivePokemon;
import trainer.Player;

class TestCharacter extends Player {
    TestCharacter(ActivePokemon mahBoiiiiiii) {
        super();
        GameTest.setNewPlayer(this);
        this.addPokemon(mahBoiiiiiii);
    }
}
