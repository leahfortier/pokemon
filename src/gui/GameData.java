package gui;

import main.Global;
import map.MapData;
import map.triggers.Trigger;
import util.Folder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GameData {
	private Map<String, MapData> maps;
	private Map<String, Trigger> triggers;

	private TileSet mapTiles;
	private TileSet battleTiles;
	private TileSet pokemonTilesLarge;
	private TileSet pokemonTilesMedium;
	private TileSet pokemonTilesSmall;
	private TileSet itemTiles;
	private TileSet trainerTiles;
	private TileSet pauseMenuTiles;
	private TileSet partyTiles;
	private TileSet mainMenuTiles;
	private TileSet bagTiles;

	public void loadData() {
		loadTiles();
		loadMaps();
	}

	private void loadTiles() {
		mapTiles = new TileSet("mapTiles", 1.0f);
		battleTiles = new TileSet("battleViewTiles", 1.0f);
		pokemonTilesLarge = new TileSet("pokemonTiles", 2.9f);
		pokemonTilesMedium = new TileSet("pokemonTiles", 2.3f);
		pokemonTilesSmall = new TileSet("pokemonTiles", 1.0f);
		pauseMenuTiles = new TileSet("pauseViewTiles", 1.0f);
		itemTiles = new TileSet("itemTiles", 1.0f);
		trainerTiles = new TileSet("trainerTiles", 1.0f);
		partyTiles = new TileSet("partyTiles", 1.0f);
		mainMenuTiles = new TileSet("mainMenuTiles", 1.0f);
		bagTiles = new TileSet("bagTiles", 1.0f);
	}

	private void loadMaps() {
		triggers = new HashMap<>();
		maps = new HashMap<>();
		File dir = new File(Folder.MAPS);

		for (File d : dir.listFiles()) {

			maps.put(d.getName(), new MapData(d));

//			if (!d.isDirectory()) {
//				continue;
//			}
//
//			for (File mapFolder : d.listFiles()) {
//				maps.put(mapFolder.getName(), new MapData(mapFolder));
//			}

		}
	}

	public void testMaps() {
		for (String map : maps.keySet()) {
			maps.get(map).populateEntities();
		}
	}

	public MapData getMap(String name) {
		if (!maps.containsKey(name)) {
			Global.error("Cannot find map with name " + name);
		}

		return maps.get(name);
	}

	public boolean hasTrigger(String triggerName) {
		return triggers.containsKey(triggerName);
	}

	public Trigger getTrigger(String name) {
		return triggers.get(name);
	}

	public void addTrigger(Trigger trigger) {
		triggers.put(trigger.getName(), trigger);
	}

	public TileSet getMapTiles() {
		return mapTiles;
	}

	public TileSet getBattleTiles() {
		return battleTiles;
	}

	public TileSet getItemTiles() {
		return itemTiles;
	}

	public TileSet getPokemonTilesLarge() {
		return pokemonTilesLarge;
	}

	public TileSet getPokemonTilesMedium() {
		return pokemonTilesMedium;
	}

	public TileSet getPokemonTilesSmall() {
		return pokemonTilesSmall;
	}

	public TileSet getTrainerTiles() {
		return trainerTiles;
	}

	public TileSet getMenuTiles() {
		return pauseMenuTiles;
	}

	public TileSet getPartyTiles() {
		return partyTiles;
	}

	public TileSet getMainMenuTiles() {
		return mainMenuTiles;
	}

	public TileSet getBagTiles() {
		return bagTiles;
	}
}
