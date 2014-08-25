package map.triggers;

import item.Item;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import battle.Attack;
import battle.Move;

import main.Game;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;

public class GiveTrigger extends Trigger
{	
	public static final Pattern pokemonPattern = Pattern.compile("(pokemon:)\\s*(?:(\\w+)\\s*(\\d+)([A-Za-z \\t0-9,:]*)|(RandomEgg))");
	
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
				if (Item.exists(m.group(2)))
					itemList.add(Item.getItem(m.group(2)));
				else
					Global.error("Invalid item: " + m.group(2));
		}
		
		pokemonList = new ArrayList<>();
		m = pokemonPattern.matcher(contents);
		while (m.find())
		{
			if (m.group(1) != null)
			{
				if (m.group(5) != null) 
				{
					pokemonList.add(new ActivePokemon(PokemonInfo.getRandomBaseEvolution()));
					continue;
				}
				
				PokemonInfo pinfo = PokemonInfo.getPokemonInfo(m.group(2));
				int level = Integer.parseInt(m.group(3));
				
				Matcher params = TrainerBattleTrigger.parameterPattern.matcher(m.group(4));

				boolean shiny = false;
				boolean setMoves = false;
				ArrayList<Move> moves = null;
				
				boolean isEgg = false;
				
				while (params.find())
				{
					if (params.group(1) != null) 
						shiny = true;
					
					if (params.group(2) != null)
					{
						setMoves = true;
						moves = new ArrayList<>();
						for (int i=0; i<4; ++i)
						{
							if (!params.group(3 + i).equals("None"))
							{
								moves.add(new Move(Attack.getAttack(params.group(3 + i))));
							}
						}
					}
					
					if (params.group(7) != null) 
						isEgg = true;
				}
				
				ActivePokemon p;
				if (isEgg) 
				{
					p = new ActivePokemon(pinfo);
				}
				else 
				{
					p = new ActivePokemon(pinfo, level, false, true);
				}
				
				if (shiny) 
				{
					p.setShiny();
				}
				
				if (setMoves) 
				{
					p.setMoves(moves);
				}

				pokemonList.add(p);
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
