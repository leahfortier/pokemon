package trainer;

import item.Item;
import item.use.BallItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import main.Namesies;
import map.DialogueSequence;
import pokemon.ActivePokemon;
import pokemon.BaseEvolution;
import pokemon.PC;
import trainer.Pokedex.PokedexStatus;
import battle.Battle;
import battle.MessageUpdate;
import battle.MessageUpdate.Update;
import battle.effect.EndBattleEffect;


public class CharacterData extends Trainer implements Serializable
{
	private static final long serialVersionUID = 4283479774388652604L;
	
	private static final int NUM_BADGES = 12;
	public static final int CATCH_SHAKES = 3;
	public static final int MAX_NAME_LENGTH = 10;
	public static final String DEFAULT_NAME = "Red";
	public static final int START_MONEY = 3000;
	
	public int locationX, locationY;
	public int direction;
	public boolean mapReset;
	public String mapName;
	public String areaName;
	HashSet<String> definedGlobals;
	
	// Used for map globals.
	public String previousMapName;
	public String mapEntranceName;
	
	private int fileNum;
	private long seconds;
	private int numBadges;

	private transient long timeSinceUpdate;
	
	private String lastPCMap;
	private String lastPCMapEntrance;
	
	private Pokedex pokedex;
	private PC pc;
	private boolean[] badges;
	private int repelSteps;
	
	public DialogueSequence messages;
	
	public transient Game game;
	
	public ActivePokemon evolvingPokemon;
	public BaseEvolution evolution;
	
	private ArrayList<String> logMessages;

	public CharacterData(Game game)
	{
		super(DEFAULT_NAME, START_MONEY);
		this.game = game;
		
		definedGlobals = new HashSet<>();
		logMessages = new ArrayList<>();
		
		pokedex = new Pokedex();
		pc = new PC();
		badges = new boolean[NUM_BADGES];
		Arrays.fill(badges, false);
		repelSteps = 0;
		
		direction = 3;
		
		seconds = 0;
		timeSinceUpdate = System.currentTimeMillis();
		
		areaName = "";
		
		mapReset = false;
	}
	
	public void setName(String s)
	{
		name = s;
	}
	
	public void giveBadge(int n)
	{
		if (!badges[n])
		{
			numBadges++;
			badges[n] = true;
		}
	}
	
	public int getNumBadges()
	{
		return numBadges;
	}
	
	private void updateTimePlayed()
	{
		seconds += (System.currentTimeMillis() - timeSinceUpdate)/1000;
		timeSinceUpdate = System.currentTimeMillis();
	}
	
	public long getTimePlayed()
	{
		return seconds + (System.currentTimeMillis() - timeSinceUpdate)/1000;
	}
	
	public int getFileNum()
	{
		return fileNum;
	}
	
	public void setFileNum(int n)
	{
		fileNum = n;
	}
	
	public void setLocation(int x, int y) 
	{
		locationX = x;
		locationY = y;
	}
	
	public void setMap(String name, String mapEntrance)
	{
		updateMapGlobals(mapName, name, mapEntranceName, mapEntrance);
		previousMapName = mapName;
		mapName = name;
		mapEntranceName = mapEntrance;
	}
	
	private void updateMapGlobals(String prevMap, String newMap, String prevMapEntrance, String newMapEntrance) {
		
		//Remove previous map
		removeGlobal("MapGlobal_PreviousMap_" + previousMapName);
		removeGlobal("MapGlobal_MapEntrance_" + prevMapEntrance);
		
		//Add current map
		addGlobal("MapGlobal_PreviousMap_" + prevMap);
		addGlobal("MapGlobal_MapEntrance_" + newMapEntrance);
	}
	
