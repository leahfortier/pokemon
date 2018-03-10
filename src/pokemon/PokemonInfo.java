package pokemon;

import battle.attack.AttackNamesies;
import item.Item;
import item.ItemNamesies;
import item.hold.IncenseItem;
import map.overworld.WildHoldItem;
import pokemon.ability.AbilityNamesies;
import pokemon.breeding.EggGroup;
import pokemon.evolution.Evolution;
import pokemon.evolution.EvolutionType;
import type.PokeType;
import type.Type;
import util.FileIO;
import util.FileName;
import util.GeneralUtils;
import util.RandomUtils;
import util.serialization.Serializable;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class PokemonInfo implements Serializable, Comparable<PokemonInfo> {
    private static final long serialVersionUID = 1L;

    public static final int NUM_POKEMON = 823;
    public static final int EVOLUTION_LEVEL_LEARNED = -1;

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

    private static final PokemonNamesies[] babyPokemon = new PokemonNamesies[] {
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
    };

    private static final Map<Type, Set<PokemonNamesies>> pokemonTypeMap = new EnumMap<>(Type.class);

    private static Map<Integer, PokemonInfo> map;
    private static Set<PokemonNamesies> incenseBabies;

    static {
        for (Type type : Type.values()) {
            pokemonTypeMap.put(type, EnumSet.noneOf(PokemonNamesies.class));
        }

        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemon = PokemonInfo.getPokemonInfo(i);
            for (Type type : pokemon.getType()) {
                pokemonTypeMap.get(type).add(pokemon.namesies());
            }
        }
    }

    private final int number;
    private final PokemonNamesies namesies;
    private final int[] baseStats;
    private final int baseExp;
    private final GrowthRate growthRate;
    private final PokeType type;
    private final List<LevelUpMove> levelUpMoves;
    private final Set<AttackNamesies> learnableMoves;
    private final int catchRate;
    private final int[] givenEVs;
    private final Evolution evolution;
    private final List<WildHoldItem> wildHoldItems;
    private final AbilityNamesies[] abilities;
    private final int maleRatio;
    private final String classification;
    private final int height;
    private final double weight;
    private final String flavorText;
    private final int eggSteps;
    private final EggGroup[] eggGroups;

    private PokemonInfo(int number,
                        String name,
                        int[] baseStats,
                        int baseExp,
                        String growthRate,
                        Type firstType,
                        Type secondType,
                        int catchRate,
                        int[] givenEVs,
                        Evolution evolution,
                        List<WildHoldItem> wildHoldItems,
                        int genderRatio,
                        List<AbilityNamesies> abilities,
                        String classification,
                        int height,
                        double weight,
                        String flavorText,
                        int eggSteps,
                        List<EggGroup> eggGroups,
                        List<LevelUpMove> levelUpMoves,
                        Set<AttackNamesies> learnableMoves) {
        this.number = number;
        this.namesies = PokemonNamesies.getValueOf(name);
        this.baseStats = baseStats;
        this.baseExp = baseExp;
        this.growthRate = GrowthRate.valueOf(growthRate);
        this.type = new PokeType(firstType, secondType);
        this.levelUpMoves = levelUpMoves;
        this.learnableMoves = EnumSet.copyOf(learnableMoves);
        this.catchRate = catchRate;
        this.givenEVs = givenEVs;
        this.evolution = evolution;
        this.wildHoldItems = wildHoldItems;
        this.abilities = abilities.toArray(new AbilityNamesies[0]);
        this.maleRatio = genderRatio;
        this.classification = classification;
        this.height = height;
        this.weight = weight;
        this.flavorText = flavorText;
        this.eggSteps = eggSteps;
        this.eggGroups = eggGroups.toArray(new EggGroup[0]);
    }

    public boolean isType(Type type) {
        return this.type.isType(type);
    }

    public PokeType getType() {
        return type;
    }

    public List<LevelUpMove> getLevelUpMoves() {
        return levelUpMoves;
    }

    public int getStat(int index) {
        return baseStats[index];
    }

    public GrowthRate getGrowthRate() {
        return growthRate;
    }

    public int getEggSteps() {
        return eggSteps;
    }

    public String getAbilitiesString() {
        return abilities[0].getName() +
                (abilities[1] == AbilityNamesies.NO_ABILITY
                        ? StringUtils.empty()
                        : ", " + abilities[1].getName()
                );
    }

    public AbilityNamesies[] getAbilities() {
        return abilities;
    }

    public boolean hasAbility(AbilityNamesies s) {
        return abilities[0] == s || abilities[1] == s;
    }

    public int getCatchRate() {
        return catchRate;
    }

    public int getBaseEXP() {
        return baseExp;
    }

    public int getGivenEV(int index) {
        return givenEVs[index];
    }

    public int[] getGivenEVs() {
        return givenEVs;
    }

    public int getMaleRatio() {
        return maleRatio;
    }

    public String getHeightString() {
        return String.format("%d'%02d\"", height/12, height%12);
    }

    public double getWeight() {
        return weight;
    }

    public String getClassification() {
        return classification;
    }

    public String getFlavorText() {
        return flavorText;
    }

    public PokemonNamesies namesies() {
        return namesies;
    }

    public String getName() {
        return namesies.getName();
    }

    public int getNumber() {
        return number;
    }

    public String getBaseImageName() {
        return String.format("%03d", number);
    }

    public String getTinyImageName() {
        return this.getBaseImageName() + "-small";
    }

    public Evolution getEvolution() {
        return evolution;
    }

    public List<WildHoldItem> getWildItems() {
        return wildHoldItems;
    }

    public String getImageName() {
        return this.getImageName(false, true);
    }

    public String getImageName(boolean shiny) {
        return this.getImageName(shiny, true);
    }

    public String getImageName(boolean shiny, boolean front) {
        return getImageName(this, shiny, front, false);
    }

    public String getImageName(boolean shiny, boolean front, boolean form) {
        return getImageName(this, shiny, front, form);
    }

    @Override
    public int compareTo(PokemonInfo p) {
        return number - p.number;
    }

    public List<AttackNamesies> getMoves(int level) {
        return levelUpMoves.stream()
                           .filter(entry -> entry.getLevel() == level)
                           .map(LevelUpMove::getMove)
                           .collect(Collectors.toList());
    }

    public boolean canBreed() {
        return eggGroups[0] != EggGroup.UNDISCOVERED;
    }

    public PokemonNamesies getBaseEvolution() {
        return getBaseEvolution(this);
    }

    public EggGroup[] getEggGroups() {
        return eggGroups;
    }

    public boolean isIncenseBaby() {
        if (incenseBabies == null) {
            loadIncenseBabies();
        }

        return incenseBabies.contains(namesies);
    }

    // Returns what level the Pokemon will learn the given attack, returns null if they cannot learn it by level up
    public Integer levelLearned(AttackNamesies attack) {
        for (LevelUpMove entry : this.levelUpMoves) {
            if (entry.getMove() == attack) {
                return entry.getLevel();
            }
        }

        return null;
    }

    public boolean canLearnMove(AttackNamesies attack) {
        return levelLearned(attack) != null || canLearnByBreeding(attack);
    }

    public boolean canLearnByBreeding(AttackNamesies attack) {
        return this.learnableMoves.contains(attack);
    }

    private static String getImageName(PokemonInfo pokemonInfo, boolean shiny, boolean front, boolean form) {
        String imageName = pokemonInfo.getBaseImageName();
        if (form) {
            imageName += "b";
        }
        if (shiny) {
            imageName += "-shiny";
        }
        if (!front) {
            imageName += "-back";
        }

        return imageName;
    }

    public static PokemonInfo getPokemonInfo(int index) {
        if (map == null) {
            loadPokemonInfo();
        }

        return map.get(index);
    }

    // Create and load the Pokemon info map if it doesn't already exist
    public static void loadPokemonInfo() {
        if (map != null) {
            return;
        }

        map = new HashMap<>();

        Scanner in = new Scanner(FileIO.readEntireFileWithReplacements(FileName.POKEMON_INFO, false));
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
                    Integer.parseInt(in.nextLine()),                // Male Ratio
                    createEnumList(in, AbilityNamesies.class),      // Abilities
                    in.nextLine().trim(),                           // Classification
                    in.nextInt(),                                   // Height
                    in.nextDouble(),                                // Weight
                    in.nextLine().trim(),                           // Flavor Text
                    Integer.parseInt(in.nextLine()),                // Egg Steps
                    createEnumList(in, EggGroup.class),             // Egg Groups
                    createLevelUpMoves(in),                         // Level Up Moves
                    createMovesSet(in)                              // Learnable Moves
            );

            map.put(pokemonInfo.getNumber(), pokemonInfo);
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

    // TODO: Instead of generating this on the fly should just be added to the text file and stored
    private static PokemonNamesies getBaseEvolution(PokemonInfo targetPokes) {
        if (targetPokes.namesies() == PokemonNamesies.MANAPHY) {
            return PokemonNamesies.PHIONE;
        }

        if (targetPokes.namesies() == PokemonNamesies.SHEDINJA) {
            return PokemonNamesies.NINCADA;
        }

        Set<PokemonNamesies> allPokes = EnumSet.complementOf(EnumSet.of(PokemonNamesies.NONE));

        while (true) {
            boolean changed = false;
            for (PokemonNamesies pokesName : allPokes) {
                PokemonInfo pokes = map.get(pokesName.ordinal());
                PokemonNamesies[] evolutionNamesies = pokes.getEvolution().getEvolutions();
                for (PokemonNamesies namesies : evolutionNamesies) {
                    if (namesies == targetPokes.namesies()) {
                        targetPokes = pokes;
                        changed = true;
                        break;
                    }
                }

                if (changed) {
                    break;
                }
            }

            if (!changed) {
                return targetPokes.namesies();
            }
        }
    }

    private static void loadIncenseBabies() {
        incenseBabies = new HashSet<>();
        for (ItemNamesies itemNamesies : ItemNamesies.values()) {
            Item item = itemNamesies.getItem();
            if (item instanceof IncenseItem) {
                incenseBabies.add(((IncenseItem)item).getBaby());
            }
        }
    }

    public static PokemonNamesies getRandomStarterPokemon() {
        return RandomUtils.getRandomValue(starterPokemon);
    }

    public static int getNumBabyPokemon() {
        return babyPokemon.length;
    }

    public static Set<PokemonNamesies> getAllBabyPokemon() {
        return Arrays.stream(babyPokemon).collect(Collectors.toSet());
    }

    public static Set<PokemonNamesies> getAllTypedPokemon(Type type) {
        return EnumSet.copyOf(pokemonTypeMap.get(type));
    }

    public static int getNumTypedPokemon(Type type) {
        return pokemonTypeMap.get(type).size();
    }
}
