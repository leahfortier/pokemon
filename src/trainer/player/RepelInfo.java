package trainer.player;

import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import mapMaker.dialogs.action.trigger.TriggerActionType;
import message.MessageUpdate;
import message.Messages;
import pattern.action.ActionMatcher;
import pattern.action.ChoiceActionMatcher;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import pattern.action.TriggerActionMatcher;
import util.SerializationUtils;

import java.io.Serializable;

public class RepelInfo implements Serializable {
    private int repelSteps;
    private ItemNamesies repelItem;

    public boolean isUsingRepel() {
        return repelSteps > 0;
    }

    public void useItem(ItemNamesies itemNamesies, int steps) {
        this.repelItem = itemNamesies;
        this.repelSteps = steps;
    }

    void step() {
        // Decrease repel steps
        if (repelSteps > 0) {
            repelSteps--;
            if (repelSteps == 0) {
                // TODO: Give choice if you want to use another.
                // Game variable needed
                Messages.add("The effects of repel have worn off.");

                Player player = Game.getPlayer();
                Bag bag = player.getBag();
                if (bag.hasItem(this.repelItem)) {
                    ActionMatcher useAnotherAction = new ActionMatcher();
                    useAnotherAction.setTrigger(new TriggerActionMatcher(TriggerActionType.USE_ITEM, repelItem.name()));
                    ChoiceMatcher useAnother = new ChoiceMatcher("Sure!", new ActionMatcher[] { useAnotherAction });

                    ActionMatcher doNotUseAction = new ActionMatcher();
                    doNotUseAction.setTrigger(new TriggerActionMatcher(TriggerActionType.DIALOGUE, "They're coming for you. Worry."));
                    ChoiceMatcher doNotUse = new ChoiceMatcher("Nah...", new ActionMatcher[] { doNotUseAction });

                    ChoiceActionMatcher choice = new ChoiceActionMatcher(
                            "Would you like to use another " + repelItem.getName() + "?",
                            new ChoiceMatcher[] { useAnother, doNotUse }
                    );

                    Trigger choiceTrigger = TriggerType.CHOICE.createTrigger(SerializationUtils.getJson(choice));
                    Messages.add(new MessageUpdate().withTrigger(choiceTrigger.getName()));
                }
            }
        } else {
            repelSteps = 0;
        }
    }
}
