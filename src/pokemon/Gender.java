package pokemon;

import battle.ActivePokemon;
import util.RandomUtils;
import util.serialization.Serializable;

import java.awt.Color;

public enum Gender implements Serializable {
    MALE("\u2642", new Color(55, 125, 220), ratio -> ratio != Gender.GENDERLESS_CONSTANT && ratio != 0),
    FEMALE("\u2640", new Color(220, 50, 70), ratio -> ratio != Gender.GENDERLESS_CONSTANT && ratio != 100),
    GENDERLESS(" ", Color.WHITE, ratio -> ratio == Gender.GENDERLESS_CONSTANT);

    private static final int GENDERLESS_CONSTANT = -1;

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
        return this.genderChecker.canHaveGender(pokemon.getMaleRatio());
    }

    public Gender getOppositeGender() {
        switch (this) {
            case MALE:
                return FEMALE;
            case FEMALE:
                return MALE;
            case GENDERLESS:
            default:
                return GENDERLESS;
        }
    }

    public static Gender getGender(int ratio) {
        if (ratio == -1) {
            return GENDERLESS;
        }

        return RandomUtils.chanceTest(ratio) ? MALE : FEMALE;
    }

    public static boolean oppositeGenders(ActivePokemon me, ActivePokemon o) {
        Gender gender = me.getGender();
        return gender != GENDERLESS && gender == o.getGender().getOppositeGender();
    }

    public static String getGenderString(PokemonInfo pokemon) {
        int maleRatio = pokemon.getMaleRatio();
        if (maleRatio == GENDERLESS_CONSTANT) {
            return "Genderless";
        }

        return String.format("%d%% Male, %d%% Female", maleRatio, 100 - maleRatio);
    }

    @FunctionalInterface
    private interface GenderChecker {
        boolean canHaveGender(int maleRatio);
    }
}
