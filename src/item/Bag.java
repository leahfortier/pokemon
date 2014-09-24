package item;

import item.hold.HoldItem;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.MoveUseItem;
import item.use.PokemonUseItem;
import item.use.TrainerUseItem;
import item.use.UseItem;

import java.awt.Color;
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

public class Bag implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private Map<Item, Integer> items; // Item -> quantity
	private Map<BagCategory, Set<Item>> bag;
	private Map<BattleBagCategory, Set<Item>> battleBag;
	private Item lastUsedItem;

	public enum BagCategory implements Serializable
	{
		MEDICINE("Medicine", new Color(248, 120, 64), 0x1), 
		BALL("Balls", new Color(232, 184, 40), 0x2), 
		STAT("Stat", new Color(80, 128, 232), 0x3), 
		KEY_ITEM("KeyItems", new Color(152, 88, 240), 0x4), 
		TM("TMs", new Color(168, 232, 72), 0x5), 
		BERRY("Berries", new Color(64, 192, 64), 0x6), 
		MISC("Misc", new Color(232, 136, 192), 0x7);
		
		// Name to display in BagView
		private String name;
		private Color color;
		private int imageNumber;
		
		private BagCategory(String name, Color color, int imageNumber)
		{
			this.name = name;
			this.color = color;
			this.imageNumber = imageNumber;
		}
		
		public String getName()
		{
			return name;
		}
		
		public Color getColor()
		{
			return color;
		}
		
		public int getImageNumber()
		{
			return imageNumber;
		}
	}
	
	public enum BattleBagCategory implements Serializable
	{
		HPPP("HP/PP", 0x16), STATUS("Status", 0x17), BALL("Balls", 0x18), BATTLE("Battle", 0x19);
		
		// Name to display in BagView
		String name;
		int imageNumber;
		
		private BattleBagCategory(String name, int imgNum)
		{
			this.name = name;
			this.imageNumber = imgNum;
		}
		
		public String getName()
		{
			return name;
		}
		
		public int getImageNumber()
		{
			return imageNumber;
		}
	}
	
	public Bag()
	{
		items = new TreeMap<>();
		
		bag = new HashMap<>();
		for (BagCategory cat : BagCategory.values())
		{
			bag.put(cat, new TreeSet<Item>());
		}
		
		battleBag = new HashMap<>();
		for (BattleBagCategory cat : BattleBagCategory.values())
		{
			battleBag.put(cat, new TreeSet<Item>());
		}
		
		lastUsedItem = Item.noneItem();
	}
	
	public String giveItem(CharacterData player, ActivePokemon p, Item hold)
	{
		if (p.isEgg()) 
		{
			return "You can't give an item to an egg!";
		}
		
		String s = "";
		
		Item item = p.getActualHeldItem();
		if (item != Item.noneItem()) 
		{
			player.getBag().addItem(item);
			p.removeItem();
			s += "Took the " + item.getName() + " from " + p.getName() + ". ";
		}
		
		p.giveItem((HoldItem)hold);
		removeItem(hold);
		s += p.getName() + " is now holding " + hold.getName() + ".";
		
		return s;
	}
	
	public String takeItem(CharacterData player, ActivePokemon p)
	{
		if (p.isEgg()) return "Eggs can't hold anything. They're eggs.";
		
		Item item = p.getActualHeldItem();
		if (item != Item.noneItem()) 
		{
			player.getBag().addItem(item);
			p.removeItem();
			return "Took the " + item.getName() + " from " + p.getName() + ".";
		}
		
		return p.getName() + " is not holding anything.";
	}
	
	public void addItem(Item i)
	{
		addItem(i, 1);
	}
	
	public void addItem(Item i, int amt)
	{
		if (items.containsKey(i)) items.put(i, items.get(i) + amt);
		else items.put(i, amt);
		
		for (Set<Item> set : getAllCategorySets(i)) set.add(i);
	}
	
	private List<Set<Item>> getAllCategorySets(Item i)
	{
		List<Set<Item>> res = new ArrayList<Set<Item>>();
		res.add(bag.get(i.cat));
		for (BattleBagCategory c : i.bcat) res.add(battleBag.get(c));
		return res;
	}
	
	private void removeItem(Item i)
	{
		if (items.containsKey(i)) items.put(i, items.get(i) - 1);
		else Global.error("Can't remove an item you don't have! (" + i.getName() + ")");

		if (items.get(i) <= 0)
		{
			for (Set<Item> set : getAllCategorySets(i))
			{			
				set.remove(i);
			}			
		}

	}
	
	public boolean useItem(Item i, Trainer t)
	{
		if (items.get(i) <= 0) Global.error("You can't use that item (" + i.getName() + ") as you do not have no more.");
		
		boolean res = false;
		if (i instanceof TrainerUseItem) 
			res |= ((TrainerUseItem)i).use(t);
		
		if (res) removeItem(i);
		return res;
	}
	
	public boolean useItem(Item i, ActivePokemon p)
	{
		if (items.get(i) <= 0) Global.error("You can't use that item (" + i.getName() + ") as you do not have no more.");
		
		boolean res = false;
		if (i instanceof PokemonUseItem) 
			res |= ((PokemonUseItem)i).use(p);
		
		if (res) removeItem(i);
		return res;
	}
	
	public boolean useMoveItem(Item i, Move m)
	{
		if (items.get(i) <= 0) Global.error("You can't use that item (" + i.getName() + ") as you do not have no more.");
		
		boolean res = false;
		if (i instanceof MoveUseItem) 
			res |= ((MoveUseItem)i).use(m);
		
		if (res) removeItem(i);
		return res;
	}
	
	public boolean battleUseItem(Item i, ActivePokemon p, Battle b)
	{
		if (items.get(i) <= 0) Global.error("You can't use that item (" + i.getName() + ") as you do not have no more.");
		
		boolean res = false;
		
		if (i instanceof BattleUseItem && b != null) 
			res |= ((BattleUseItem)i).use(p, b);
		else if (i instanceof PokemonUseItem)
		{
			System.err.println("PokemonUseItem called from Bag.battleUseItem() instead of BattleUseItem.");
			res |= ((PokemonUseItem)i).use(p);
		}
		else if (i instanceof BallItem)
			res |= b.getPlayer().catchPokemon(b, (BallItem)i);
		
		if (res)
		{
			if (i instanceof UseItem)
			{
				boolean front = b.getPlayer().front() == p;
				
				b.addMessage(((Trainer)b.getTrainer(p.user())).getName() + " used " + i.name + "!");
				b.addMessage(((UseItem)i).getSuccessMessage(p));
				if (front) b.addMessage("", p.getHP(), p.user());
				if (front) b.addMessage("", p.getStatus().getType(), p.user());
			}
			
			if (items.get(i) > 1) lastUsedItem = i;
			else lastUsedItem = Item.noneItem();
			
			removeItem(i);
		}
		else if (i instanceof UseItem) b.addMessage("It won't have any effect.");

		return res;
	}
	
	public Set<Item> getCategory(BagCategory bc)
	{		
		return bag.get(bc);
	}
	
	public Set<Item> getCategory(BattleBagCategory bc)
	{
		return battleBag.get(bc);
	}
	
	public Item getLastUsedItem()
	{
		return lastUsedItem;
	}
	
	public int getQuantity(Item i)
	{
		if (items.containsKey(i)) return items.get(i);
		
		return 0;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (BagCategory bc : BagCategory.values()) {
			sb.append(bc.name);
			sb.append("\n");
			for (Item i : items.keySet())
			{
				sb.append('\t');
				sb.append(i.getName());
				sb.append('\n');
			}
		}
		
		return sb.substring(0, sb.length() - 1);
	}
}
