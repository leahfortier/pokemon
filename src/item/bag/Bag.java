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
import trainer.Trainer;
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

    // Only for battle
    private ItemNamesies lastUsedItem;
    private ItemNamesies selectedBattleItem;
    private PartyPokemon battleItemRecipient;

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

    public void giveItem(PartyPokemon p, ItemNamesies hold) {
        if (p.isEgg()) {
            Messages.add("You can't give an item to an egg!");
            return;
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
    }

    public void takeItem(PartyPokemon p) {
        if (p.isEgg()) {
            Messages.add("Eggs can't hold anything. They're eggs.");
            return;
        }

        ItemNamesies item = p.getActualHeldItem().namesies();
        if (item == ItemNamesies.NO_ITEM) {
            Messages.add(p.getActualName() + " is not holding anything.");
            return;
        }

        // Remove the item from the pokemon and add it to the bag
        p.removeItem();
        addItem(item);
        Messages.add("Took the " + item.getName() + " from " + p.getActualName() + ".");
    }

    public void addItem(ItemNamesies item) {
        addItem(item, 1);
    }

    public void addItem(ItemNamesies item, int amount) {
        if (amount < 1) {
            Global.error("Invalid add amount " + amount + " -- must be positive");
        }

        Item itemValue = item.getItem();

        // Don't increase for TMs or KeyItems
        if (!itemValue.hasQuantity()) {
            if (amount > 1) {
                Global.error("Cannot add multiple TM/KeyItems: " + item.getName() + " " + amount);
            } else if (this.hasItem(item)) {
                return;
            }
        }

        // Increment the items by the amount
        if (items.containsKey(item)) {
            items.put(item, items.get(item) + amount);
        } else {
            items.put(item, amount);
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
        this.removeItem(item, 1);
    }

    public void removeItem(ItemNamesies item, int amount) {
        if (amount < 1) {
            Global.error("Invalid remove amount " + amount + " -- must be positive");
        }

        // Trying to remove nonexistent items -- bad news
        if (!items.containsKey(item) || items.get(item) < amount) {
            Global.error("You can't remove an item you don't have! (" + item.getName() + ")");
        }

        Item itemValue = item.getItem();

        // Don't decrement for TMs or KeyItems
        if (!itemValue.hasQuantity()) {
            if (items.get(item) != 1 || amount > 1) {
                Global.error("Must only have exactly quantity per TM/KeyItem");
            }

            return;
        }

        // All other items -- decrement by amount
        items.put(item, items.get(item) - amount);

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
        return this.useItem(item, null, null);
    }

    public boolean usePokemonItem(ItemNamesies item, PartyPokemon p) {
        return this.useItem(item, p, null);
    }

    public boolean useMoveItem(ItemNamesies item, PartyPokemon p, Move move) {
        return this.useItem(item, p, move);
    }

    public void setSelectedBattleItem(ItemNamesies item, PartyPokemon recipient) {
        this.selectedBattleItem = item;
        this.battleItemRecipient = recipient;
    }

    // Checks conditions, adds messages, and executes the UseItem
    public boolean battleUseItem(Battle battle, Trainer trainer) {
        if (this.selectedBattleItem == null) {
            Global.error("Cannot use a battle item that was never selected!");
            return false;
        }

        ItemNamesies item = this.selectedBattleItem;
        PartyPokemon p = this.battleItemRecipient;
        this.selectedBattleItem = null;
        this.battleItemRecipient = null;

        Item itemValue = item.getItem();

        boolean used;
        if (itemValue instanceof BallItem) {
            Player player = (Player)trainer;
            used = player.catchPokemon(battle, (BallItem)itemValue);
            if (used) {
                removeItem(item);
            }
        } else if (itemValue instanceof BattleUseItem) {
            Messages.add(trainer.getName() + " used the " + item.getName() + "!");
            used = this.useItemNoMessages(battle, item, p, null);
            if (!used) {
                Messages.add("But it had no effect!");
            }
        } else {
            Global.error("Invalid battle item " + item.getName());
            return false;
        }

        if (used) {
            ActivePokemon front = trainer.front();
            if (front == p) {
                Messages.add(new MessageUpdate().updatePokemon(battle, front));
            }

            // Update last move used
            this.lastUsedItem = this.hasItem(item) ? item : ItemNamesies.NO_ITEM;
        }

        return used;
    }

    // Uses the item and adds the messages
    // Move should be nonnull for MoveUseItem and null otherwise
    // Not intended for battle use
    private boolean useItem(ItemNamesies itemName, PartyPokemon p, Move move) {
        boolean success = this.useItemNoMessages(null, itemName, p, move);
        if (success) {
            Messages.addToFront(Game.getPlayer().getName() + " used the " + itemName.getName() + "!");
        } else {
            Messages.add(DEFAULT_FAIL_MESSAGE);
        }
        return success;
    }

    // Uses the item and returns if used successfully
    // If successful, will remove the item from the bag
    // Includes messages from the item itself but not messages like "<Player> used <item>!" or "It won't have any effect."
    // Move should be nonnull for MoveUseItem and null otherwise
    // Battle can be null or non-null
    private boolean useItemNoMessages(Battle battle, ItemNamesies itemName, PartyPokemon p, Move move) {
        Item item = itemName.getItem();

        if (move != null && !(item instanceof MoveUseItem)) {
            Global.error("Move can only be non-null for MoveUseItem");
        }

        if (battle != null && !(item instanceof BattleUseItem)) {
            Global.error("Battle can only be non-null for BattleUseItem");
        }

        // Eggs can't do shit
        if (p != null && p.isEgg()) {
            return false;
        }

        // Try to use the item
        UseItem useItem = (UseItem)item;
        final boolean success = useItem.use(battle, (ActivePokemon)p, move);

        // Item successfully used -- remove this item from the bag
        if (success) {
            removeItem(itemName);
        }

        return success;
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
