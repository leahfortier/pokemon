package pokemon.evolution;

import item.ItemNamesies;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import util.StringUtils;

class GenderEvolution extends Evolution {
    private static final long serialVersionUID = 1L;

    private final BaseEvolution evolution;
    private final Gender gender;

    GenderEvolution(String gender, BaseEvolution evolution) {
        this.evolution = evolution;
        this.gender = Gender.valueOf(gender.toUpperCase());
        if (this.gender != Gender.MALE && this.gender != Gender.FEMALE) {
            Global.error("Incorrect Gender Name for Evolution");
        }
    }

    @Override
    public BaseEvolution getEvolution(EvolutionMethod type, ActivePokemon pokemon, ItemNamesies use) {
        if (pokemon.getGender() == this.gender) {
            return this.evolution.getEvolution(type, pokemon, use);
        }

        return null;
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
        return EvolutionType.GENDER + " " + gender.name() + " " + evolution.toString();
    }
}
