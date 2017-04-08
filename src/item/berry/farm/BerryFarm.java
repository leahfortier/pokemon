package item.berry.farm;

import item.ItemNamesies;
import item.bag.Bag;
import main.Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BerryFarm implements Serializable {
    public static final int MAX_BERRIES = 24;

    private final List<BerryStats> berries;

    public BerryFarm() {
        this.berries = new ArrayList<>();
    }

    public BerryStats getBerry(int index) {
        if (index < 0 || index >= this.berries.size()) {
            return null;
        }

        return this.berries.get(index);
    }

    public String harvest(ItemNamesies selected) {
        Bag bag = Game.getPlayer().getBag();

        // Break at the first berry that can be harvested
        for (BerryStats berry : berries) {
            if (berry.isFinished()) {
                ItemNamesies berryKind = berry.getBerry().namesies();
                int harvestAmount = berry.getHarvestAmount();

                bag.addItem(berryKind, harvestAmount);
                berries.remove(berry);

                // TODO: Make plural for > 1
                return "Harvested " + harvestAmount + " " + berryKind.getName() + "!";
            }
        }

        // No berries to harvest, plant the selected berry
        if (berries.size() >= MAX_BERRIES) {
            return "Berry Farm is full.";
        }

        if (selected == ItemNamesies.NO_ITEM) {
            return "No berries to plant!!";
        }

        berries.add(new BerryStats(selected));
        bag.removeItem(selected);

        return "Planted a " + selected.getName() + "!";
    }
}
