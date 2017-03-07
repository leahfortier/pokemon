package trainer.player;

import item.ItemNamesies;
import message.Messages;

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
            }
        }
        else {
            repelSteps = 0;
        }
    }
}
