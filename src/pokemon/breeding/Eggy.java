package pokemon.breeding;

import battle.ActivePokemon;
import battle.attack.Move;
import pokemon.active.Gender;
import pokemon.active.Nature;
import pokemon.active.PartyPokemon;
import pokemon.species.PokemonNamesies;
import trainer.TrainerType;

import java.util.List;

public class Eggy extends PartyPokemon {
    private static final long serialVersionUID = 1L;

    public static final String TINY_EGG_IMAGE_NAME = "egg-small";
    public static final String BASE_EGG_IMAGE_NAME = "egg";
    public static final String SPRITE_EGG_IMAGE_NAME = "egg";

    private int eggSteps;

    public Eggy(PokemonNamesies pokemonNamesies, Boolean shiny, List<Move> moves, Gender gender, Nature nature) {
        super(pokemonNamesies, 1, TrainerType.PLAYER, "Egg", shiny, moves, gender, nature);

        this.eggSteps = this.getPokemonInfo().getEggSteps();
    }

    public Eggy(PokemonNamesies pokemonNamesies) {
        this(pokemonNamesies, null, null, null, null);
    }

    public Eggy(ActivePokemon daddy, ActivePokemon mommy, PokemonNamesies pokemonNamesies) {
        this(pokemonNamesies);

        Breeding breeding = Breeding.instance();
        this.setMoves(breeding.getBabyMoves(daddy, mommy, pokemonNamesies));
        this.setNature(breeding.getBabyNature(daddy, mommy));
        this.setIVs(breeding.getBabyIVs(daddy, mommy));
    }

    @Override
    public boolean canFight() {
        // Eggs can't fight!!!
        return false;
    }

    @Override
    public void resetAttributes() {}

    @Override
    public void setUsed(boolean used) {}

    @Override
    public boolean isUsed() {
        return false;
    }

    @Override
    public boolean isBattleUsed() {
        return false;
    }

    @Override
    public boolean canBreed() {
        // Eggs can't breed!!!
        return false;
    }

    // Does not include shiny -- this is for the small party tiles
    @Override
    public String getTinyImageName() {
        return Eggy.TINY_EGG_IMAGE_NAME;
    }

    // Does not include shiny -- this is for the pokedex tiles (in new pokemon view)
    @Override
    public String getBaseImageName() {
        return Eggy.BASE_EGG_IMAGE_NAME;
    }

    // Larger image index
    @Override
    public String getImageName(boolean front) {
        return Eggy.SPRITE_EGG_IMAGE_NAME;
    }

    @Override
    public boolean isEgg() {
        return true;
    }

    @Override
    public String getGenderString() {
        return "";
    }

    public ActivePokemon hatch(boolean doubleHatch) {
        eggSteps -= doubleHatch ? 2 : 1;

        if (eggSteps > 0) {
            return null;
        }

        return new ActivePokemon(this);
    }

    public String getEggMessage() {
        if (eggSteps > 10*255) {
            return "Wonder what's inside? It needs more time though.";
        } else if (eggSteps > 5*255) {
            return "It moves around inside sometimes. It must be close to hatching.";
        } else {
            return "It's making sounds inside! It's going to hatch soon!";
        }
    }
}
