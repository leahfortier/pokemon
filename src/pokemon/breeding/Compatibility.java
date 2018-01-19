package pokemon.breeding;

import battle.ActivePokemon;
import util.PokeString;
import util.RandomUtils;
import util.StringUtils;

enum Compatibility {
    SINGLE(0, StringUtils.empty()),
    NO_DICE(0, "They prefer to play with other " + PokeString.POKEMON + " rather than each other."),
    OKAY_I_GUESS(35, "I guess they're okay with each other I guess."),
    HOUSE_ON_FIRE(60, "Those two seem to get along like a house on fire!!!!!");

    private final int eggChance;
    private final String message;

    Compatibility(int eggChance, String message) {
        this.eggChance = eggChance;
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean eggChanceTest() {
        return RandomUtils.chanceTest(eggChance);
    }

    public static Compatibility getCompatibility(ActivePokemon first, ActivePokemon second) {
        if (first == null || second == null) {
            return Compatibility.SINGLE;
        }

        if (!Breeding.instance().canBreed(first, second)) {
            return Compatibility.NO_DICE;
        }

        if (first.namesies() == second.namesies()) {
            return Compatibility.HOUSE_ON_FIRE;
        }

        return Compatibility.OKAY_I_GUESS;
    }
}
