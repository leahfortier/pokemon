package map.overworld;

import item.ItemNamesies;
import item.hold.HoldItem;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.ability.AbilityNamesies;
import util.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WildHoldItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private enum Chance {
        ONE(1, 5),
        FIVE(5, 20),
        TEN(10, 30),
        FIFTY(50, 60),
        NINETY_FIVE(95, 100),
        HUNDRED(100, 100);

        private final int chance;
        private final int increasedChance;

        Chance(int chance, int increasedChance) {
            this.chance = chance;
            this.increasedChance = increasedChance;
        }

        static Chance getChance(int chance) {
            for (Chance chanceValue : Chance.values()) {
                if (chanceValue.chance == chance) {
                    return chanceValue;
                }
            }

            Global.error("Invalid wild hold item chance " + chance);
            return ONE;
        }
    }

    private final HoldItem item;
    private final Chance chance;

    private WildHoldItem(int chance, ItemNamesies itemName) {
        this.item = (HoldItem) itemName.getItem();
        this.chance = Chance.getChance(chance);
    }

    public ItemNamesies getItem() {
        return item.getItem().namesies();
    }

    public static List<WildHoldItem> createList(Scanner in) {
        List<WildHoldItem> list = new ArrayList<>();
        int num = in.nextInt();
        in.nextLine();

        for (int i = 0; i < num; i++) {
            list.add(new WildHoldItem(in.nextInt(), ItemNamesies.getValueOf(in.nextLine().trim())));
        }

        return list;
    }

    static HoldItem getWildHoldItem(ActivePokemon attacking, List<WildHoldItem> list) {
        boolean compoundEyes = attacking.hasAbility(AbilityNamesies.COMPOUNDEYES);
        int random = RandomUtils.getRandomInt(100);
        int sum = 0;

        // Sort with rarest items first
        list.sort((first, second) -> first.chance.chance - second.chance.chance);
        for (WildHoldItem wildHoldItem : list) {
            Chance chance = wildHoldItem.chance;
            sum += compoundEyes ? chance.increasedChance : chance.chance;
            if (random < sum) {
                return wildHoldItem.item;
            }
        }

        return (HoldItem)ItemNamesies.NO_ITEM.getItem();
    }
}
