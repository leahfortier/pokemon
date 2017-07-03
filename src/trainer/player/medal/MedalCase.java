package trainer.player.medal;

import pokemon.ActivePokemon;
import type.Type;
import type.TypeAdvantage;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/*
    TODO:
        Berry harvests
        Nicknames given
        Critical captures
        Hatch all babies
        Arceus plates
        Catch legendary trios
        trades
 */
public class MedalCase implements Serializable {
    private final Set<Medal> medalsEarned;

    private long cashMoneySpent;

    private int superEffectiveMovesUsed;
    private boolean notVeryEffectiveMoveUsed;

    private int totalPokemonCaught;
    private Map<Type, Integer> totalPokemonCaughtTypeMap;

    private int totalShiniesSeen;
    private int totalPokemonEvolved;
    private int eggsHatched;
    private int fishReeledIn;
    private int hiddenItemsFound;
    private int timesSaved;
    private int pokecenterHeals;

    private MedalCounter stepsWalked = new MedalCounter(
            new MedalThreshold(Medal.LIGHT_WALKER, 5000),
            new MedalThreshold(Medal.MIDDLE_WALKER, 10000),
            new MedalThreshold(Medal.HEAVY_WALKER, 25000),
            new MedalThreshold(Medal.HONORED_FOOTPRINTS, 100000)
    );

    private MedalCounter medalsCollected = new MedalCounter(
            new MedalThreshold(Medal.ROOKIE_MEDALIST, 50),
            new MedalThreshold(Medal.ELITE_MEDALIST, 100),
            new MedalThreshold(Medal.MASTER_MEDALIST, 150),
            new MedalThreshold(Medal.LEGEND_MEDALIST, 200),
            new MedalThreshold(Medal.TOP_MEDALIST, Medal.values().length - 1)
    );

    public MedalCase() {
        this.medalsEarned = EnumSet.noneOf(Medal.class);

        this.totalPokemonCaughtTypeMap = new EnumMap<>(Type.class);
        for (Type type : Type.values()) {
            this.totalPokemonCaughtTypeMap.put(type, 0);
        }
    }

    public boolean hasMedal(Medal medal) {
        return medalsEarned.contains(medal);
    }

    public void earnMedal(Medal medal) {
        if (!this.hasMedal(medal)) {
            // TODO: Animation
            medalsEarned.add(medal);
            System.out.println("Medal Earned: " + medal.getMedalName() + "!");
        }
    }

    public void catchNewPokemon(ActivePokemon caught) {
        totalPokemonCaught++;

        for (Type type : caught.getActualType()) {
            totalPokemonCaughtTypeMap.put(type, totalPokemonCaughtTypeMap.get(type) + 1);
        }
    }

    public void encounterPokemon(ActivePokemon encountered) {
        if (encountered.isShiny()) {
            totalShiniesSeen++;
        }
    }

    public void step() {
        stepsWalked.increase(1);
    }

    public void hatch() {
        eggsHatched++;
    }

    public void moveAdvantage(double advantage) {
        if (TypeAdvantage.isSuperEffective(advantage)) {
            superEffectiveMovesUsed++;
        }

        if (TypeAdvantage.isNotVeryEffective(advantage)) {
            notVeryEffectiveMoveUsed = true;
        }
    }

    public void livinLarge(int datCash) {
        this.cashMoneySpent += datCash;
    }
}
