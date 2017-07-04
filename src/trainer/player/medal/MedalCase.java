package trainer.player.medal;

import battle.attack.AttackNamesies;
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
        hidden items
 */
public class MedalCase implements Serializable {
    private final Set<Medal> medalsEarned;

    private int totalPokemonCaught;
    private Map<Type, Integer> totalPokemonCaughtTypeMap;

    private int totalShiniesSeen;
    private int totalPokemonEvolved;

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

    public final MedalCounter eggsHatched = new MedalCounter(
            Medal.EGG_BEGINNER,
            Medal.EGG_BREEDER,
            Medal.EGG_ELITE,
            Medal.HATCHING_AFICIONADO
    );

    public final MedalCounter dayCareDeposited = new MedalCounter(
            Medal.DAY_CARE_FAITHFUL,
            Medal.DAY_CARE_SUPER_FAITHFUL,
            Medal.DAY_CARE_EXTRAORDINARY_FAITHFUL
    );

    public final MedalCounter itemsBought = new MedalCounter(
            Medal.REGULAR_CUSTOMER
    );

    public final MedalCounter cashMoneySpent = new MedalCounter(
            Medal.MODERATE_CUSTOMER,
            Medal.GREAT_CUSTOMER,
            Medal.INDULGENT_CUSTOMER,
            Medal.SUPER_RICH
    );

    public final MedalCounter superEffectiveMovesUsed = new MedalCounter(
            Medal.SUPEREFFECTIVE_SAVANT
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
        eggsHatched.increase();
    }

    public void useMove(AttackNamesies attack, double advantage) {
        if (attack == AttackNamesies.SPLASH) {
            earnMedal(Medal.MAGIKARP_AWARD);
        }
        else if (attack == AttackNamesies.STRUGGLE) {
            earnMedal(Medal.NEVER_GIVE_UP);
        }

        if (TypeAdvantage.isSuperEffective(advantage)) {
            superEffectiveMovesUsed.increase();
        }
        else if (TypeAdvantage.isNotVeryEffective(advantage)) {
            earnMedal(Medal.NONEFFECTIVE_ARTIST);
        }
    }

    public void livinLarge(int datCash) {
        this.cashMoneySpent.increase(datCash);
    }
}
