package trainer.player;

import gui.view.ViewMode;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.Messages;
import battle.ActivePokemon;
import pokemon.evolution.BaseEvolution;

import java.io.Serializable;

public class EvolutionInfo implements Serializable {
    private ActivePokemon evolvingPokemon;
    private BaseEvolution evolution;

    public ActivePokemon getEvolvingPokemon() {
        return evolvingPokemon;
    }

    public BaseEvolution getEvolution() {
        return evolution;
    }

    void setEgg(ActivePokemon hatched) {
        evolvingPokemon = hatched;
        evolution = null;

        Messages.add(new MessageUpdate().withTrigger(TriggerType.GROUP.getTriggerNameFromSuffix("EggHatching")));
    }

    public void setEvolution(ActivePokemon pokemon, BaseEvolution evolution) {
        this.evolvingPokemon = pokemon;
        this.evolution = evolution;

        Messages.add(new MessageUpdate().withViewChange(ViewMode.EVOLUTION_VIEW));
    }
}
