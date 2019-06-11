package item.bag;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;
import item.Item;
import item.ItemNamesies;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.MoveUseItem;
import item.use.UseItem;
import main.Game;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.active.PartyPokemon;
import trainer.player.Player;
import util.serialization.Serializable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class Bag implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_FAIL_MESSAGE = "It won't have any effect.";

    private final Map<ItemNamesies, Integer> items; // Item -> quantity
    private final Map<BagCategory, Set<ItemNamesies>> bag;
    private final Map<BattleBagCategory, Set<ItemNamesies>> battleBag;

    // TODO: This doesn't work for Pokemon Use Items -- it automatically selects the front Pokemon
    private ItemNamesies lastUsedItem; // Only for battle

    public Bag() {
        this.items = new EnumMap<>(ItemNamesies.class);
        this.bag = new EnumMap<>(BagCategory.class);
        this.battleBag = new EnumMap<>(BattleBagCategory.class);
        this.lastUsedItem = ItemNamesies.NO_ITEM;

        for (BagCategory category : BagCategory.values()) {
            this.bag.put(category, EnumSet.noneOf(ItemNamesies.class));
        }

        for (BattleBagCategory category : BattleBagCategory.values()) {
            this.battleBag.put(category, EnumSet.noneOf(ItemNamesies.class));
        }
    }

    public boolean giveItem(PartyPokemon p, ItemNamesies hold) {
        if (p.isEgg()) {
            Messages.add("You can't give an item to an egg!");
            return false;
        }

        ItemNamesies item = p.getActualHeldItem().namesies();
        if (item != ItemNamesies.NO_ITEM) {
            addItem(item);
            p.removeItem();
            Messages.add("Took the " + item.getName() + " from " + p.getActualName() + ".");
        }

        p.giveItem(hold);
        removeItem(hold);
        Messages.add(p.getActualName() + " is now holding " + hold.getName() + ".");

        return true;
    }

    public boolean takeItem(PartyPokemon p) {
        if (p.isEgg()) {
            Messages.add("Eggs can't hold anything. They're eggs.");
            return false;
        }

        ItemNamesies item = p.getActualHeldItem().namesies();
        if (item == ItemNamesies.NO_ITEM) {
            Messages.add(p.getActualName() + " is not holding anything.");
            return false;
        }

        // Remove the item from the pokemon and add it to the bag
        p.removeItem();
        addItem(item);
        Messages.add("Took the " + item.getName() + " from " + p.getActualName() + ".");
        return true;
    }

    public void addItem(ItemNamesies item) {
        addItem(item, 1);
    }

    public void addItem(ItemNamesies item, int amount) {
        // Increment the items by the amount
        if (items.containsKey(item)) {
            items.put(item, items.get(item) + amount);
        } else {
            items.put(item, amount);
            Item itemValue = item.getItem();
            this.getCategory(itemValue.getBagCategory()).add(item);
            for (BattleBagCategory category : itemValue.getBattleBagCategories()) {
                this.getCategory(category).add(item);
            }
        }
    }

    public Set<ItemNamesies> getCategory(BagCategory category) {
        return bag.get(category);
    }

    public Set<ItemNamesies> getCategory(BattleBagCategory category) {
        return battleBag.get(category);
    }

    public void removeItem(ItemNamesies item) {
        // Trying to remove nonexistent items -- bad news
        if (!items.containsKey(item) || items.get(item) <= 0) {
            Global.error("You can't remove an item you don't have! (" + item.getName() + ")");
        }

        Item itemValue = item.getItem();

        // Don't decrement for TMs or KeyItems
        if (!itemValue.hasQuantity()) {
            if (items.get(item) != 1) {
                Global.error("Must only have exactly quantity per TM/KeyItem");
            }

            return;
        }

        // All other items -- decrement by one
        items.put(item, items.get(item) - 1);

        // If this was the last one, remove from maps
        if (items.get(item) == 0) {
            items.remove(item);
            this.getCategory(itemValue.getBagCategory()).remove(item);
            for (BattleBagCategory category : itemValue.getBattleBagCategories()) {
                this.getCategory(category).remove(item);
            }
        }
    }

    public boolean usePlayerItem(ItemNamesies item) {
        return this.useItem(null, item, null, null);
    }

    public boolean usePokemonItem(ItemNamesies item, PartyPokemon p) {
        return this.useItem(null, item, p, null);
    }

    public boolean useMoveItem(ItemNamesies item, PartyPokemon p, Move move) {
        return this.useItem(null, item, p, move);
    }

    // Checks conditions, adds messages, and executes the UseItem
    public boolean battleUseItem(ItemNamesies item, PartyPokemon p, Battle battle) {
        Player player = Game.getPlayer();
        Item useItem = item.getItem();

        if (useItem instanceof BallItem) {
            return player.catchPokemon(battle, (BallItem)useItem);
        } else if (useItem instanceof BattleUseItem) {
            boolean used = this.useItem(battle, item, p, null);
            if (used) {
                this.lastUsedItem = this.hasItem(item) ? item : ItemNamesies.NO_ITEM;
                ActivePokemon front = player.front();
                if (front == p) {
                    Messages.add(new MessageUpdate().updatePokemon(battle, front));
                }
            }
            return used;
        } else {
            Global.error("Invalid battle item " + item.getName());
            return false;
        }
    }

    // Move should be nonnull for MoveUseItem and null otherwise
    // Battle can be null or non-null
    private boolean useItem(Battle battle, ItemNamesies itemName, PartyPokemon p, Move move) {
        Item item = itemName.getItem();

        if (move != null && !(item instanceof MoveUseItem)) {
            Global.error("Move can only be non-null for MoveUseItem");
        }

        if (battle != null && !(item instanceof BattleUseItem)) {
            Global.error("Battle can only be non-null for BattleUseItem");
        }

        // Eggs can't do shit
        if (p != null && p.isEgg()) {
            Messages.add(DEFAULT_FAIL_MESSAGE);
            return false;
        }

        // Try to use the item
        UseItem useItem = (UseItem)item;
        final boolean success = useItem.use(battle, (ActivePokemon)p, move);

        // :(
        if (!success) {
            Messages.add(DEFAULT_FAIL_MESSAGE);
            return false;
        }

        // Item successfully used -- display success messages to the user and remove this item from the bag
        Messages.addToFront(Game.getPlayer().getName() + " used the " + item.getName() + "!");
        removeItem(itemName);
        return true;
    }

    public ItemNamesies getLastUsedItem() {
        return lastUsedItem;
    }

    public boolean hasItem(ItemNamesies item) {
        return this.getQuantity(item) > 0;
    }

    public int getQuantity(ItemNamesies item) {
        if (items.containsKey(item)) {
            return items.get(item);
        }

        return 0;
    }
}
