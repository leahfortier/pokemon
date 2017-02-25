package map.triggers.daycare;

import main.Game;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import mapMaker.dialogs.action.trigger.TriggerActionType;
import message.MessageUpdate;
import message.Messages;
import pattern.action.ActionMatcher;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import pattern.action.TriggerActionMatcher;
import pokemon.breeding.DayCareCenter;
import trainer.Player;
import util.PokeString;
import util.StringUtils;

public class DayCareTrigger extends Trigger {
    public DayCareTrigger(String contents, String condition) {
        super(TriggerType.DAY_CARE, contents, condition);
    }

    @Override
    protected void executeTrigger() {
        Player player = Game.getPlayer();
        DayCareCenter dayCare = player.getDayCareCenter();

        Messages.add("Welcome to the " + PokeString.POKEMON + " Brothel, err... Day Care Center. We totes not creeps!");
        Messages.add(dayCare.getPokemonPresentMessage());
        Messages.add(dayCare.getCompatibilityMessage());

        ActionMatcher withdrawAction = new ActionMatcher();
        withdrawAction.setTrigger(new TriggerActionMatcher(TriggerActionType.DAY_CARE_WITHDRAW, StringUtils.empty()));
        ChoiceMatcher withdrawChoice = new ChoiceMatcher(
                "Withdraw",
                new ActionMatcher[] { withdrawAction }
        );

        ActionMatcher depositAction = new ActionMatcher();
        depositAction.setTrigger(new TriggerActionMatcher(TriggerActionType.DAY_CARE_DEPOSIT, StringUtils.empty()));
        ChoiceMatcher depositChoice = new ChoiceMatcher(
                "Deposit",
                new ActionMatcher[] { depositAction }
        );

        Messages.add(new MessageUpdate("What would you like to do?").withChoices(new ChoiceMatcher[] { withdrawChoice, depositChoice }));
    }
}
