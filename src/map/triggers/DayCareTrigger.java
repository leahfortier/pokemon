package map.triggers;

import gui.view.ViewMode;
import main.Game;
import map.condition.Condition;
import message.Messages;
import pokemon.breeding.DayCareCenter;
import trainer.player.Player;
import util.PokeString;

public class DayCareTrigger extends Trigger {
    public DayCareTrigger(String contents, Condition condition) {
        this(condition);
    }

    public DayCareTrigger(Condition condition) {
        super(null, condition);
    }

    @Override
    protected void executeTrigger() {
        Player player = Game.getPlayer();
        DayCareCenter dayCare = player.getDayCareCenter();

        if (dayCare.hasEggy()) {
            dayCare.giveEggy();
            return;
        }

        Messages.add("Welcome to the " + PokeString.POKEMON + " Brothel, err... Day Care Center. We totes not creeps!");
        Messages.add(dayCare.getPokemonPresentMessage());
        Messages.add(dayCare.getCompatibilityMessage());

        ChangeViewTrigger.addChangeViewTriggerMessage(ViewMode.DAY_CARE_VIEW);
    }
}
