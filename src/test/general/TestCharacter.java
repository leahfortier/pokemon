package test.general;

import battle.ActivePokemon;
import pokemon.species.PokemonNamesies;
import test.pokemon.TestPokemon;
import trainer.player.Player;

public class TestCharacter extends Player {
    private static final long serialVersionUID = 1L;

    public TestCharacter() {
        this(TestPokemon.newPlayerPokemon(PokemonNamesies.BULBASAUR));
    }

    public TestCharacter(ActivePokemon mahBoiiiiiii) {
        super();
        TestGame.setNewPlayer(this, mahBoiiiiiii);
    }
}
