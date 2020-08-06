package pokemon.species;

import battle.attack.AttackNamesies;
import item.Item;
import item.ItemNamesies;
import item.hold.IncenseItem;
import map.overworld.wild.WildHoldItem;
import pokemon.ability.AbilityNamesies;
import pokemon.breeding.EggGroup;
import pokemon.evolution.EvolutionType;
import type.Type;
import util.GeneralUtils;
import util.RandomUtils;
import util.file.FileIO;
import util.file.FileName;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PokemonList implements Iterable<PokemonInfo> {
    private static PokemonList instance;

    public static PokemonList instance() {
        if (instance == null) {
            instance = new PokemonList();
        }
        return instance;
    }

    // All starters
    private final PokemonNamesies[] starterPokemon = new PokemonNamesies[] {
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
            PokemonNamesies.POPPLIO,
            PokemonNamesies.GROOKEY,
            PokemonNamesies.SCORBUNNY,
            PokemonNamesies.SOBBLE
    };

    // All baby Pokemon
    private final Set<PokemonNamesies> babyPokemon = EnumSet.of(
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
            PokemonNamesies.MANTYKE,
            PokemonNamesies.TOXEL
    );

    private final Map<Type, Set<PokemonNamesies>> pokemonTypeMap;
    private final Set<PokemonNamesies> incenseBabies;

    // Maps pokemon number to its corresponding info
    // Size NUM_POKEMON + 1, zero-index unused
    private final PokemonInfo[] map;

    // Singleton
    private PokemonList() {
        // Load all Pokemon info into map
        this.map = new PokemonInfo[PokemonInfo.NUM_POKEMON + 1];
        this.loadPokemonInfo();

        // Create map from type to list of Pokemon with that type
        pokemonTypeMap = new EnumMap<>(Type.class);
        for (Type type : Type.values()) {
            pokemonTypeMap.put(type, EnumSet.noneOf(PokemonNamesies.class));
        }
        for (PokemonInfo pokemon : this) {
            for (Type type : pokemon.getType()) {
                pokemonTypeMap.get(type).add(pokemon.namesies());
            }
        }

        // Create list of incense babies by finding all incense items
        incenseBabies = EnumSet.noneOf(PokemonNamesies.class);
        for (ItemNamesies itemNamesies : ItemNamesies.values()) {
            Item item = itemNamesies.getItem();
            if (item instanceof IncenseItem) {
                incenseBabies.add(((IncenseItem)item).getBaby());
            }
        }
    }

    // Read the Pokemon input file and create all PokemonInfo and add to the map
    private void loadPokemonInfo() {
        Scanner in = new Scanner(FileIO.readEntireFileWithReplacements(FileName.POKEMON_INFO));
        while (in.hasNext()) {
            PokemonInfo pokemonInfo = new PokemonInfo(
                    in.nextInt(),                                   // Num
                    in.nextLine().trim() + in.nextLine().trim(),    // Name
                    GeneralUtils.sixIntArray(in),                   // Base Stats
                    in.nextInt(),                                   // Base EXP
                    in.nextLine().trim() + in.nextLine().trim(),    // Growth Rate
                    Type.valueOf(in.next().trim()),                 // First Type
                    Type.valueOf(in.nextLine().trim()),             // Second Type
                    in.nextInt(),                                   // Catch Rate
                    GeneralUtils.sixIntArray(in),                   // EVs
                    EvolutionType.getEvolution(in),                 // Evolution
                    WildHoldItem.createList(in),                    // Wild Items
                    Integer.parseInt(in.nextLine()),                // Female Ratio
                    createEnumList(in, AbilityNamesies.class),      // Abilities
                    in.nextLine().trim(),                           // Classification
                    in.nextInt(),                                   // Height
                    in.nextDouble(),                                // Weight
                    in.nextLine().trim() + in.nextLine().trim(),    // Flavor Text
                    Integer.parseInt(in.nextLine()),                // Egg Steps
                    createEnumList(in, EggGroup.class),             // Egg Groups
                    createLevelUpMoves(in),                         // Level Up Moves
                    createMovesSet(in)                              // Learnable Moves
            );

            map[pokemonInfo.getNumber()] = pokemonInfo;
        }

        in.close();
    }

    private static <T extends Enum<T>> List<T> createEnumList(Scanner in, Class<T> enumType) {
        return GeneralUtils.arrayValueOf(enumType, in.nextLine().trim().split(" "));
    }

    private static List<LevelUpMove> createLevelUpMoves(Scanner in) {
        List<LevelUpMove> levelUpMoves = new ArrayList<>();
        int numMoves = in.nextInt();

        for (int i = 0; i < numMoves; i++) {
            int level = in.nextInt();
            String attackName = in.nextLine().trim();
            AttackNamesies namesies = AttackNamesies.valueOf(attackName);

            levelUpMoves.add(new LevelUpMove(level, namesies));
        }

        return levelUpMoves;
    }

    private static Set<AttackNamesies> createMovesSet(Scanner in) {
        Set<AttackNamesies> learnableMoves = EnumSet.noneOf(AttackNamesies.class);
        int numMoves = in.nextInt();
        in.nextLine();

        for (int i = 0; i < numMoves; i++) {
            String attackName = in.nextLine().trim();
            AttackNamesies namesies = AttackNamesies.valueOf(attackName);
            learnableMoves.add(namesies);
        }

        return learnableMoves;
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

    public PokemonInfo getPokemonInfo(int number) {
        return map[number];
    }

    // Shorthand static method to get the PokemonInfo for the specified number
    public static PokemonInfo get(int number) {
        return instance().getPokemonInfo(number);
    }

    @Override
    public Iterator<PokemonInfo> iterator() {
        return new PokemonIterator();
    }

    public Stream<PokemonInfo> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    private class PokemonIterator implements Iterator<PokemonInfo> {
        // Pokemon numbers are 1-indexed
        private int index = 1;

        @Override
        public boolean hasNext() {
            return index <= PokemonInfo.NUM_POKEMON;
        }

        @Override
        public PokemonInfo next() {
            return getPokemonInfo(index++);
        }
    }
}
