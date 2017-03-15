package map.triggers.daycare;

import gui.view.ViewMode;
import main.Game;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.Messages;
import pokemon.breeding.DayCareCenter;
import trainer.player.Player;
import util.PokeString;

public class DayCareTrigger extends Trigger {
    public DayCareTrigger(String contents, String condition) {
        super(TriggerType.DAY_CARE, contents, condition);
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

        Messages.add(new MessageUpdate().withTrigger(TriggerType.CHANGE_VIEW.createTrigger(ViewMode.DAY_CARE_VIEW.name()).getName()));
    }
}
