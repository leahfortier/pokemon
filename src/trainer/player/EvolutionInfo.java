package trainer.player;

import gui.view.ViewMode;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.evolution.BaseEvolution;

public class EvolutionInfo {
    private ActivePokemon evolvingPokemon;
    private BaseEvolution evolution;

    public ActivePokemon getEvolvingPokemon() {
        return evolvingPokemon;
    }

    public BaseEvolution getEvolution() {
        return evolution;
    }

    void setEgg(ActivePokemon eggy) {
        evolvingPokemon = eggy;
        evolution = null;
    }

    public void setEvolution(ActivePokemon pokemon, BaseEvolution evolution) {
        this.evolvingPokemon = pokemon;
        this.evolution = evolution;

        Messages.add(new MessageUpdate().withViewChange(ViewMode.EVOLUTION_VIEW));
    }
}
