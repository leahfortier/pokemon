package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;

class ItemEvolution extends BaseEvolution {
    private static final long serialVersionUID = 1L;

    private final ItemNamesies item;

    ItemEvolution(int num, String item) {
        super(EvolutionMethod.ITEM, num);

        this.item = ItemNamesies.getValueOf(item);
    }

    @Override
    public Evolution getEvolution(ActivePokemon toEvolve, ItemNamesies useItem) {
        if (useItem == item) {
            return this;
        }

        return null;
    }

    @Override
    public String toString() {
        return EvolutionType.ITEM + " " + super.evolutionNumber + " " + this.item;
    }
}
