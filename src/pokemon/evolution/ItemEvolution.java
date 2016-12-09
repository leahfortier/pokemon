package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;

class ItemEvolution extends Evolution implements BaseEvolution {
    private static final long serialVersionUID = 1L;

    private int evolutionNumber;
    private ItemNamesies item;

    ItemEvolution(int num, String item) {
        this.evolutionNumber = num;

        this.item = ItemNamesies.getValueOf(item);
    }

    @Override
    public PokemonInfo getEvolution() {
        return PokemonInfo.getPokemonInfo(evolutionNumber);
    }

    @Override
    public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, ItemNamesies use) {
        if (type != EvolutionCheck.ITEM) {
            return null;
        }

        if (use == item) {
            return this;
        }

        return null;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return new PokemonNamesies[] { PokemonInfo.getPokemonInfo(evolutionNumber).namesies() };
    }
}
