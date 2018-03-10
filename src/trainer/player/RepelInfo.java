package trainer.player;

import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import map.triggers.ChoiceTrigger;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.Messages;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.ChoiceActionMatcher;
import pattern.action.ActionMatcher.UseItemActionMatcher;
import pattern.action.ChoiceMatcher;
import pattern.action.StringActionMatcher.DialogueActionMatcher;
import util.serialization.Serializable;

public class RepelInfo implements Serializable {
    private static final long serialVersionUID = 1L;

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
                    ActionMatcher useAnotherAction = new UseItemActionMatcher(repelItem);
                    ChoiceMatcher useAnother = new ChoiceMatcher("Sure!", new ActionMatcher[] { useAnotherAction });

                    ActionMatcher doNotUseAction = new DialogueActionMatcher("They're coming for you. Worry.");
                    ChoiceMatcher doNotUse = new ChoiceMatcher("Nah...", new ActionMatcher[] { doNotUseAction });

                    ChoiceActionMatcher choice = new ChoiceActionMatcher(
                            "Would you like to use another " + repelItem.getName() + "?",
                            new ChoiceMatcher[] { useAnother, doNotUse }
                    );

                    Trigger choiceTrigger = new ChoiceTrigger(choice);
                    Messages.add(new MessageUpdate().withTrigger(choiceTrigger));
                }
            }
        } else {
            repelSteps = 0;
        }
    }
}
