package gui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import trainer.CharacterData;

import main.Global;
import map.DialogueSequence;
import map.MapData;
import map.triggers.BadgeTrigger;
import map.triggers.ChangeViewTrigger;
import map.triggers.EventTrigger;
import map.triggers.GiveTrigger;
import map.triggers.GroupTrigger;
import map.triggers.HealPartyTrigger;
import map.triggers.LastPokeCenterTrigger;
import map.triggers.MapTransitionTrigger;
import map.triggers.SoundTrigger;
import map.triggers.TrainerBattleTrigger;
import map.triggers.Trigger;
import map.triggers.WildBattleTrigger;

public class GameData
{
	public static final Pattern dialogueBlockPattern = Pattern.compile("Dialogue\\s+(\\w+)\\s*\\{([^}]*)\\}");
	public static final Pattern triggerBlockPattern = Pattern.compile("(Group|Event|MapTransition|TrainerBattle|WildBattle|Give|HealParty|LastPokeCenter|Badge|ChangeView|Sound)Trigger\\s+(\\w+)\\s*\\{([^}]*)\\}");
	public static final Pattern areaIndexPattern = Pattern.compile("\"([^\"]*)\"\\s+(\\w+)\\s*((?:(?:[()&|!\\w]+\\s*:\\s*)?[\\w-]+\\s*,?\\s*)+)?");
	public static final Pattern areaSoundConditionPattern = Pattern.compile("(?:([()&|!\\w]+)\\s*:\\s*)?([\\w-]+)");
	
	public static final String DATA_LOCATION = "rec" + Global.FILE_SLASH;
	private HashMap<String, MapData> maps;
	private HashMap<Integer, String> areas;
	private HashMap<String, Trigger> triggers;
	private HashMap<String, DialogueSequence> dialogues;
	private TileSet mapTiles, battleTiles, pokemonTilesLarge, pokemonTilesMedium, pokemonTilesSmall, itemTiles, trainerTiles, pauseMenuTiles, partyTiles, mainMenuTiles, bagTiles;

	public GameData()
	{
		loadTiles();
		loadTriggers();
		loadAreas();
		loadDialogue();
		loadMaps();
	}

	private void loadMaps()
	{
		maps = new HashMap<>();
		File dir = new File(DATA_LOCATION + "maps" + Global.FILE_SLASH);
		for (File d : dir.listFiles())
		{
			if (d.getName().charAt(0) == '.' || d.getName().equals("areaIndex.txt")) continue;
			maps.put(d.getName(), new MapData(d, this));
		}
	}
	
	public void testMaps(CharacterData charData)
	{
		for(String map: maps.keySet())
		{
			maps.get(map).populateEntities(charData, this);
		}
	}
	
	private void loadAreas()
	{
		areas = new HashMap<>();
		
		File indexFile = new File("rec" + Global.FILE_SLASH + "maps" + Global.FILE_SLASH + "areaIndex.txt");
		if (!indexFile.exists())
		{
			System.err.println("Failed to find map area index file: " + indexFile.getName() +".");
			return;
		}
		
		String fileText = Global.readEntireFile(indexFile, false);
		Matcher m = areaIndexPattern.matcher(fileText);
		while (m.find())
		{
			String areaName = m.group(1);
			int value = (int) Long.parseLong(m.group(2), 16);
			areas.put(value, areaName);
			
			if(m.group(3) != null)
			{
				//GroupTrigger areaGroupTrigger = new GroupTrigger("GroupTrigger_AreaSound_for_"+areaName, "");
				StringBuilder groupTriggers = new StringBuilder();
				String areaNameDisplay = areaName.replace(' ', '_').replaceAll("\\W", "");
				
				Matcher areaSoundMatcher = areaSoundConditionPattern.matcher(m.group(3));
				while(areaSoundMatcher.find())
				{
					String condition = areaSoundMatcher.group(1);
					String musicName = areaSoundMatcher.group(2);
					String soundTriggerName = "SoundTrigger_AreaSound_for_"+areaNameDisplay+"_MusicName_"+musicName;
					
					//System.out.println(condition +"\n" +musicName);
					
					addTrigger("Sound", soundTriggerName, (condition != null? "condition: "+condition: "") + "\nmusicName: "+musicName);
					groupTriggers.append("trigger: "+soundTriggerName +"\n");
				}
				
				addTrigger("Group", "GroupTrigger_AreaSound_for_"+areaNameDisplay, groupTriggers.toString());
				
				//musicForAreas.put(areaName, m.group(3));
				//System.out.println(m.group(3));
			}
			//System.out.println("Area " +areaName +" loaded.");
		}
	}

	private void loadTiles()
	{
		try
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
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void loadDialogue()
	{
		dialogues = new HashMap<>();
		File dialogueFolder = new File(DATA_LOCATION + "dialogue" + Global.FILE_SLASH);
		for (File f : dialogueFolder.listFiles())
		{
			if (f.getName().charAt(0) == '.') continue;
			String fileText = Global.readEntireFile(f, false);
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
		File triggerFolder = new File(DATA_LOCATION + "triggers" + Global.FILE_SLASH);
		for (File f : triggerFolder.listFiles())
		{
			if (f.getName().charAt(0) == '.') continue;
			String fileText = Global.readEntireFile(f, false);
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
	
	public String getArea(int color)
	{
		return areas.containsKey(color)? areas.get(color): areas.get(0);
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
	
	public static Trigger createTrigger(String type, String name, String contents) {
		Trigger trig = null;
		switch (type)
		{
			case "Event":
				trig = new EventTrigger(name, contents);
				break;
			case "MapTransition":
				trig = new MapTransitionTrigger(name, contents);
				break;
			case "TrainerBattle":
				trig = new TrainerBattleTrigger(name, contents);
				break;
			case "WildBattle":
				trig = new WildBattleTrigger(name, contents);
				break;
			case "Group":
				trig = new GroupTrigger(name, contents);
				break;
			case "Give":
				trig = new GiveTrigger(name, contents);
				break;
			case "HealParty":
				trig = new HealPartyTrigger(name, contents);
				break;
			case "LastPokeCenter":
				trig = new LastPokeCenterTrigger(name, contents);
				break;
			case "Badge":
				trig = new BadgeTrigger(name, contents);
				break;
			case "ChangeView":
				trig = new ChangeViewTrigger(name, contents);
				break;
			case "Sound":
				trig = new SoundTrigger(name, contents);
				break;
			default:
				Global.error("Invalid trigger type " + type + ".");
		}
		return trig;
	}

	public void addTrigger(String type, String name, String contents)
	{
		Trigger trig = createTrigger(type, name, contents);
		triggers.put(name, trig);
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
