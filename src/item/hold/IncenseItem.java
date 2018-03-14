package item.hold;

import pokemon.species.PokemonNamesies;

public interface IncenseItem extends HoldItem {
    // Huggies and kissies for mommy and daddy
    PokemonNamesies getBaby();

    @Override
    default int flingDamage() {
        return 10;
    }
}
