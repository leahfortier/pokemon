package test.general;

import battle.ActivePokemon;
import trainer.player.Player;

public class TestCharacter extends Player {
    private static final long serialVersionUID = 1L;

    public TestCharacter(ActivePokemon mahBoiiiiiii) {
        super();
        TestGame.setNewPlayer(this, mahBoiiiiiii);
    }
}
