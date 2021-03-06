package map.triggers;

import main.Game;
import pattern.PokemonMatcher;
import trainer.TrainerType;

public class GivePokemonTrigger extends Trigger {
    private final PokemonMatcher pokemonMatcher;

    public GivePokemonTrigger(PokemonMatcher matcher) {
        this.pokemonMatcher = matcher;
    }

    @Override
    public void execute() {
        Game.getPlayer().addPokemon(this.pokemonMatcher.createPokemon(TrainerType.PLAYER));
    }
}
