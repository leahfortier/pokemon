package pokemon.species;

import item.Item;
import item.ItemNamesies;
import item.hold.IncenseItem;
import type.Type;
import util.RandomUtils;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PokemonList implements Iterable<PokemonInfo> {
    private static PokemonList instance;

    public static PokemonList instance() {
        if (instance == null) {
            instance = new PokemonList();
        }
        return instance;
    }

    // All starters
    private static final PokemonNamesies[] starterPokemon = new PokemonNamesies[] {
            PokemonNamesies.BULBASAUR,
            PokemonNamesies.CHARMANDER,
            PokemonNamesies.SQUIRTLE,
            PokemonNamesies.CHIKORITA,
            PokemonNamesies.CYNDAQUIL,
            PokemonNamesies.TOTODILE,
            PokemonNamesies.TREECKO,
            PokemonNamesies.TORCHIC,
            PokemonNamesies.MUDKIP,
            PokemonNamesies.TURTWIG,
            PokemonNamesies.CHIMCHAR,
            PokemonNamesies.PIPLUP,
            PokemonNamesies.SNIVY,
            PokemonNamesies.TEPIG,
            PokemonNamesies.OSHAWOTT,
            PokemonNamesies.CHESPIN,
            PokemonNamesies.FENNEKIN,
            PokemonNamesies.FROAKIE,
            PokemonNamesies.ROWLET,
            PokemonNamesies.LITTEN,
            PokemonNamesies.POPPLIO
    };

    private static final Set<PokemonNamesies> babyPokemon = EnumSet.of(
            PokemonNamesies.PICHU,
            PokemonNamesies.CLEFFA,
            PokemonNamesies.IGGLYBUFF,
            PokemonNamesies.TOGEPI,
            PokemonNamesies.TYROGUE,
            PokemonNamesies.SMOOCHUM,
            PokemonNamesies.ELEKID,
            PokemonNamesies.MAGBY,
            PokemonNamesies.AZURILL,
            PokemonNamesies.WYNAUT,
            PokemonNamesies.BUDEW,
            PokemonNamesies.CHINGLING,
            PokemonNamesies.BONSLY,
            PokemonNamesies.MIME_JR,
            PokemonNamesies.HAPPINY,
            PokemonNamesies.MUNCHLAX,
            PokemonNamesies.RIOLU,
            PokemonNamesies.MANTYKE
    );

    private final Map<Type, Set<PokemonNamesies>> pokemonTypeMap;
    private final Set<PokemonNamesies> incenseBabies;

    // Singleton
    private PokemonList() {
        pokemonTypeMap = new EnumMap<>(Type.class);
        for (Type type : Type.values()) {
            pokemonTypeMap.put(type, EnumSet.noneOf(PokemonNamesies.class));
        }

        for (PokemonInfo pokemon : this) {
            for (Type type : pokemon.getType()) {
                pokemonTypeMap.get(type).add(pokemon.namesies());
            }
        }

        incenseBabies = EnumSet.noneOf(PokemonNamesies.class);
        for (ItemNamesies itemNamesies : ItemNamesies.values()) {
            Item item = itemNamesies.getItem();
            if (item instanceof IncenseItem) {
                incenseBabies.add(((IncenseItem)item).getBaby());
            }
        }
    }

    public PokemonNamesies getRandomStarterPokemon() {
        return RandomUtils.getRandomValue(starterPokemon);
    }

    public boolean isIncenseBaby(PokemonNamesies namesies) {
        return incenseBabies.contains(namesies);
    }

    public boolean isBabyPokemon(PokemonNamesies namesies) {
        return babyPokemon.contains(namesies);
    }

    public int getNumBabyPokemon() {
        return babyPokemon.size();
    }

    public Set<PokemonNamesies> getAllBabyPokemon() {
        return EnumSet.copyOf(babyPokemon);
    }

    public int getNumTypedPokemon(Type type) {
        return pokemonTypeMap.get(type).size();
    }

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
