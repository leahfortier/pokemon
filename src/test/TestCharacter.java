package test;

import battle.ActivePokemon;
import map.MapName;
import pattern.SimpleMapTransition;
import trainer.player.Player;

public class TestCharacter extends Player {
    public TestCharacter(ActivePokemon mahBoiiiiiii) {
        super();
        TestGame.setNewPlayer(this);
        this.addPokemon(mahBoiiiiiii);
        this.setMap(new SimpleMapTransition(new MapName("Depth First Search Town", "PlayersHouseUp"), "GameStartLocation"));
    }
}
