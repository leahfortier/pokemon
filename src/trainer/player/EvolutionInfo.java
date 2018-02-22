package trainer.player;

import battle.ActivePokemon;
import gui.view.ViewMode;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.Messages;
import pattern.action.StringActionMatcher.GroupTriggerActionMatcher;
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

        Trigger trigger = new GroupTriggerActionMatcher("EggHatching").createNewTrigger(null);
        Messages.add(new MessageUpdate().withTrigger(trigger));
    }

    public void setEvolution(ActivePokemon pokemon, BaseEvolution evolution) {
        this.evolvingPokemon = pokemon;
        this.evolution = evolution;

        Messages.add(new MessageUpdate().withViewChange(ViewMode.EVOLUTION_VIEW));
    }
}
