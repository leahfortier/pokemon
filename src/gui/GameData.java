package gui;

import main.Global;
import map.AreaData;
import map.MapData;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import util.FileIO;
import util.FileName;
import util.Folder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameData {
	public static final Pattern triggerBlockPattern = Pattern.compile("(Group|Dialogue|MapTransition|TrainerBattle|WildBattle|Give|HealParty|LastPokeCenter|Badge|ChangeView|Sound)Trigger\\s+(\\w+)\\s*\\{([^}]*)\\}"); // TODO: Make private again maybe
	private static final Pattern areaIndexPattern = Pattern.compile("\"([^\"]*)\"\\s+(\\w+)\\s*(\\w+)\\s*(\\w+)\\s*([()&|!\\w-:,]+)?");

	private Map<String, MapData> maps;
	private Map<Integer, AreaData> areas;
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
		loadTriggers();
		loadAreas();
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
		maps = new HashMap<>();
		File dir = new File(Folder.MAPS); // TODO: Check exists

		for (File d : dir.listFiles()) {
			if (!d.isDirectory()) {
				continue;
			}

			for (File mapFolder : d.listFiles()) {
				maps.put(mapFolder.getName(), new MapData(mapFolder));
			}

		}
	}

	public void testMaps() {
		for (String map : maps.keySet()) {
			maps.get(map).populateEntities();
		}
	}

	private void loadAreas() {
		areas = new HashMap<>();

		File indexFile = new File(FileName.MAP_AREA_INDEX);
		if (!indexFile.exists()) {
			Global.error("Failed to find map area index file: " + indexFile.getName() + ".");
		}

		String fileText = FileIO.readEntireFileWithReplacements(indexFile, false);

		Matcher m = areaIndexPattern.matcher(fileText);
		while (m.find()) {
			String areaName = m.group(1);
			int value = (int) Long.parseLong(m.group(2), 16);

			AreaData area = new AreaData(areaName, value, m.group(3), m.group(4), m.group(5));
			areas.put(value, area);
			area.addMusicTriggers(this);
		}
	}

	private void loadTriggers() {
		triggers = new HashMap<>();
		File triggerFolder = new File(Folder.TRIGGERS);
		for (File f : triggerFolder.listFiles()) {
			if (f.getName().charAt(0) == '.') {
				continue;
			}

			String fileText = FileIO.readEntireFileWithReplacements(f, false);
			Matcher m = triggerBlockPattern.matcher(fileText);
			while (m.find()) {
				TriggerType type = TriggerType.getTriggerType(m.group(1));

				addTrigger(type, m.group(3));
			}
		}
	}

	public MapData getMap(String name) {
		if (!maps.containsKey(name)) {
			Global.error("Cannot find map with name " + name);
		}

		return maps.get(name);
	}

	public AreaData getArea(int color) {
		return areas.containsKey(color) ? areas.get(color) : areas.get(0);
	}

	public boolean hasTrigger(String triggerName) {
		return triggers.containsKey(triggerName);
	}

	public Trigger getTrigger(String name) {
		return triggers.get(name);
	}

	public void addTrigger(TriggerType type, String contents) {
		this.addTrigger(type.createTrigger(contents));
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
