package map.triggers;

import item.Item;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Game;
import main.Global;
import pokemon.ActivePokemon;

public class GiveTrigger extends Trigger
{	
	public static final Pattern pokemonTriggerPattern = Pattern.compile("(pokemon:)\\s*([A-Za-z \\t0-9,:.\\-'*]*)");
	
	ArrayList<Item> itemList;
	ArrayList<ActivePokemon> pokemonList;
	
	public GiveTrigger(String name, String contents) 
	{
		super(name, contents);
		itemList = new ArrayList<Item>();
		
		Matcher m = variablePattern.matcher(contents);		
		while (m.find()){
			String type = m.group(1);
			if (type.equals("item"))
				if (Item.isItem(m.group(2)))
					itemList.add(Item.getItemFromName(m.group(2)));
				else
					Global.error("Invalid item: " + m.group(2));
		}
		
		pokemonList = new ArrayList<>();
		m = pokemonTriggerPattern.matcher(contents);
		while (m.find())
		{
			if(m.group(1) != null)
			{
				pokemonList.add(ActivePokemon.createActivePokemon(m.group(2), true));
			}
		}
	}

	public void execute(Game game) 
	{
		super.execute(game);
		for (Item i: itemList)
		{
			game.charData.addItem(i);
		}
		
		for (ActivePokemon p: pokemonList) 
		{
			game.charData.addPokemon(null, p);
		}
	}

	public String toString() 
	{
		StringBuilder ret = new StringBuilder();
		ret.append("GiveTrigger: " + name + "\n"); 
		
		ret.append("Item List: ");
		for (Item i: itemList)
		{
			ret.append(i.getName() + " ");
		}
		
		ret.append("\nPokemon: ");
		for (ActivePokemon p: pokemonList) 
		{
			ret.append(p.getPokemonInfo().getName() + " ");
		}
		
		return ret.toString();
	}
}
