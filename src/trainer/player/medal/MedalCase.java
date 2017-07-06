package trainer.player.medal;

import battle.attack.AttackNamesies;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import trainer.player.pokedex.Pokedex;
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
        Arceus plates
        Catch legendary trios
        trades
 */
public class MedalCase implements Serializable {
    private final Set<Medal> medalsEarned;

    private final Map<MedalTheme, Integer> themeCounters;

    // Start with all baby Pokemon and remove as we go, earn medal when empty
    private final Set<PokemonNamesies> babyPokemonUnhatched;

    private int totalPokemonCaught;
    private Map<Type, Integer> totalPokemonCaughtTypeMap;
    private Map<Type, Set<PokemonNamesies>> uncaughtPokemonTypeMap;

    public MedalCase() {
        this.medalsEarned = EnumSet.noneOf(Medal.class);

        this.themeCounters = new EnumMap<>(MedalTheme.class);
        for (MedalTheme theme : MedalTheme.values()) {
            this.themeCounters.put(theme, 0);
        }

        this.totalPokemonCaughtTypeMap = new EnumMap<>(Type.class);
        this.uncaughtPokemonTypeMap = new EnumMap<>(Type.class);
        for (Type type : Type.values()) {
            this.totalPokemonCaughtTypeMap.put(type, 0);
            this.uncaughtPokemonTypeMap.put(type, EnumSet.noneOf(PokemonNamesies.class));
        }

        for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
            PokemonInfo pokemon = PokemonInfo.getPokemonInfo(i);
            for (Type type : pokemon.getType()) {
                this.uncaughtPokemonTypeMap.get(type).add(pokemon.namesies());
            }
        }

        this.babyPokemonUnhatched = PokemonInfo.getAllBabyPokemon();
    }

    public long getCount(Medal medal) {
        return themeCounters.get(MedalTheme.getMedalTheme(medal));
    }

    public boolean hasMedal(Medal medal) {
        return medalsEarned.contains(medal);
    }

    public void earnMedal(Medal medal) {
        if (!this.hasMedal(medal)) {
            medalsEarned.add(medal);
            Messages.add("Medal Earned: " + medal.getMedalName() + "!");
            System.out.println("Medal Earned: " + medal.getMedalName() + "!");

            this.update(MedalTheme.MEDALS_COLLECTED, this.medalsEarned.size());
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
            this.increase(MedalTheme.SHINIES_FOUND);
        }
    }

    public void hatch(ActivePokemon hatched) {
        this.increase(MedalTheme.EGGS_HATCHED);
        this.babyPokemonUnhatched.remove(hatched.getPokemonInfo().namesies());
        this.update(MedalTheme.BABIES_HATCHED, PokemonInfo.getNumBabyPokemon() - this.babyPokemonUnhatched.size());
    }

    public void useMove(AttackNamesies attack) {
        if (attack == AttackNamesies.SPLASH) {
            earnMedal(Medal.MAGIKARP_AWARD);
        }
        else if (attack == AttackNamesies.STRUGGLE) {
            earnMedal(Medal.NEVER_GIVE_UP);
        }
    }

    public void checkAdvantage(double advantage) {
        if (TypeAdvantage.isSuperEffective(advantage)) {
            this.increase(MedalTheme.SUPER_EFFECTIVE_MOVES_USED);
        }
        else if (TypeAdvantage.isNotVeryEffective(advantage)) {
            earnMedal(Medal.NONEFFECTIVE_ARTIST);
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
            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(updated);
            for (Type type : pokemonInfo.getType()) {
                this.uncaughtPokemonTypeMap.get(type).remove(updated);
                // TODO: Test case for this in case it changes names -- I don't like that this is hardcoded
                this.update(MedalTheme.valueOf(type.name() + "_CATCHER"), PokemonInfo.getNumTypedPokemon(type) - this.uncaughtPokemonTypeMap.get(type).size());
            }
        }
    }

    private void checkThreshold(MedalTheme theme) {
        theme.checkThreshold(themeCounters.get(theme));
    }

    public void update(MedalTheme theme, int count) {
        this.themeCounters.put(theme, count);
        this.checkThreshold(theme);
    }

    public void increase(MedalTheme theme) {
        this.increase(theme, 1);
    }

    public void increase(MedalTheme theme, int amount) {
        this.themeCounters.put(theme, this.themeCounters.get(theme) + amount);
        this.checkThreshold(theme);
    }
}
