package pokemon.species;

import battle.attack.AttackNamesies;
import map.overworld.wild.WildHoldItem;
import pokemon.ability.AbilityNamesies;
import pokemon.breeding.EggGroup;
import pokemon.evolution.Evolution;
import type.PokeType;
import type.Type;
import util.serialization.Serializable;
import util.string.StringAppender;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PokemonInfo implements Serializable, Comparable<PokemonInfo> {
    private static final long serialVersionUID = 1L;

    public static final int NUM_POKEMON = 825;
    public static final int EVOLUTION_LEVEL_LEARNED = 0;

    private final int number;
    private final PokemonNamesies namesies;
    private final BaseStats baseStats;
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
    private final int femaleRatio;
    private final String classification;
    private final int height;
    private final double weight;
    private final String flavorText;
    private final int eggSteps;
    private final EggGroup[] eggGroups;

    PokemonInfo(int number,
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
        this.baseStats = new BaseStats(baseStats);
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
        this.femaleRatio = genderRatio;
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

    public BaseStats getStats() {
        return this.baseStats;
    }

    public GrowthRate getGrowthRate() {
        return growthRate;
    }

    public int getEggSteps() {
        return eggSteps;
    }

    // Returns the abilities separated by commas
    public String getAbilitiesString() {
        return new StringAppender()
                .appendJoin(", ", abilities, AbilityNamesies::getName)
                .toString();
    }

    public AbilityNamesies[] getAbilities() {
        return abilities.clone();
    }

    public boolean hasAbility(AbilityNamesies s) {
        for (AbilityNamesies ability : abilities) {
            if (ability == s) {
                return true;
            }
        }
        return false;
    }

    public int numAbilities() {
        return abilities.length;
    }

    public int getCatchRate() {
        return catchRate;
    }

    public int getBaseEXP() {
        return baseExp;
    }

    public int getGivenEV(int statIndex) {
        return givenEVs[statIndex];
    }

    public int[] getGivenEVs() {
        return givenEVs;
    }

    // Female ratio is in eighths, -1 for genderless
    public int getFemaleRatio() {
        return femaleRatio;
    }

    public String getHeightString() {
        return String.format("%d'%02d\"", height/12, height%12);
    }

    public int getHeightInches() {
        return height;
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
        return eggGroups[0] != EggGroup.NO_EGGS;
    }

    public PokemonNamesies getBaseEvolution() {
        return getBaseEvolution(this);
    }

    public EggGroup[] getEggGroups() {
        return eggGroups;
    }

    public boolean isIncenseBaby() {
        return PokemonList.instance().isIncenseBaby(namesies);
    }

    public boolean isBabyPokemon() {
        return PokemonList.instance().isBabyPokemon(namesies);
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
                PokemonInfo pokes = PokemonList.get(pokesName.ordinal());
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
}
