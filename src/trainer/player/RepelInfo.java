package trainer.player;

import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.Messages;
import pattern.action.ActionMatcher2;
import pattern.action.ActionMatcher2.ChoiceActionMatcher2;
import pattern.action.ActionMatcher2.TriggerActionMatcher2;
import pattern.action.ChoiceMatcher;
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
                Messages.add("The effects of repel have worn off.");

                Player player = Game.getPlayer();
                Bag bag = player.getBag();
                if (bag.hasItem(this.repelItem)) {
                    TriggerActionMatcher2 useAnotherAction = new TriggerActionMatcher2(TriggerType.USE_ITEM, repelItem.name());
                    ChoiceMatcher useAnother = new ChoiceMatcher("Sure!", new ActionMatcher2[] { useAnotherAction });

                    TriggerActionMatcher2 doNotUseAction = new TriggerActionMatcher2(TriggerType.DIALOGUE, "They're coming for you. Worry.");
                    ChoiceMatcher doNotUse = new ChoiceMatcher("Nah...", new ActionMatcher2[] { doNotUseAction });

                    ChoiceActionMatcher2 choice = new ChoiceActionMatcher2(
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
