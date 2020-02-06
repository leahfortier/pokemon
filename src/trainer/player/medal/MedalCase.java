package trainer.player.medal;

import battle.ActivePokemon;
import battle.attack.AttackNamesies;
import pokemon.active.IndividualValues;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonList;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;
import trainer.player.pokedex.Pokedex;
import type.Type;
import type.TypeAdvantage;
import util.serialization.Serializable;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/*
    TODO:
        Berry harvests
        Critical captures
        Arceus plates
        Catch legendary trios
        trades
 */
public class MedalCase implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final MedalCase EMPTY_CASE = new MedalCase();

    private final Set<Medal> medalsEarned;
    private final Map<MedalTheme, Integer> themeCounters;
    private final Map<Type, Set<PokemonNamesies>> uncaughtPokemonTypeMap;

    // Start with all baby Pokemon and remove as we go, earn medal when empty
    private final Set<PokemonNamesies> babyPokemonUnhatched;

    private final ArrayDeque<Medal> toDisplay;

    public MedalCase() {
        this.medalsEarned = EnumSet.noneOf(Medal.class);

        this.themeCounters = new EnumMap<>(MedalTheme.class);
        for (MedalTheme theme : MedalTheme.values()) {
            this.themeCounters.put(theme, 0);
        }

        this.uncaughtPokemonTypeMap = new EnumMap<>(Type.class);
        for (Type type : Type.values()) {
            this.uncaughtPokemonTypeMap.put(type, EnumSet.noneOf(PokemonNamesies.class));
        }

        for (PokemonInfo pokemon : PokemonList.instance()) {
            for (Type type : pokemon.getType()) {
                this.uncaughtPokemonTypeMap.get(type).add(pokemon.namesies());
            }
        }

        this.babyPokemonUnhatched = PokemonList.instance().getAllBabyPokemon();

        this.toDisplay = new ArrayDeque<>();
    }

    public long getCount(Medal medal) {
        return themeCounters.get(MedalTheme.getMedalTheme(medal));
    }

    public String getThresholdString(Medal medal) {
        if (medal.hasThreshold()) {
            return this.getCount(medal) + "/" + medal.getThreshold();
        }

        return "";
    }

    public boolean hasMedal(Medal medal) {
        return medalsEarned.contains(medal);
    }

    public void earnMedal(Medal medal) {
        if (!this.hasMedal(medal)) {
            medalsEarned.add(medal);
            toDisplay.add(medal);
            this.update(MedalTheme.MEDALS_COLLECTED, this.medalsEarned.size());
        }
    }

    public boolean hasQueuedDisplayMedal() {
        return !this.toDisplay.isEmpty();
    }

    public Medal getNextDisplayMedal() {
        return this.toDisplay.pop();
    }

    public void encounterPokemon(ActivePokemon encountered) {
        if (encountered.isShiny()) {
            this.increase(MedalTheme.SHINIES_FOUND);
        }
    }

    public void hatch(ActivePokemon hatched) {
        this.increase(MedalTheme.EGGS_HATCHED);

        this.babyPokemonUnhatched.remove(hatched.namesies());
        this.update(MedalTheme.BABIES_HATCHED, PokemonList.instance().getNumBabyPokemon() - this.babyPokemonUnhatched.size());

        int perfectIVs = 0;
        for (Stat stat : Stat.STATS) {
            if (hatched.getIVs().get(stat) == IndividualValues.MAX_IV) {
                perfectIVs++;
            }
        }

        if (perfectIVs >= Stat.NUM_STATS - 1) {
            earnMedal(Medal.CHAMPION_OF_GENETICS);
        }
    }

    public void useMove(AttackNamesies attack) {
        if (attack == AttackNamesies.SPLASH) {
            earnMedal(Medal.MAGIKARP_AWARD);
        } else if (attack == AttackNamesies.STRUGGLE) {
            earnMedal(Medal.NEVER_GIVE_UP);
        }
    }

    public void checkAdvantage(double advantage) {
        if (TypeAdvantage.isSuperEffective(advantage)) {
            this.increase(MedalTheme.SUPER_EFFECTIVE_MOVES_USED);
        } else if (TypeAdvantage.isNotVeryEffective(advantage)) {
            this.earnMedal(Medal.NONEFFECTIVE_ARTIST);
        }
    }

    public void updatePokedex(Pokedex pokedex, PokemonNamesies updated) {
        this.update(MedalTheme.POKEMON_SEEN, pokedex.numSeen());
        this.update(MedalTheme.POKEMON_CAUGHT, pokedex.numCaught());

        // If you've seen Mew, then Mew're the Winner
        if (updated == PokemonNamesies.MEW && !pokedex.isNotSeen(updated)) {
            this.earnMedal(Medal.MEWRE_THE_WINNER);
        }

        if (pokedex.isCaught(updated)) {
            PokemonInfo pokemonInfo = updated.getInfo();
            for (Type type : pokemonInfo.getType()) {
                this.uncaughtPokemonTypeMap.get(type).remove(updated);
                // TODO: Test case for this in case it changes names -- I don't like that this is hardcoded
                this.update(MedalTheme.valueOf(type.name() + "_CATCHER"), PokemonList.instance().getNumTypedPokemon(type) - this.uncaughtPokemonTypeMap.get(type).size());
            }
        }
    }

    private void update(MedalTheme theme, int count) {
        this.themeCounters.put(theme, count);
        theme.checkThreshold(themeCounters.get(theme));
    }

    public void increase(MedalTheme theme, int amount) {
        this.update(theme, this.themeCounters.get(theme) + amount);
    }

    public void increase(MedalTheme theme) {
        this.increase(theme, 1);
    }

    public int numMedalsEarned() {
        return this.medalsEarned.size();
    }
}
