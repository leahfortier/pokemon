package gui;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Global;
import map.AreaData;
import map.DialogueSequence;
import map.MapData;
import map.triggers.Trigger;
import trainer.CharacterData;
import util.FileIO;

public class GameData
{
	public static final Pattern triggerBlockPattern = Pattern.compile("(Group|Event|MapTransition|TrainerBattle|WildBattle|Give|HealParty|LastPokeCenter|Badge|ChangeView|Sound)Trigger\\s+(\\w+)\\s*\\{([^}]*)\\}"); // TODO: Make private again maybe
	private static final Pattern dialogueBlockPattern = Pattern.compile("Dialogue\\s+(\\w+)\\s*\\{([^}]*)\\}");
	private static final Pattern areaIndexPattern = Pattern.compile("\"([^\"]*)\"\\s+(\\w+)\\s*(\\w+)\\s*(\\w+)\\s*([()&|!\\w-:,]+)?");

	private static final String TRIGGER_FOLDER = FileIO.makePath("rec", "triggers");
	private static final String DIALOGUE_FOLDER = FileIO.makePath("rec", "dialogue");
	private static final String MAPS_FOLDER = FileIO.makePath("rec", "maps");
	private static final String AREA_LOCATION =  MAPS_FOLDER + "areaIndex.txt";

	private HashMap<String, MapData> maps;
	private HashMap<Integer, AreaData> areas;
	private HashMap<String, Trigger> triggers;
	private HashMap<String, DialogueSequence> dialogues;

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

	public GameData()
	{
		loadTiles();
		loadTriggers();
		loadAreas();
		loadDialogue();
		loadMaps();
	}

	private void loadTiles()
	{
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

	private void loadMaps()
	{
		maps = new HashMap<>();
		File dir = new File(MAPS_FOLDER);

		for (File d : dir.listFiles())
		{
			if (d.getName().charAt(0) == '.' || d.getName().equals("areaIndex.txt"))
				continue;

			maps.put(d.getName(), new MapData(d, this));
		}
	}

	public void testMaps(CharacterData charData)
	{
		for (String map : maps.keySet())
		{
			maps.get(map).populateEntities(charData, this);
		}
	}

	private void loadAreas()
	{
		areas = new HashMap<>();

		File indexFile = new File(AREA_LOCATION);
		if (!indexFile.exists())
		{
			Global.error("Failed to find map area index file: " + indexFile.getName() + ".");
		}

		String fileText = FileIO.readEntireFileWithReplacements(indexFile, false);

		Matcher m = areaIndexPattern.matcher(fileText);
		while (m.find())
		{
			String areaName = m.group(1);
			int value = (int) Long.parseLong(m.group(2), 16);

			AreaData area = new AreaData(areaName, value, m.group(3), m.group(4), m.group(5));
			areas.put(value, area);
			area.addMusicTriggers(this);
		}
	}

	private void loadDialogue()
	{
		dialogues = new HashMap<>();
		File dialogueFolder = new File(DIALOGUE_FOLDER);
		for (File f : dialogueFolder.listFiles())
		{
			if (f.getName().charAt(0) == '.')
				continue;
			String fileText = FileIO.readEntireFileWithReplacements(f, false);
			Matcher m = dialogueBlockPattern.matcher(fileText);
			while (m.find())
			{
				String name = m.group(1);
				addDialogue(name, m.group(2));
				// System.out.println("Dialogue: " + name + " " + m.group(2));
			}
		}
	}

	private void loadTriggers()
	{
		triggers = new HashMap<>();
		File triggerFolder = new File(TRIGGER_FOLDER);
		for (File f : triggerFolder.listFiles())
		{
			if (f.getName().charAt(0) == '.')
				continue;
			String fileText = FileIO.readEntireFileWithReplacements(f, false);
			Matcher m = triggerBlockPattern.matcher(fileText);
			while (m.find())
			{
				String type = m.group(1);
				String name = m.group(2);
				addTrigger(type, name, m.group(3));
			}
		}
	}

	public MapData getMap(String name)
	{
		return maps.get(name);
	}

	public AreaData getArea(int color)
	{
		return areas.containsKey(color) ? areas.get(color) : areas.get(0);
	}

	public Trigger getTrigger(String name)
	{
		return triggers.get(name);
	}

	public DialogueSequence getDialogue(String name)
	{
		return dialogues.get(name);
	}

	public void addDialogue(String name, String contents)
	{
		DialogueSequence dialogue = new DialogueSequence(name, contents);
		dialogues.put(name, dialogue);
	}

	public void addTrigger(String type, String name, String contents)
	{
		Trigger trigger = Trigger.createTrigger(type, name, contents);
		triggers.put(name, trigger);
	}

	public TileSet getMapTiles()
	{
		return mapTiles;
	}

	public TileSet getBattleTiles()
	{
		return battleTiles;
	}

	public TileSet getItemTiles()
	{
		return itemTiles;
	}

	public TileSet getPokemonTilesLarge()
	{
		return pokemonTilesLarge;
	}

	public TileSet getPokemonTilesMedium()
	{
		return pokemonTilesMedium;
	}

	public TileSet getPokemonTilesSmall()
	{
		return pokemonTilesSmall;
	}

	public TileSet getTrainerTiles()
	{
		return trainerTiles;
	}

	public TileSet getMenuTiles()
	{
		return pauseMenuTiles;
	}

	public TileSet getPartyTiles()
	{
		return partyTiles;
	}

	public TileSet getMainMenuTiles()
	{
		return mainMenuTiles;
	}

	public TileSet getBagTiles()
	{
		return bagTiles;
	}
}
