package pokemon.evolution;

import item.ItemNamesies;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonNamesies;

class GenderEvolution extends Evolution {
    private static final long serialVersionUID = 1L;

    private Evolution evolution;
    private Gender gender;

    GenderEvolution(String gender, Evolution evolution) {
        if (!(evolution instanceof BaseEvolution)) {
            Global.error("Gender evolution does not make any sense!");
        }

        this.evolution = evolution;
        this.gender = Gender.valueOf(gender.toUpperCase());
        if (this.gender != Gender.MALE && this.gender != Gender.FEMALE) {
            Global.error("Incorrect Gender Name for Evolution");
        }
    }

    @Override
    public Evolution getEvolution(EvolutionMethod type, ActivePokemon pokemon, ItemNamesies use) {
        if (pokemon.getGender() == this.gender) {
            return this.evolution.getEvolution(type, pokemon, use);
        }

        return null;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return this.evolution.getEvolutions();
    }
}
