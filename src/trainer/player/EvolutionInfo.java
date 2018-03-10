package trainer.player;

import battle.ActivePokemon;
import gui.view.ViewMode;
import map.triggers.CommonTrigger;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.Messages;
import pokemon.evolution.BaseEvolution;
import util.Serializable;

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

        Trigger trigger = CommonTrigger.EGGY_HATCH.getTrigger();
        Messages.add(new MessageUpdate().withTrigger(trigger));
    }

    public void setEvolution(ActivePokemon pokemon, BaseEvolution evolution) {
        this.evolvingPokemon = pokemon;
        this.evolution = evolution;

        Messages.add(new MessageUpdate().withViewChange(ViewMode.EVOLUTION_VIEW));
    }
}
