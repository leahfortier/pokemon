package pokemon;

import item.ItemNamesies;
import item.hold.HoldItem;
import util.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class WildHoldItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final HoldItem item;
    private final int chance;

    WildHoldItem(int chance, ItemNamesies itemName) {
        item = (HoldItem) itemName.getItem();
        this.chance = chance;
    }

    static List<WildHoldItem> createList(Scanner in) {
        List<WildHoldItem> list = new ArrayList<>();
        int num = in.nextInt();
        in.nextLine();

        for (int i = 0; i < num; i++) {
            list.add(new WildHoldItem(in.nextInt(), ItemNamesies.getValueOf(in.nextLine().trim())));
        }

        return list;
    }

    static HoldItem getWildHoldItem(List<WildHoldItem> list) {
        int random = RandomUtils.getRandomInt(100);
        int sum = 0;

        for (WildHoldItem i : list) {
            sum += i.chance;
            if (random < sum) {
                return i.item;
            }
        }

        return (HoldItem)ItemNamesies.NO_ITEM.getItem();
    }
}
