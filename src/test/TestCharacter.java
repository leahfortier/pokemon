package test;

import map.MapName;
import pattern.SimpleMapTransition;
import pokemon.ActivePokemon;
import trainer.player.Player;

class TestCharacter extends Player {
    TestCharacter(ActivePokemon mahBoiiiiiii) {
        super();
        GameTest.setNewPlayer(this);
        this.addPokemon(mahBoiiiiiii);
        this.setMap(new SimpleMapTransition(new MapName("Depth First Search Town", "PlayersHouseUp"), "GameStartLocation"));
    }
}
