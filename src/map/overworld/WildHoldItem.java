package map.overworld;

import battle.ActivePokemon;
import item.ItemNamesies;
import main.Global;
import pokemon.species.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import util.RandomUtils;
import util.serialization.Serializable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class WildHoldItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ItemNamesies item;
    private final Chance chance;

    private WildHoldItem(int chance, ItemNamesies itemName) {
        if (!itemName.getItem().isHoldable()) {
            Global.error("Cannot have a wild hold item that is not holdable: " + itemName.getName());
        }

        this.item = itemName;
        this.chance = Chance.getChance(chance);
    }

    public ItemNamesies getItem() {
        return item.getItem().namesies();
    }

    @Override
    public String toString() {
        return this.item.getName() + " " + this.chance;
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

    static ItemNamesies getWildHoldItem(PokemonNamesies pokemon, ActivePokemon playerFront) {
        boolean compoundEyes = playerFront.hasAbility(AbilityNamesies.COMPOUNDEYES);
        int random = RandomUtils.getRandomInt(100);
        int sum = 0;

        // Sort with rarest items first
        List<WildHoldItem> list = pokemon.getInfo().getWildItems();
        list.sort(Comparator.comparingInt(wildHoldItem -> wildHoldItem.chance.chance));

        for (WildHoldItem wildHoldItem : list) {
            Chance chance = wildHoldItem.chance;
            sum += compoundEyes ? chance.increasedChance : chance.chance;
            if (random < sum) {
                return wildHoldItem.item;
            }
        }

        return ItemNamesies.NO_ITEM;
    }

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

        @Override
        public String toString() {
            return this.chance + "% (" + this.increasedChance + "%)";
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
}