	// Called when a character steps once in any given direction
	public void step()
	{
		// Decrease repel steps
		if (repelSteps > 0)
		{
			repelSteps--;
			if (repelSteps == 0)
			{
				// TODO: Display message that the effects have worn off
				// Josh suggestion: Create a message variable in this class and have mapView 
				// check to see if there is a message. If there is a message and no current dialogue, 
				// set the message as the current dialogue.
				// Do you think that would work?
				
				// TODO: Give choice if you want to use another. 
				// Game variable needed
				messages = new DialogueSequence("The effects of repel have worn off.", null, null, null);
			}
			
			System.out.println("Repel Steps: " + repelSteps);
		}
		else
		{
			repelSteps = 0;
		}
		
		// Hatch eggs
		for (ActivePokemon p : team)
		{
			if (p.isEgg() && p.hatch())
			{
				// TODO: Show hatch animation
				evolvingPokemon = p;
				messages = new DialogueSequence("Huh?", null, null, new String[]{"Evolution_View_Trigger"});
				this.getPokedex().setStatus(p, Pokedex.PokedexStatus.CAUGHT);
				
				// Only one hatch per step
				break;
			}
		}
	}
	
	public boolean isUsingRepel()
	{
		return repelSteps > 0;
	}
	
	public void addRepelSteps(int steps)
	{
		repelSteps += steps;
	}
	
	public String getRouteName()
	{
		return areaName;
	}

	public void setDirection(int d)
	{
		direction = d;
	}
	
	public void setPokeCenter()
	{
		lastPCMap = mapName;
		lastPCMapEntrance = mapEntranceName;
	}
	
	public void teleportToPokeCenter()
	{
		setMap(lastPCMap, lastPCMapEntrance);

		direction = 3;
		mapReset = true;
	}
	
	public boolean globalsContain(String s)
	{
		return definedGlobals.contains(s);
	}
	
	public void addGlobal(String s)
	{
		if (s == null) return;
		definedGlobals.add(s);
	}
	
	public void removeGlobal(String s) 
	{
		if (definedGlobals.contains(s)) definedGlobals.remove(s);
	}
	
	public PC getPC()
	{
		return pc;
	}
	
	// Gives EXP to all Pokemon who participated in battle
	public void gainEXP(ActivePokemon dead, Battle b)
	{
		int numUsed = 0;
		for (ActivePokemon p : team)
		{
			if (!p.canFight()) continue;
			if (p.getLevel() == ActivePokemon.MAX_LEVEL) continue;
			if (p.getAttributes().isUsed()) numUsed++;
		}
		
		// Everyone died at the same time! Or only Level 100 Pokemon were used!
		if (numUsed == 0) return;
		
		double wild = b.isWildBattle() ? 1 : 1.5;
		int lev = dead.getLevel(), base = dead.getPokemonInfo().getBaseEXP();
		for (int i = 0; i < team.size(); i++)
		{
			ActivePokemon p = team.get(i); 
			if (p.canFight() && p.getAttributes().isUsed())
			{
				double gain = wild*base*lev*Math.pow(2*lev + 10, 2.5);
				gain /= 5*Math.pow(lev + p.getLevel() + 10, 2.5);
				gain++;
				gain *= p.isHoldingItem(b, Namesies.LUCKY_EGG_ITEM) ? 1.5 : 1;
				
				p.gainEXP(b, (int)Math.max(1, gain/numUsed), dead);				
			}
		}
	}
	
	public void winBattle(Battle b, Opponent opponent)
	{
		
		// Trainers pay up!
		if (opponent instanceof Trainer)
		{
			Trainer opp = (Trainer)opponent;
			b.addMessage(getName() + " defeated " + opp.getName() + "!", Update.WIN_BATTLE);
			addGlobal(b.getWinGlobal());
			
			int datCash = opp.getDatCashMoney()*(hasEffect(Namesies.DOUBLE_MONEY_EFFECT) ? 2 : 1);
			b.addMessage(getName() + " received " + datCash + " pokedollars for winning! Woo!");
			getDatCashMoney(datCash);
		}
		else 
		{
			b.addMessage("", Update.WIN_BATTLE);
		}
		
		Global.invoke(getEffects().toArray(), EndBattleEffect.class, "afterBattle", this, b, front());
		
		for (ActivePokemon p : team)
		{
			Global.invoke(new Object[] {p.getAbility()}, EndBattleEffect.class, "afterBattle", this, b, p);
		}
		
		setFront();
	}
	
	public Pokedex getPokedex()
	{
		return pokedex;
	}
	
