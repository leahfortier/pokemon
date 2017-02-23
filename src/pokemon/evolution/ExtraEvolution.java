package pokemon.evolution;

import item.ItemNamesies;
import main.Game;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;

// SHEDDDIIINNNJJJAAAAAA
public class ExtraEvolution extends Evolution {
    private static final long serialVersionUID = 1L;

    private final BaseEvolution evolution;
    private final PokemonNamesies extraPokemon;
    private final int extraLevel;

    ExtraEvolution(String extraPokemon, String extraLevel, BaseEvolution evolution) {
        this.evolution = evolution;
        this.extraPokemon = PokemonNamesies.valueOf(extraPokemon);
        this.extraLevel = Integer.parseInt(extraLevel);
    }

    @Override
    public BaseEvolution getEvolution(EvolutionMethod type, ActivePokemon pokemon, ItemNamesies use) {
        BaseEvolution baseEvolution = this.evolution.getEvolution(type, pokemon, use);
        if (baseEvolution == null) {
            return null;
        }

        // If an evolution will indeed occur, add the extra Pokemon
        Game.getPlayer().addPokemon(new ActivePokemon(extraPokemon, extraLevel, false, true), false);
        return baseEvolution;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return this.evolution.getEvolutions();
    }

    @Override
    public String toString() {
        return EvolutionType.EXTRA + " " + extraPokemon.name() + " " + evolution.toString();
    }
}
