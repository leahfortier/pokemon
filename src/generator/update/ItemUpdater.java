package generator.update;

import generator.GeneratorType;
import generator.update.ItemUpdater.ItemParser;
import item.ItemNamesies;
import item.use.TechnicalMachine;
import type.Type;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ItemUpdater extends GeneratorUpdater<ItemNamesies, ItemParser> {
    public ItemUpdater() {
        super(GeneratorType.ITEM_GEN, "items");
    }

    @Override
    protected Map<ItemNamesies, ItemParser> createEmpty() {
        return new EnumMap<>(ItemNamesies.class);
    }

    @Override
    protected Set<ItemNamesies> getToParse() {
        Set<ItemNamesies> toParse = EnumSet.allOf(ItemNamesies.class);
        toParse.remove(ItemNamesies.NO_ITEM);
        toParse.remove(ItemNamesies.SYRUP);
        toParse.remove(ItemNamesies.SURFBOARD);
        toParse.remove(ItemNamesies.RUBY);
        toParse.remove(ItemNamesies.HARDY_MINT);
        toParse.remove(ItemNamesies.DOCILE_MINT);
        toParse.remove(ItemNamesies.BASHFUL_MINT);
        toParse.remove(ItemNamesies.QUIRKY_MINT);
        toParse.removeIf(itemNamesies -> itemNamesies.getItem() instanceof TechnicalMachine);
        return toParse;
    }

    @Override
    protected ItemNamesies getNamesies(String name) {
        return ItemNamesies.getValueOf(name);
    }

    @Override
    protected String getName(ItemNamesies namesies) {
        return namesies.getName();
    }

    @Override
    protected String getDescription(ItemNamesies namesies) {
        return namesies.getItem().getDescription();
    }

    @Override
    protected ItemParser createParser(Scanner in) {
        return new ItemParser(in);
    }

    // Contains the raw information about an item as parsed from serebii
    // Although serebii kind of sucks especially for price so this is kind of useless oh well
    public class ItemParser extends BaseParser {
        public final ItemNamesies itemNamesies;
        public final String itemType;

        public final int fling;
        public final int price;

        public final Type naturalGiftType;
        public final int naturalGiftPower;

        private ItemParser(Scanner in) {
            name = in.nextLine().trim();
            itemType = in.nextLine().trim();

            fling = Integer.parseInt(in.nextLine().trim());
            price = Integer.parseInt(in.nextLine().trim());

            naturalGiftType = Type.valueOf(in.nextLine().trim().toUpperCase());
            naturalGiftPower = Integer.parseInt(in.nextLine().trim());

            description = in.nextLine().trim();

            itemNamesies = getNamesies(name);
        }
    }
}