	public void addPokemon(Battle b, ActivePokemon p)
	{
		p.setCaught();
		if (!pokedex.caught(p.getPokemonInfo().namesies()))
		{
			if (b != null) b.addMessage(p.getPokemonInfo().getName() + " was registered in the Pok\u00e9dex!");
			if (!p.isEgg()) pokedex.setStatus(p, PokedexStatus.CAUGHT);			
		}
		
		if (team.size() < MAX_POKEMON)
		{
			team.add(p);
		}
		else
		{
			if (b != null) b.addMessage(p.getName() + " was sent to Box " + (pc.getBoxNum() + 1) + " of your PC!");
			pc.depositPokemon(p);
		}
	}
	
	// Determines whether or not a Pokemon can be deposited
	public boolean canDeposit(ActivePokemon p)
	{
		// You can't deposit a Pokemon that you don't have
		if (!team.contains(p)) return false;
		
		// Eggs can always be deposited
		if (p.isEgg()) return true;
		
		// Otherwise you can if you have at least one other Pokemon that is not an egg
		return team.size() - totalEggs() > 1;
	}
	
	public int totalEggs()
	{
		int count = 0;
		for (ActivePokemon p : team)
		{
			if (p.isEgg()) count++;
		}
		
		return count;
	}
	
	// OH MY GOD CATCH A POKEMON OH MY GOD
	public boolean catchPokemon(Battle b, BallItem ball)
	{
		if (!b.isWildBattle())
		{
			b.addMessage("You can't try and catch a trainer's Pokemon! That's just rude!!!");
			return false;
		}
		
		b.addMessage(name + " threw the " + ((Item)ball).getName() + "!");
		
		ActivePokemon c = b.getOtherPokemon(true);
		int maxHP = c.getMaxHP(), hp = c.getHP(), catchRate = c.getPokemonInfo().getCatchRate();
		double[] ballInfo = ball.catchRate(front(), c, b);
		double ballMod = ballInfo[0], ballAdd = ballInfo[1], statusMod = c.getStatus().getType().getCatchModifier();
		
		double catchVal = (3*maxHP - 2*hp)*catchRate*ballMod*statusMod/(3*maxHP) + ballAdd;
		double shakeVal = 65536/Math.pow(255/catchVal, .25);
				
		for (int i = 0; i < CATCH_SHAKES + 1; i++)
		{
			if (Math.random()*65536 > shakeVal)
			{
				b.addMessage("", i);
				b.addMessage("Oh no! " + c.getName() + " broke free!");
				return true;
			}
		}
		
		b.addMessage("", -1);
		b.addMessage("Gotcha! " + c.getName() + " was caught!");
		gainEXP(c, b); 
		addPokemon(b, c);
		
		b.addMessage(" ", Update.EXIT_BATTLE);
		return true;
	}
	
	public void setEvolution(ActivePokemon pokemon, BaseEvolution evolution)
	{
		this.evolvingPokemon = pokemon;
		this.evolution = evolution;
		game.setViewMode(ViewMode.EVOLUTION_VIEW);
	}
	
	public void addLogMessage(MessageUpdate messageUpdate)
	{
		String messageString = messageUpdate.getMessage().trim();
		if (messageString.equals(""))
			return;
		
		logMessages.add("-" + messageString);
	}
	
	public void clearLogMessages()
	{
		logMessages.clear();
	}
	
	public ArrayList<String> getLogMessages() 
	{
		return logMessages;
	}
	
	public void save(){
		
		//printGlobals();
		
		try
		{
			updateTimePlayed();

			File saveDir = new File("saves" + Global.FILE_SLASH);
			if (!saveDir.exists())
				saveDir.mkdirs();
			
			FileOutputStream fout = new FileOutputStream("saves" + Global.FILE_SLASH + "File" + (fileNum + 1) + ".ser");
			ObjectOutputStream out = new ObjectOutputStream(fout);
			out.writeObject(this);
			out.close();
			fout.close();
			
			PrintStream prevOut = new PrintStream("saves" + Global.FILE_SLASH + "Preview" + (fileNum + 1) + ".out");
			prevOut.print(name + " " + seconds + " " + numBadges + " " + pokedex.numSeen());
			prevOut.close();
		}
		catch(IOException z)
		{
			Global.error("Oh no! That didn't save quite right!");
		}
	}
	
