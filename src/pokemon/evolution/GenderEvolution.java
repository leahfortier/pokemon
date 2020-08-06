package pokemon.evolution;

import battle.ActivePokemon;
import main.Global;
import pokemon.active.Gender;
import pokemon.species.PokemonNamesies;
import util.string.StringUtils;

class GenderEvolution extends ConditionEvolution {
    private static final long serialVersionUID = 1L;

    private final Gender gender;

    GenderEvolution(String gender, BaseEvolution evolution) {
        super(evolution);
        this.gender = Gender.valueOf(gender.toUpperCase());
        if (this.gender != Gender.MALE && this.gender != Gender.FEMALE) {
            Global.error("Incorrect Gender Name for Evolution");
        }
    }

    @Override
    protected boolean meetsCondition(ActivePokemon pokemon) {
        return pokemon.getGender() == this.gender;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return this.evolution.getEvolutions();
    }

    @Override
    public String getString() {
        return this.evolution.getString() + ", " + StringUtils.properCase(this.gender.name().toLowerCase()) + " only";
    }

    @Override
    public String toString() {
        return EvolutionType.GENDER + " " + gender.name() + " " + evolution;
    }
}
