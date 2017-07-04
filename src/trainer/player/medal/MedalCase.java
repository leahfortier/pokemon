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
    private int hiddenItemsFound;

    public final MedalCounter stepsWalked = new MedalCounter(
            Medal.LIGHT_WALKER,
            Medal.MIDDLE_WALKER,
            Medal.HEAVY_WALKER,
            Medal.HONORED_FOOTPRINTS
    );

    public final MedalCounter timesSaved = new MedalCounter(
            Medal.STEP_BY_STEP_SAVER,
            Medal.BUSY_SAVER,
            Medal.EXPERIENCED_SAVER,
            Medal.WONDER_WRITER
    );

    // TODO
    public final MedalCounter pokecenterHeals = new MedalCounter(
            Medal.POKEMON_CENTER_FAN,
            Medal.POKEMON_CENTER_SUPER_FAN
    );

    public final MedalCounter bicycleCount = new MedalCounter(
            Medal.STARTER_CYCLING,
            Medal.EASY_CYCLING,
            Medal.HARD_CYCLING,
            Medal.PEDALING_LEGEND
    );

    public final MedalCounter fishReeledIn = new MedalCounter(
            Medal.OLD_ROD_FISHERMAN,
            Medal.GOOD_ROD_FISHERMAN,
            Medal.SUPER_ROD_FISHERMAN,
            Medal.MIGHTY_FISHER
    );

    private final MedalCounter medalsCollected = new MedalCounter(
             Medal.ROOKIE_MEDALIST,
             Medal.ELITE_MEDALIST,
             Medal.MASTER_MEDALIST,
             Medal.LEGEND_MEDALIST,
             Medal.TOP_MEDALIST
    );

    public MedalCase() {
        this.medalsEarned = EnumSet.noneOf(Medal.class);

        this.totalPokemonCaughtTypeMap = new EnumMap<>(Type.class);
        for (Type type : Type.values()) {
            this.totalPokemonCaughtTypeMap.put(type, 0);
        }
    }

    private boolean hasMedal(Medal medal) {
        return medalsEarned.contains(medal);
    }

    void earnMedal(Medal medal) {
        if (!this.hasMedal(medal)) {
            // TODO: Animation
            medalsEarned.add(medal);
            System.out.println("Medal Earned: " + medal.getMedalName() + "!");

            this.medalsCollected.update(this.medalsEarned.size());
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
