package item.bag;

import battle.Battle;
import battle.attack.Move;
import item.Item;
import item.ItemNamesies;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.MoveUseItem;
import item.use.PokemonUseItem;
import item.use.TrainerUseItem;
import item.use.UseItem;
import main.Game;
import main.Global;
import message.Messages;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import trainer.Trainer;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class Bag implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<ItemNamesies, Integer> items; // Item -> quantity
	private Map<BagCategory, Set<ItemNamesies>> bag;
	private Map<BattleBagCategory, Set<ItemNamesies>> battleBag;
	private ItemNamesies lastUsedItem;
	
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
	
	public String giveItem(ActivePokemon p, ItemNamesies hold) {
		if (p.isEgg()) {
			return "You can't give an item to an egg!";
		}
		
		String message = "";

		ItemNamesies item = p.getActualHeldItem().namesies();
		if (item != ItemNamesies.NO_ITEM) {
			addItem(item);
			p.removeItem();
			message += "Took the " + item.getName() + " from " + p.getActualName() + ".";
		}
		
		p.giveItem(hold);
		removeItem(hold);
		message += p.getActualName() + " is now holding " + hold.getName() + ".";
		
		return message;
	}
	
	public String takeItem(ActivePokemon p) {
		if (p.isEgg()) {
			return "Eggs can't hold anything. They're eggs.";
		}
		
		ItemNamesies item = p.getActualHeldItem().namesies();
		if (item != ItemNamesies.NO_ITEM) {
			addItem(item);
			p.removeItem();
			return "Took the " + item.getName() + " from " + p.getActualName() + ".";
		}
		
		return p.getActualName() + " is not holding anything.";
	}
	
	public void addItem(ItemNamesies item) {
		addItem(item, 1);
	}
	
	public void addItem(ItemNamesies item, int amount) {
		// Increment the items by the amount
		if (items.containsKey(item)) {
			items.put(item, items.get(item) + amount);
		}
		else {
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
	
	private void removeItem(ItemNamesies item) {
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
	
	public boolean useItem(ItemNamesies item, Trainer trainer) {
		// This theoretically should not be possible anymore but I can't bring myself to remove error checking
		if (items.get(item) <= 0) {
			Global.error("You can't use that item (" + item.getName() + ") as you do not have no more.");
		}

		Item useItem = item.getItem();
		if (useItem instanceof TrainerUseItem && ((TrainerUseItem)useItem).use(trainer)) {
			removeItem(item);
			return true;
		}
		
		return false;
	}

	public boolean useItem(CharacterData player, ItemNamesies item, ActivePokemon p) {
		// This theoretically should not be possible anymore but I can't bring myself to remove error checking
		if (items.get(item) <= 0) {
			Global.error("You can't use that item (" + item.getName() + ") as you do not have no more.");
		}

		Item useItem = item.getItem();
		if (useItem instanceof PokemonUseItem && ((PokemonUseItem)useItem).use(player, p)) {
			removeItem(item);
			return true;
		}

		return false;
	}

	public boolean useMoveItem(ItemNamesies item, ActivePokemon p, Move m) {
		if (items.get(item) <= 0) {
			Global.error("You can't use that item (" + item.getName() + ") as you do not have no more.");
		}

		Item useItem = item.getItem();
		if (useItem instanceof MoveUseItem && ((MoveUseItem)useItem).use(p, m)) {
			removeItem(item);
			return true;
		}

		return false;
	}

	public boolean battleUseItem(ItemNamesies item, ActivePokemon activePokemon, Battle battle) {
		if (items.get(item) <= 0) {
			Global.error("You can't use that item (" + item.getName() + ") as you do not have no more.");
		}

		Item useItem = item.getItem();
		final boolean used;
		if (useItem instanceof BattleUseItem && battle != null)  {
			used = ((BattleUseItem) useItem).use(activePokemon, battle);
		} else if (useItem instanceof PokemonUseItem) {
			System.err.println("PokemonUseItem called from Bag.battleUseItem() instead of BattleUseItem.");
			used = ((PokemonUseItem) useItem).use(Game.getPlayer(), activePokemon);
		} else if (useItem instanceof BallItem) {
			used = Game.getPlayer().catchPokemon(battle, (BallItem) useItem);
		} else {
			used = false;
		}

		if (used) {
			if (useItem instanceof UseItem) {
				boolean front = Game.getPlayer().front() == activePokemon;

				// TODO: This is made to look generalized for an enemy trainer using an item, but this method is inside Bag, which is only valid for the player
				Messages.addMessage(((Trainer)battle.getTrainer(activePokemon.user())).getName() + " used " + useItem.getName() + "!");
				Messages.addMessage(((UseItem)useItem).getSuccessMessage(activePokemon));
				
				if (front) {
					Messages.addMessage("", battle, activePokemon);
				}
			}
			
			if (items.get(item) > 1) {
				lastUsedItem = item;
			}
			else {
				lastUsedItem = ItemNamesies.NO_ITEM;
			}
			
			removeItem(item);
		}
		else if (useItem instanceof UseItem) {
			Messages.addMessage("It won't have any effect.");
		}

		return used;
	}

	public ItemNamesies getLastUsedItem() {
		return lastUsedItem;
	}
	
	public int getQuantity(ItemNamesies item) {
		if (items.containsKey(item)) {
			return items.get(item);
		}
		
		return 0;
	}
}
