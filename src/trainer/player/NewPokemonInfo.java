package trainer.player;

import pokemon.PartyPokemon;
import util.Serializable;

public class NewPokemonInfo implements Serializable {
    private PartyPokemon newPokemon;
    private Integer newPokemonBox;
    private boolean isFirstNewPokemon;

    public PartyPokemon getNewPokemon() {
        return this.newPokemon;
    }

    public void setNewPokemon(PartyPokemon p) {
        this.newPokemon = p;
    }

    public Integer getNewPokemonBox() {
        return this.newPokemonBox;
    }

    public boolean isFirstNewPokemon() {
        return this.isFirstNewPokemon;
    }

    public void setFirstNewPokemon(boolean isFirstNewPokemon) {
        this.isFirstNewPokemon = isFirstNewPokemon;
    }

    public void inTeam() {
        this.newPokemonBox = null;
    }

    public void inBox(int boxNum) {
        this.newPokemonBox = boxNum;
    }
}
