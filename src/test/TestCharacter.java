package test;

import pokemon.ActivePokemon;
import trainer.CharacterData;

class TestCharacter extends CharacterData {
    TestCharacter(ActivePokemon mahBoiiiiiii) {
        super();
        GameTest.setPlayer(this);
        this.addPokemon(mahBoiiiiiii);
    }
}
