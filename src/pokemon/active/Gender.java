package pokemon.active;

import battle.ActivePokemon;
import main.Global;
import pokemon.species.PokemonInfo;
import util.RandomUtils;

import java.awt.Color;
import java.text.DecimalFormat;

public enum Gender {
    MALE("\u2642", new Color(55, 125, 220), ratio -> ratio != Gender.GENDERLESS_VALUE && ratio != 8),
    FEMALE("\u2640", new Color(220, 50, 70), ratio -> ratio != Gender.GENDERLESS_VALUE && ratio != 0),
    GENDERLESS(" ", Color.WHITE, ratio -> ratio == Gender.GENDERLESS_VALUE);

    public static final int GENDERLESS_VALUE = -1;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();
    static {
        DECIMAL_FORMAT.setMaximumFractionDigits(1);
    }

    private final String character;
    private final Color color;
    private final GenderChecker genderChecker;

    Gender(String character, Color color, GenderChecker genderChecker) {
        this.character = character;
        this.color = color;
        this.genderChecker = genderChecker;
    }

    public String getCharacter() {
        return character;
    }

    public Color getColor() {
        return color;
    }

    public boolean genderApplies(PokemonInfo pokemon) {
        return this.genderChecker.canHaveGender(pokemon.getFemaleRatio());
    }

    public Gender getOppositeGender() {
        switch (this) {
            case MALE:
                return FEMALE;
            case FEMALE:
                return MALE;
            case GENDERLESS:
                return GENDERLESS;
            default:
                Global.error("Unknown gender " + this);
                return GENDERLESS;
        }
    }

    // femaleRatio is in eighths or -1 for genderless
    public static Gender getGender(int femaleRatio) {
        if (femaleRatio == GENDERLESS_VALUE) {
            return GENDERLESS;
        }

        return RandomUtils.chanceTest(femaleRatio, 8) ? FEMALE : MALE;
    }

    public static boolean oppositeGenders(ActivePokemon me, ActivePokemon o) {
        Gender gender = me.getGender();
        return gender != GENDERLESS && gender == o.getGender().getOppositeGender();
    }

    public static String getGenderString(PokemonInfo pokemon) {
        int femaleRatio = pokemon.getFemaleRatio();
        if (femaleRatio == GENDERLESS_VALUE) {
            return "Genderless";
        }

        double femalePercentage = 100*femaleRatio/8.0;
        if (femalePercentage == 100) {
            return "100% Female";
        } else if (femalePercentage == 0) {
            return "100% Male";
        } else {
            // Prints at most one decimal place
            // Ex: "50% Male, 50% Female" or "87.5% Male, 12.5% Female"
            return String.format(
                    "%s%% Male, %s%% Female",
                    DECIMAL_FORMAT.format(100 - femalePercentage),
                    DECIMAL_FORMAT.format(femalePercentage)
            );
        }
    }

    @FunctionalInterface
    private interface GenderChecker {
        boolean canHaveGender(int femaleRatio);
    }
}
