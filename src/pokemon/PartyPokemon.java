package pokemon;

import battle.ActivePokemon;
import main.Global;
import pattern.PokemonMatcher;
import util.StringUtils;

import java.io.Serializable;

public abstract class PartyPokemon implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_LEVEL = 100;
    public static final int MAX_NAME_LENGTH = 10;

    public static final String TINY_EGG_IMAGE_NAME = "egg-small";
    public static final String BASE_EGG_IMAGE_NAME = "egg";
    public static final String SPRITE_EGG_IMAGE_NAME = "EggSprite";

    protected static final String[][] characteristics =
        {{"Loves to eat",            "Proud of its power",      "Sturdy body",            "Highly curious",        "Strong willed",     "Likes to run"},
         {"Takes plenty of siestas", "Likes to thrash about",   "Capable of taking hits", "Mischievous",           "Somewhat vain",     "Alert to sounds"},
         {"Nods off a lot",          "A little quick tempered", "Highly persistent",      "Thoroughly cunning",    "Strongly defiant",  "Impetuous and silly"},
         {"Scatters things often",   "Likes to fight",          "Good endurance",         "Often lost in thought", "Hates to lose",     "Somewhat of a clown"},
         {"Likes to relax",          "Quick tempered",          "Good perseverance",      "Very finicky",          "Somewhat stubborn", "Quick to flee"}};

    /*
         * Format: Name Level Parameters
         * Possible parameters:
         *         Moves: Move1, Move2, Move3, Move4*
         *         Shiny
         *         Egg
         *         Item: item name*
         */
    // Constructor for triggers
    public static ActivePokemon createActivePokemon(PokemonMatcher pokemonMatcher, boolean user) {

        // Random Starter Egg
        if (pokemonMatcher.isStarterEgg()) {
            if (!user) {
                Global.error("Trainers cannot have eggs.");
            }

            return new ActivePokemon(PokemonInfo.getRandomStarterPokemon());
        }

        final PokemonNamesies namesies = pokemonMatcher.getNamesies();

        ActivePokemon pokemon;
        if (pokemonMatcher.isEgg()) {
            if (!user) {
                Global.error("Trainers cannot have eggs.");
            }

            pokemon = new ActivePokemon(namesies);
        } else {
            pokemon = new ActivePokemon(namesies, pokemonMatcher.getLevel(), false, user);
            String nickname = pokemonMatcher.getNickname();
            if (!StringUtils.isNullOrEmpty(nickname)) {
                pokemon.setNickname(nickname);
            }
        }

        if (pokemonMatcher.isShiny()) {
            pokemon.setShiny();
        }

        if (pokemonMatcher.hasMoves()) {
            pokemon.setMoves(pokemonMatcher.getMoves());
        }

        if (pokemonMatcher.hasHoldItem()) {
            pokemon.giveItem(pokemonMatcher.getHoldItem());
        }

        return pokemon;
    }
}
