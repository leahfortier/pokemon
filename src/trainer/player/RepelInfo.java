package trainer.player;

import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import map.triggers.ChoiceTrigger;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.Messages;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.ChoiceActionMatcher;
import pattern.action.ChoiceMatcher;
import pattern.action.ActionMatcher.TriggerActionMatcher;

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
                Messages.add("The effects of repel have worn off.");

                Player player = Game.getPlayer();
                Bag bag = player.getBag();
                if (bag.hasItem(this.repelItem)) {
                    TriggerActionMatcher useAnotherAction = new TriggerActionMatcher(TriggerType.USE_ITEM, repelItem.name());
                    ChoiceMatcher useAnother = new ChoiceMatcher("Sure!", new ActionMatcher[] { useAnotherAction });

                    TriggerActionMatcher doNotUseAction = new TriggerActionMatcher(TriggerType.DIALOGUE, "They're coming for you. Worry.");
                    ChoiceMatcher doNotUse = new ChoiceMatcher("Nah...", new ActionMatcher[] { doNotUseAction });

                    ChoiceActionMatcher choice = new ChoiceActionMatcher(
                            "Would you like to use another " + repelItem.getName() + "?",
                            new ChoiceMatcher[] { useAnother, doNotUse }
                    );

                    Trigger choiceTrigger = new ChoiceTrigger(choice, null);
                    Messages.add(new MessageUpdate().withTrigger(choiceTrigger.getName()));
                }
            }
        } else {
            repelSteps = 0;
        }
    }
}
