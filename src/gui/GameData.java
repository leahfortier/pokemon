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

	private TileSet partyTiles;
	private TileSet pokemonTilesSmall;
	private TileSet pokemonTilesMedium;
	private TileSet pokemonTilesLarge;
	private TileSet pokedexTilesSmall;
	private TileSet pokedexTilesLarge;
	private TileSet itemTiles;
	private IndexTileSet mapTiles;
	private IndexTileSet trainerTiles;
	private IndexTileSet terrainTiles;

	public void loadData() {
		loadTiles();
		loadMaps();
	}

	private void loadTiles() {
		partyTiles = new TileSet(Folder.PARTY_TILES);
		pokemonTilesSmall = new TileSet(Folder.POKEMON_TILES);
		pokemonTilesMedium = new TileSet(Folder.POKEMON_TILES, 2.3f);
		pokemonTilesLarge = new TileSet(Folder.POKEMON_TILES, 2.9f);
		pokedexTilesSmall = new TileSet(Folder.POKEDEX_TILES, .5f);
		pokedexTilesLarge = new TileSet(Folder.POKEDEX_TILES);
		itemTiles = new TileSet(Folder.ITEM_TILES);
		mapTiles = new IndexTileSet(Folder.MAP_TILES);
		trainerTiles = new IndexTileSet(Folder.TRAINER_TILES);
		terrainTiles = new IndexTileSet(Folder.TERRAIN_TILES);
	}

	private void loadMaps() {
		triggers = new HashMap<>();
		maps = new HashMap<>();
		File dir = new File(Folder.MAPS);

		for (File d : dir.listFiles()) {
			if (d.getName().charAt(0) == '.') {
				continue;
			}

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

	public IndexTileSet getMapTiles() {
		return mapTiles;
	}

	public IndexTileSet getTerrainTiles() {
		return terrainTiles;
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

	// TODO: Eventually might want this to not be indexed and have an enum or something so it can be referenced easily
	public IndexTileSet getTrainerTiles() {
		return trainerTiles;
	}

	public TileSet getPartyTiles() {
		return partyTiles;
	}

	public TileSet getPokedexTilesSmall() {
		return pokedexTilesSmall;
	}

	public TileSet getPokedexTilesLarge() {
		return pokedexTilesLarge;
	}
}
