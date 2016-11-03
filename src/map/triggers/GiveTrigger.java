package map.triggers;

import item.Item;
import main.Game;
import namesies.ItemNamesies;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: This needs to be two separate trigger classes -- should not store a list, but item trigger can store a quantity of a single item
public class GiveTrigger extends Trigger {
	private static final Pattern pokemonTriggerPattern = Pattern.compile("(pokemon:)\\s*([A-Za-z \\t0-9,:.\\-'*]*)");
	
	private List<Item> itemList;
	private List<ActivePokemon> pokemonList;

	public GiveTrigger(String name, ItemNamesies itemNamesies) {
		super(name, StringUtils.empty());

		itemList = new ArrayList<>();
		itemList.add(Item.getItem(itemNamesies));
		pokemonList = new ArrayList<>();
	}

	public GiveTrigger(String name, String contents) {
		super(name, contents);
		itemList = new ArrayList<>();

		if (Item.isItem(contents)) {
			itemList.add(Item.getItem(ItemNamesies.getValueOf(contents)));
		}

		Matcher m = variablePattern.matcher(contents);		
		while (m.find()) {
			String type = m.group(1);
			if (type.equals("item")) {
				itemList.add(Item.getItemFromName(m.group(2)));
			}
		}
		
		pokemonList = new ArrayList<>();
		m = pokemonTriggerPattern.matcher(contents);
		while (m.find()) {
			if(m.group(1) != null) {
				pokemonList.add(ActivePokemon.createActivePokemon(m.group(2), true));
			}
		}
	}

	public void execute() {
		super.execute();

		CharacterData player = Game.getPlayer();
		for (Item i: itemList) {
			player.addItem(i);
		}
		
		for (ActivePokemon p: pokemonList) {
			player.addPokemon(null, p);
		}
	}

	// TODO: appendLine and addSpace
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("GiveTrigger: ")
				.append(name)
				.append("\n");

		ret.append("Item List: ");
		for (Item i: itemList) {
			ret.append(i.getName())
					.append(" ");
		}
		ret.append("\n");
		
		ret.append("Pokemon: ");
		for (ActivePokemon p: pokemonList) {
			ret.append(p.getPokemonInfo().getName())
					.append(" ");
		}
		
		return ret.toString();
	}
}
