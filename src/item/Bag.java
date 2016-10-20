package item;

import item.hold.HoldItem;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.MoveUseItem;
import item.use.PokemonUseItem;
import item.use.TrainerUseItem;
import item.use.UseItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import main.Global;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import trainer.Trainer;
import battle.Battle;
import battle.Move;

public class Bag implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Map<Item, Integer> items; // Item -> quantity
	private Map<BagCategory, Set<Item>> bag;
	private Map<BattleBagCategory, Set<Item>> battleBag;
	private Item lastUsedItem;
	
	public Bag() {
		items = new TreeMap<>();
		
		bag = new HashMap<>();
		for (BagCategory cat : BagCategory.values()) {
			bag.put(cat, new TreeSet<>());
		}
		
		battleBag = new HashMap<>();
		for (BattleBagCategory cat : BattleBagCategory.values()) {
			battleBag.put(cat, new TreeSet<>());
		}
		
		lastUsedItem = Item.noneItem();
	}
	
	public String giveItem(CharacterData player, ActivePokemon p, Item hold) {
		if (p.isEgg()) {
			return "You can't give an item to an egg!";
		}
		
		String s = "";
		
		Item item = p.getActualHeldItem();
		if (item != Item.noneItem()) {
			player.getBag().addItem(item);
			p.removeItem();
			s += "Took the " + item.getName() + " from " + p.getActualName() + ". ";
		}
		
		p.giveItem((HoldItem)hold);
		removeItem(hold);
		s += p.getActualName() + " is now holding " + hold.getName() + ".";
		
		return s;
	}
	
	public String takeItem(CharacterData player, ActivePokemon p) {
		if (p.isEgg()) {
			return "Eggs can't hold anything. They're eggs.";
		}
		
		Item item = p.getActualHeldItem();
		if (item != Item.noneItem()) {
			player.getBag().addItem(item);
			p.removeItem();
			return "Took the " + item.getName() + " from " + p.getActualName() + ".";
		}
		
		return p.getActualName() + " is not holding anything.";
	}
	
	public void addItem(Item i) {
		addItem(i, 1);
	}
	
	public void addItem(Item i, int amt) {
		// Increment the items by the amount
		if (items.containsKey(i)) {
			items.put(i, items.get(i) + amt);
		}
		else {
			items.put(i, amt);
		}
		
		// Update lists
		for (Set<Item> set : getAllCategorySets(i)) {
			set.add(i);
		}
	}

	private List<Set<Item>> getAllCategorySets(Item item) {
		List<Set<Item>> categories = new ArrayList<>();
		categories.add(bag.get(item.getBagCategory()));
		for (BattleBagCategory battleBagCategory : item.getBattleBagCategories()) {
			categories.add(battleBag.get(battleBagCategory));
		}

		return categories;
	}
	
	private void removeItem(Item item) {
		// Trying to remove nonexistent items -- bad news
		if (!items.containsKey(item) || items.get(item) <= 0) {
			Global.error("You can't remove an item you don't have! (" + item.getName() + ")");
		}
		
		// Don't decrement for TMs or KeyItems
		if (!item.hasQuantity()) {
			if (items.get(item) != 1) {
				Global.error("Must only have exactly quantity per TM/KeyItem");
			}
			
			return;
		}
		
		// All other items -- decrement by one and remove from sets
		items.put(item, items.get(item) - 1);

		if (items.get(item) == 0) {
			for (Set<Item> set : getAllCategorySets(item)) {
				set.remove(item);
			}			
		}

	}
	
	public boolean useItem(Item item, Trainer trainer) {
		if (items.get(item) <= 0) {
			Global.error("You can't use that item (" + item.getName() + ") as you do not have no more.");
		}
		
		if (item instanceof TrainerUseItem && ((TrainerUseItem)item).use(trainer)) {
			removeItem(item);
			return true;
		}
		
		return false;
	}
	
	public boolean useItem(CharacterData player, Item i, ActivePokemon p) {
		if (items.get(i) <= 0) {
			Global.error("You can't use that item (" + i.getName() + ") as you do not have no more.");
		}
		
		if (i instanceof PokemonUseItem && ((PokemonUseItem)i).use(player, p)) {
			removeItem(i);
			return true;
		}
		
		return false;
	}
	
	public boolean useMoveItem(Item i, ActivePokemon p, Move m) {
		if (items.get(i) <= 0) {
			Global.error("You can't use that item (" + i.getName() + ") as you do not have no more.");
		}
		
		if (i instanceof MoveUseItem && ((MoveUseItem)i).use(p, m)) {
			removeItem(i);
			return true;
		}
		
		return false;
	}
	
	public boolean battleUseItem(Item item, ActivePokemon activePokemon, Battle battle) {
		if (items.get(item) <= 0) {
			Global.error("You can't use that item (" + item.getName() + ") as you do not have no more.");
		}
		
		final boolean res;
		if (item instanceof BattleUseItem && battle != null)  {
			res = ((BattleUseItem) item).use(activePokemon, battle);
		}
		else if (item instanceof PokemonUseItem) {
			System.err.println("PokemonUseItem called from Bag.battleUseItem() instead of BattleUseItem.");
			res = ((PokemonUseItem) item).use(battle.getPlayer(), activePokemon);
		}
		else if (item instanceof BallItem) {
			res = battle.getPlayer().catchPokemon(battle, (BallItem) item);
		} else {
			res = false;
		}

		if (res) {
			if (item instanceof UseItem) {
				boolean front = battle.getPlayer().front() == activePokemon;
				
				battle.addMessage(((Trainer)battle.getTrainer(activePokemon.user())).getName() + " used " + item.name + "!");
				battle.addMessage(((UseItem)item).getSuccessMessage(activePokemon));
				
				if (front) {
					battle.addMessage("", activePokemon);
				}
			}
			
			if (items.get(item) > 1) {
				lastUsedItem = item;
			}
			else {
				lastUsedItem = Item.noneItem();
			}
			
			removeItem(item);
		}
		else if (item instanceof UseItem) {
			battle.addMessage("It won't have any effect.");
		}

		return res;
	}
	
	public Set<Item> getCategory(BagCategory bc) {
		return bag.get(bc);
	}
	
	public Set<Item> getCategory(BattleBagCategory bc) {
		return battleBag.get(bc);
	}
	
	public Item getLastUsedItem() {
		return lastUsedItem;
	}
	
	public int getQuantity(Item item) {
		if (items.containsKey(item)) {
			return items.get(item);
		}
		
		return 0;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (BagCategory bc : BagCategory.values()) {
			sb.append(bc.getName());
			sb.append("\n");
			
			for (Item i : items.keySet()) {
				sb.append('\t');
				sb.append(i.getName());
				sb.append('\n');
			}
		}
		
		return sb.substring(0, sb.length() - 1);
	}
}
