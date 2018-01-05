package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;

class ItemEvolution extends BaseEvolution {
    private static final long serialVersionUID = 1L;
    
    private final ItemNamesies item;
    
    ItemEvolution(String namesies, String item) {
        super(EvolutionMethod.ITEM, namesies);
        
        this.item = ItemNamesies.getValueOf(item);
    }
    
    @Override
    public BaseEvolution getEvolution(ActivePokemon toEvolve, ItemNamesies useItem) {
        if (useItem == item) {
            return this;
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return EvolutionType.ITEM + " " + super.getEvolution().namesies() + " " + this.item;
    }
    
    @Override
    public String getString() {
        return "Use " + item.getName();
    }
}
