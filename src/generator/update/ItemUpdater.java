package generator.update;

import generator.GeneratorType;
import item.ItemNamesies;
import item.use.TechnicalMachine;
import type.Type;
import util.file.FileIO;
import util.file.Folder;
import util.string.StringAppender;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ItemUpdater extends GeneratorUpdater {
    private static final String SCRIPTS_INPUT_FILE_NAME = Folder.SCRIPTS_COMPARE + "items.in";
    private static final String SCRIPTS_OUTPUT_FILE_NAME = Folder.SCRIPTS_COMPARE + "items.out";

    private final Map<ItemNamesies, ItemParser> parseItems;

    public ItemUpdater() {
        super(GeneratorType.ITEM_GEN);

        parseItems = new EnumMap<>(ItemNamesies.class);

        Scanner in = FileIO.openFile(SCRIPTS_OUTPUT_FILE_NAME);
        while (in.hasNext()) {
            ItemParser itemParser = new ItemParser(in);
            parseItems.put(itemParser.itemNamesies, itemParser);
        }
    }

    @Override
    public void writeScriptInputList() {
        Set<ItemNamesies> toParse = EnumSet.allOf(ItemNamesies.class);
        toParse.remove(ItemNamesies.NO_ITEM);
        toParse.remove(ItemNamesies.SYRUP);
        toParse.remove(ItemNamesies.SURFBOARD);
        toParse.remove(ItemNamesies.RUBY);
        toParse.removeIf(itemNamesies -> itemNamesies.getItem() instanceof TechnicalMachine);

        String out = new StringAppender()
                .appendJoin("\n", toParse, ItemNamesies::getName)
                .toString();
        FileIO.overwriteFile(SCRIPTS_INPUT_FILE_NAME, out);
    }

    @Override
    public String getNewDescription(String name) {
        ItemNamesies itemNamesies = ItemNamesies.getValueOf(name);
        ItemParser itemParser = parseItems.get(itemNamesies);
        if (itemParser != null) {
            return itemParser.description;
        } else {
            return itemNamesies.getItem().getDescription();
        }
    }

    public Iterable<ItemParser> getParseItems() {
        return parseItems.values();
    }

    // Contains the raw information about an item as parsed from serebii
    // Although serebii kind of sucks especially for price so this is kind of useless oh well
    public static class ItemParser {
        public final ItemNamesies itemNamesies;
        public final String itemType;

        public final int fling;
        public final int price;

        public final Type naturalGiftType;
        public final int naturalGiftPower;

        public final String description;

        private ItemParser(Scanner in) {
            itemNamesies = ItemNamesies.getValueOf(in.nextLine().trim());
            itemType = in.nextLine().trim();

            fling = Integer.parseInt(in.nextLine().trim());
            price = Integer.parseInt(in.nextLine().trim());

            naturalGiftType = Type.valueOf(in.nextLine().trim().toUpperCase());
            naturalGiftPower = Integer.parseInt(in.nextLine().trim());

            description = in.nextLine().trim();
        }
    }
}
