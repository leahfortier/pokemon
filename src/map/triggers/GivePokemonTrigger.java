package map.triggers;

import main.Game;
import pokemon.ActivePokemon;

public class GivePokemonTrigger extends Trigger {
    private final ActivePokemon pokemon;

    public GivePokemonTrigger(String name, String contents) {
        super(name, contents);

        this.pokemon = ActivePokemon.createActivePokemon(contents, true);
    }

    public void execute() {
        super.execute();
        Game.getPlayer().addPokemon(null, pokemon);
    }
}
