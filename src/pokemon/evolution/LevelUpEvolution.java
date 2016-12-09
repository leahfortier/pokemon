package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;

class LevelUpEvolution extends Evolution implements BaseEvolution {
    private static final long serialVersionUID = 1L;

    private int evolutionNumber;
    private int level;

    LevelUpEvolution(int num, int level) {
        this.evolutionNumber = num;
        this.level = level;
    }

    @Override
    public PokemonInfo getEvolution() {
        return PokemonInfo.getPokemonInfo(evolutionNumber);
    }

    @Override
    public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, ItemNamesies use) {
        if (type != EvolutionCheck.LEVEL) {
            return null;
        }

        if (p.getLevel() >= level) {
            return this;
        }

        return null;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return new PokemonNamesies[] { PokemonInfo.getPokemonInfo(evolutionNumber).namesies() };
    }
}
