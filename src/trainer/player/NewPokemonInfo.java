package trainer.player;

import pokemon.ActivePokemon;

import java.io.Serializable;

public class NewPokemonInfo implements Serializable {
    private ActivePokemon newPokemon;
    private Integer newPokemonBox;
    private boolean isFirstNewPokemon;

    public ActivePokemon getNewPokemon() {
        return this.newPokemon;
    }

    public void setNewPokemon(ActivePokemon p) {
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