	public static CharacterData load(int fileNum, Game game){
		CharacterData loadChar = null;
		
		//updateSerVariables();
		
		try {
			FileInputStream fin = new FileInputStream("saves" + Global.FILE_SLASH + "File" + (fileNum + 1) + ".ser");
			ObjectInputStream in = new ObjectInputStream(fin);
			loadChar = (CharacterData) in.readObject();
			loadChar.game = game;
			loadChar.logMessages = new ArrayList<>();
			in.close();
			fin.close();
		}
		catch(IOException | ClassNotFoundException y){
			Global.error("Oh no! That didn't load quite right!");
		}
		catch(NullPointerException n){
			Global.error("Someone's been trying to cheat and edit this save file! Commence deletion!");
		}
		
		loadChar.timeSinceUpdate = System.currentTimeMillis();
		
		//loadChar.updateGlobals(false);
		
		return loadChar;
	}
	
	private static void updateSerVariables(int fileNum) 
	{
		try {
			//Replace bytes of renamed variable name
			FileInputStream fin = new FileInputStream("saves" + Global.FILE_SLASH + "File" + (fileNum + 1) + ".ser");
			byte[] bytes = new byte[fin.available()];
			fin.read(bytes);
			fin.close();
			
			boolean edited = false;
			
			byte[][][] variablesToUpdate = new byte[][][] {
					
					//Move.move to Move.attack
					{
						//Move.move: 00 04 6D 6F 76 65
						//extra: 00 04 75 73 65 64 4C 00 04 6D 6F 76 65 74
						{0x00, 0x04, 0x75, 0x73, 0x65, 0x64, 0x4C, 0x00, 0x04, 0x6D, 0x6F, 0x76, 0x65, 0x74},
						//Move.attack: 00 06 61 74 74 61 63 6B
						//extra: 00 04 75 73 65 64 4C 00 06 61 74 74 61 63 6B 74
						{0x00, 0x04, 0x75, 0x73, 0x65, 0x64, 0x4C, 0x00, 0x06, 0x61, 0x74, 0x74, 0x61, 0x63, 0x6B, 0x74}
					}
					//next
			};
			
			for(int currVariable = 0; currVariable < variablesToUpdate.length; ++currVariable)
			{
				byte[] newBytes = updateSerVariables(bytes, variablesToUpdate[currVariable][0], variablesToUpdate[currVariable][1]);
				if(newBytes != null) 
				{
					bytes = newBytes;
					edited = true;
				}
			}

			//Replacement was found, resave the file.
			if(edited) 
			{
				FileOutputStream out = new FileOutputStream("saves" + Global.FILE_SLASH + "File" + (fileNum + 1) + ".ser");
				out.write(bytes);
				out.close();
			}
		} 
		catch (IOException ex)
		{
			Global.error("Couldn't update Move variable name.");
		}
	}
	
	private static byte[] updateSerVariables(byte[] bytes, byte[] find, byte[] replace)
	{
		boolean edited = false;
		
		StringBuilder findString = new StringBuilder();
		for(byte b: find) 
		{
			findString.append((char)b);
		}
		
		StringBuilder replaceString = new StringBuilder();
		for(byte b: replace) 
		{
			replaceString.append((char)b);
		}
		
		//Loop through the entire array of bytes.
		for (int curr = 0; curr< bytes.length; ++curr) 
		{
			//Search the bytes for the search array.
			int currLoc;
			for (currLoc = 0; currLoc < find.length && currLoc < bytes.length && bytes[curr+currLoc] == find[currLoc]; ++currLoc);
			
			//If searched the entire search array, location was found.
			if(currLoc == find.length)
			{
				System.out.println("Updating Serializable variable: " +findString.toString() +" with: "+replaceString.toString());
				edited = true;
				
				//Make a copy of the bytes with the new length.
				int dif = replace.length - find.length;
				
				//Move the end of the array over by the difference in the find and replace arrays.
				//If difference is smaller, move bytes before copying the array.
				if (dif < 0)
				{
					for(int newPos = bytes.length-1; newPos > curr; --newPos)
					{
						bytes[newPos+dif] = bytes[newPos];
					}
				}
				
				//Update the size of the bytes array.
				bytes = Arrays.copyOf(bytes, bytes.length + dif);
				
				//Move the end of the array over by the difference in the find and replace arrays.
				//If difference is larger, move bytes after copying the array. 
				if(dif > 0)
				{
					for(int newPos = bytes.length-1; newPos > curr; --newPos)
					{
						bytes[newPos] = bytes[newPos-dif];
					}
				}
				
				//Add the replace array.
				for(int newPos = 0; newPos < replace.length; ++newPos)
				{
					bytes[newPos+curr] = replace[newPos];
				}
			}
		}
		
		return edited? bytes: null;
	}
	
