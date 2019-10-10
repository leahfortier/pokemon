package pokemon.species;

import java.util.Iterator;

public class PokemonList implements Iterable<PokemonInfo> {
    private static PokemonList instance;

    public static PokemonList instance() {
        if (instance == null) {
            instance = new PokemonList();
        }
        return instance;
    }

    // Singleton
    private PokemonList() {}

    @Override
    public Iterator<PokemonInfo> iterator() {
        return new PokemonIterator();
    }

    private static class PokemonIterator implements Iterator<PokemonInfo> {
        // Pokemon numbers are 1-indexed
        private int index = 1;

        @Override
        public boolean hasNext() {
            return index <= PokemonInfo.NUM_POKEMON;
        }

        @Override
        public PokemonInfo next() {
            return PokemonInfo.getPokemonInfo(index++);
        }
    }
}