	private void printGlobals() 
	{
		//List all the globals for this saved character
		for (String s: definedGlobals) 
		{
			System.out.println(s);
		}
		System.out.println();
	}
	
	private void updateGlobals (boolean printGlobals) 
	{
		if (printGlobals) 
		{
			System.out.println("Old Globals:");
			printGlobals();
		}
		
		
		updateGlobals_from_2013_11_24_to_2014_01_30();
		updateGlobals_from_2014_01_30_to_2014_08_14();
		
		
		if (printGlobals) 
		{
			System.out.println("New Globals:");
			printGlobals();
		}
	}
	
	private void replaceGlobals(String[][][] globalsToChange)
	{
		for (int currGlobal = 0; currGlobal < globalsToChange.length; ++currGlobal) 
		{
			boolean containsGlobal = false;
			for (int prevGlobal = 0; prevGlobal < globalsToChange[currGlobal][0].length; ++prevGlobal) 
			{
				containsGlobal |= definedGlobals.remove(globalsToChange[currGlobal][0][prevGlobal]);
			}
			
			if (containsGlobal) 
			{
				for (int newGlobal = 0; newGlobal < globalsToChange[currGlobal][1].length; ++newGlobal) 
				{
					addGlobal(globalsToChange[currGlobal][1][newGlobal]);
				}
			}
		}
	}
	
	
	//Convert globals from final class demo to latest commit before map maker trigger update
	private void updateGlobals_from_2013_11_24_to_2014_01_30 () 
	{
		String[][][] globalsToChange = {
				//Trainers/NPCs
				{{"BallGiven"}, 					{"triggered_Ball_Giver"}},
				{{"PotionGiven"}, 					{"triggered_Potion_Giver"}},
				{{"Prof_Maple_First", "hasSyrup"}, 	{"triggered_Prof_Maple"}},
				{{"battled_Trainer_1"}, 			{"triggered_Edge1_Trainer_1"}},
				{{"battled_Trainer_2"}, 			{"triggered_Edge1_Trainer_2"}},
				{{"battled_Trainer_3"}, 			{"triggered_Edge1_Trainer_3"}},
				{{"battled_Rival_1"}, 				{"triggered_RSA_Rival_1"}},
				{{"battled_Meadow_Trainer_1"}, 		{"triggered_Meadow_Trainer_1"}},
				{{"battled_Meadow_Trainer_2"}, 		{"triggered_Meadow_Trainer_2"}},
				{{"battled_Tom_Trainer_1"}, 		{"triggered_Tom_Trainer_1"}},
				{{"battled_Tom_Trainer_2"}, 		{"triggered_Tom_Trainer_2"}},
				{{"battled_Leader_Moore"}, 			{"triggered_Leader_Moore"}}
		};
		replaceGlobals(globalsToChange);
	}
	
	//Convert globals from the last commit to the new globals within the map maker trigger update
	private void updateGlobals_from_2014_01_30_to_2014_08_14 () 
	{
		//If someone was in the Tom Town pokecenter before update.
		if (definedGlobals.contains("PC_TomTown")) 
		{
			previousMapName = "Tom_Town";
		}
		
		//If someone was in the RSA Town pokecenter before update.
		if (definedGlobals.contains("PC_RSA")) 
		{
			previousMapName = "RSATown";
		}
		
		//If someone was in the Horizontal Transition Building before update.
		if (definedGlobals.contains("TBH_DFS") || definedGlobals.contains("TBH_RSA_L")) 
		{
			previousMapName = "RSATown";
			mapEntranceName = "WestDoor";
		}
		else if (definedGlobals.contains("TBH_RSA_R") || definedGlobals.contains("TBH_BFM_L")) 
		{
			previousMapName = "Bloom_Filter_Meadow";
			mapEntranceName = "WestDoor";
		}
		else if (definedGlobals.contains("TBH_BFM_R") || definedGlobals.contains("TBH_TomTown_L")) 
		{
			previousMapName = "Tom_Town";
			mapEntranceName = "WestDoor";
		}
		
		//Remove old globals and replace with new globals
		String[][][] globalsToChange = {
				//Pokemon center
				{{"PC_TomTown"}, 	{"MapGlobal_PreviousMap_Tom_Town", 	"MapGlobal_toPokeCenterFromEntrance_PokeCenter01"}},
				{{"PC_RSA"}, 		{"MapGlobal_PreviousMap_RSATown",	"MapGlobal_toPokeCenterFromEntrance_PokeCenter01"}},
				
				//Horizontal Transition Building
				{{"TBH_DFS", "TBH_RSA_L"}, 			{"MapGlobal_TransitionPair01", "MapGlobal_MapEntrance_WestDoor", "MapGlobal_PreviousMap_RSATown"}},
				{{"TBH_RSA_R", "TBH_BFM_L"}, 		{"MapGlobal_TransitionPair01", "MapGlobal_MapEntrance_WestDoor", "MapGlobal_PreviousMap_Bloom_Filter_Meadow"}},
				{{"TBH_BFM_R", "TBH_TomTown_L"}, 	{"MapGlobal_TransitionPair01", "MapGlobal_MapEntrance_WestDoor", "MapGlobal_PreviousMap_Tom_Town"}},
				
				//Items
				{{"hasDFSParalyzeHeal"}, 	{"hasDFS_Town_Item_Paralyze_Heal_01"}},
				{{"hasBFMMeadowPlate"}, 	{"hasBloom_Filter_Meadow_Item_Meadow_Plate_01"}},
				{{"hasBFMPechaBerry"}, 		{"hasBloom_Filter_Meadow_Item_Pecha_Berry_01"}},
				
				//NPCs and Trainers
				{{"triggered_Ball_Giver"}, 			{"triggered_DFS_Town_NPC_Ball_Giver_01"}},
				{{"triggered_Potion_Giver"}, 		{"triggered_DFS_Town_NPC_Potion_Giver_01"}},
				{{"triggered_Prof_Maple"}, 			{"triggered_MaplesLab_NPC_Prof_Maple_01"}},
				{{"triggered_Edge1_Trainer_1"}, 	{"triggered_DFS_Town_NPC_Edge1_Trainer_01"}},
				{{"triggered_Edge1_Trainer_2"}, 	{"triggered_DFS_Town_NPC_Edge1_Trainer_02"}},
				{{"triggered_Edge1_Trainer_3"}, 	{"triggered_DFS_Town_NPC_Edge1_Trainer_03"}},
				{{"triggered_RSA_Rival_1"}, 		{"triggered_RSATown_NPC_Rival_01"}},
				{{"triggered_Meadow_Trainer_1"}, 	{"triggered_Bloom_Filter_Meadow_NPC_Meadow_Trainer_01"}},
				{{"triggered_Meadow_Trainer_2"}, 	{"triggered_Bloom_Filter_Meadow_NPC_Meadow_Trainer_02"}},
				{{"triggered_Tom_Trainer_1"}, 		{"triggered_Tom_Gym_NPC_Tom_Trainer_01"}},
				{{"triggered_Tom_Trainer_2"}, 		{"triggered_Tom_Gym_NPC_Tom_Trainer_02"}},
				{{"triggered_Leader_Moore"}, 		{"triggered_Tom_Gym_NPC_Leader_Moore_01"}}
		};
		
		replaceGlobals(globalsToChange);
	}
}
