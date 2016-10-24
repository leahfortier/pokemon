package trainer;

import item.Item;
import item.use.BallItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.Game;
import main.Game.ViewMode;
import namesies.Namesies;
import map.DialogueSequence;
import map.entity.MovableEntity.Direction;
import pokemon.ActivePokemon;
import pokemon.BaseEvolution;
import pokemon.PC;
import trainer.Pokedex.PokedexStatus;
import battle.Battle;
import battle.MessageUpdate;
import battle.MessageUpdate.Update;
import battle.effect.EndBattleEffect;

public class CharacterData extends Trainer implements Serializable {
	private static final long serialVersionUID = 4283479774388652604L;

	private static final int NUM_BADGES = 12;
	public static final int CATCH_SHAKES = 3;
	public static final int MAX_NAME_LENGTH = 10;
	public static final String DEFAULT_NAME = "Red";
	private static final int START_MONEY = 3000;

	// TODO: Look into most of these to check if they really do need to be public
	public int locationX, locationY;
	public Direction direction;
	
	public boolean mapReset;
	public String mapName;
	public String areaName;
	private Set<String> definedGlobals;
	
	// Used for map globals.
	private String previousMapName;
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
	
	private List<String> logMessages;

	public CharacterData(Game game) {
		super(DEFAULT_NAME, START_MONEY);
		this.initialize(game);
		
		definedGlobals = new HashSet<>();
		
		pokedex = new Pokedex();
		pc = new PC();
		
		badges = new boolean[NUM_BADGES];
		Arrays.fill(badges, false);
		
		repelSteps = 0;
		seconds = 0;
		
		direction = Direction.DOWN;
		areaName = "";
		mapReset = false;
	}
	
	// Initializes the character with the current game -- used when recovering a save file as well as the generic constructor
	public void initialize(Game game) {
		this.game = game;
		this.logMessages = new ArrayList<>();
		this.timeSinceUpdate = System.currentTimeMillis();
	}
	
	public void setName(String playerName) {
		this.name = playerName;
	}

	// TODO: n??? srsly??? did I fucking write this??????
	public void giveBadge(int n) {
		if (!badges[n]) {
			numBadges++;
			badges[n] = true;
		}
	}
	
	public int getNumBadges() {
		return numBadges;
	}
	
	public void updateTimePlayed() {
		seconds += (System.currentTimeMillis() - timeSinceUpdate)/1000;
		timeSinceUpdate = System.currentTimeMillis();
	}
	
	public long getTimePlayed() {
		return seconds + (System.currentTimeMillis() - timeSinceUpdate)/1000;
	}
	
	public long getSeconds() {
		return this.seconds;
	}
	
	public int getFileNum() {
		return fileNum;
	}
	
	public void setFileNum(int n) {
		fileNum = n;
	}
	
	public void setLocation(int x, int y) {
		locationX = x;
		locationY = y;
	}
	
	public void setMap(String name, String mapEntrance) {
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
	public void step() {
		// Decrease repel steps
		if (repelSteps > 0) {
			repelSteps--;
			if (repelSteps == 0) {
				// TODO: Give choice if you want to use another. 
				// Game variable needed
				messages = new DialogueSequence("The effects of repel have worn off.", null, null, null);
			}
			
			System.out.println("Repel Steps: " + repelSteps);
		}
		else {
			repelSteps = 0;
		}
		
		// Hatch eggs
		for (ActivePokemon p : team) {
			if (p.isEgg() && p.hatch()) {
				evolvingPokemon = p;
				messages = new DialogueSequence("Huh?", null, null, new String[] {"Evolution_View_Trigger"});
				
				// Only one hatch per step
				break;
			}
		}
	}
	
	public boolean isUsingRepel() {
		return repelSteps > 0;
	}
	
	public void addRepelSteps(int steps) {
		repelSteps += steps;
	}
	
	public String getAreaName() {
		return areaName;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	public void setPokeCenter() {
		lastPCMap = mapName;
		lastPCMapEntrance = mapEntranceName;
	}
	
	public void teleportToPokeCenter() {
		setMap(lastPCMap, lastPCMapEntrance);

		direction = Direction.DOWN;
		mapReset = true;
	}
	
	public boolean globalsContain(String s) {
		return definedGlobals.contains(s);
	}
	
	public void addGlobal(String s) {
		if (s == null) {
			return;
		}

		definedGlobals.add(s);
	}
	
	public void removeGlobal(String s) {
		if (definedGlobals.contains(s)) {
			definedGlobals.remove(s);
		}
	}
	
	public PC getPC() {
		return pc;
	}
	
	// Gives EXP to all Pokemon who participated in battle
	public void gainEXP(ActivePokemon dead, Battle b) {
		int numUsed = 0;
		for (ActivePokemon p : team) {
			if (!p.canFight()) {
				continue;
			}

			if (p.getLevel() == ActivePokemon.MAX_LEVEL) {
				continue;
			}

			if (p.getAttributes().isUsed()) {
				numUsed++;
			}
		}
		
		// Everyone died at the same time! Or only Level 100 Pokemon were used!
		if (numUsed == 0) {
			return;
		}
		
		double wild = b.isWildBattle() ? 1 : 1.5;
		int lev = dead.getLevel(), base = dead.getPokemonInfo().getBaseEXP();
		for (ActivePokemon p : team) {
			if (p.canFight() && p.getAttributes().isUsed()) {
				double gain = wild * base * lev * Math.pow(2 * lev + 10, 2.5);
				gain /= 5 * Math.pow(lev + p.getLevel() + 10, 2.5);
				gain++;
				gain *= p.isHoldingItem(b, Namesies.LUCKY_EGG_ITEM) ? 1.5 : 1;

				p.gainEXP(b, (int) Math.max(1, gain / numUsed), dead);
			}
		}
	}
	
	public void winBattle(Battle b, Opponent opponent) {
		
		// Trainers pay up!
		if (opponent instanceof Trainer) {
			Trainer opp = (Trainer)opponent;
			b.addMessage(getName() + " defeated " + opp.getName() + "!", Update.WIN_BATTLE);
			addGlobal(b.getWinGlobal());
			
			// I've decided that the next line of code is the best line in this entire codebase
			int datCash = opp.getDatCashMoney()*(hasEffect(Namesies.GET_DAT_CASH_MONEY_TWICE_EFFECT) ? 2 : 1);
			b.addMessage(getName() + " received " + datCash + " pokedollars for winning! Woo!");
			getDatCashMoney(datCash);
		}
		else {
			b.addMessage("", Update.WIN_BATTLE);
		}
		
		Battle.invoke(getEffects().toArray(), EndBattleEffect.class, "afterBattle", this, b, front());
		for (ActivePokemon p : team) {
			Battle.invoke(new Object[] {p.getAbility()}, EndBattleEffect.class, "afterBattle", this, b, p);
		}
		
		setFront();
	}
	
	public Pokedex getPokedex() {
		return pokedex;
	}
	
	public void addPokemon(Battle b, ActivePokemon p) {
		p.setCaught();
		if (!pokedex.caught(p.getPokemonInfo().namesies())) {
			if (b != null) {
				b.addMessage(p.getPokemonInfo().getName() + " was registered in the Pok\u00e9dex!");
			}

			if (!p.isEgg()) {
				pokedex.setStatus(p.getPokemonInfo(), PokedexStatus.CAUGHT);
			}
		}
		
		if (team.size() < MAX_POKEMON) {
			team.add(p);
		}
		else {
			if (b != null) {
				b.addMessage(p.getActualName() + " was sent to Box " + (pc.getBoxNum() + 1) + " of your PC!");
			}

			pc.depositPokemon(p);
		}
	}
	
	// Determines whether or not a Pokemon can be deposited
	public boolean canDeposit(ActivePokemon p) {
		// You can't deposit a Pokemon that you don't have
		if (!team.contains(p)) {
			return false;
		}
		
		// Eggs can always be deposited
		if (p.isEgg()) {
			return true;
		}
		
		// Otherwise you can if you have at least one other Pokemon that is not an egg
		return team.size() - totalEggs() > 1;
	}
	
	public int totalEggs() {
		int count = 0;
		for (ActivePokemon p : team) {
			if (p.isEgg()) {
				count++;
			}
		}
		
		return count;
	}
	
	// OH MY GOD CATCH A POKEMON OH MY GOD
	public boolean catchPokemon(Battle b, BallItem ball) {
		if (!b.isWildBattle()) {
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
				
		for (int i = 0; i < CATCH_SHAKES + 1; i++) {
			if (Math.random()*65536 > shakeVal) { // TODO: Random
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
	
	public void addLogMessage(MessageUpdate messageUpdate) {
		String messageString = messageUpdate.getMessage().trim();
		if (messageString.isEmpty()) {
			return;
		}
		
		logMessages.add("-" + messageString);
	}
	
	public void clearLogMessages() {
		logMessages.clear();
	}
	
	public List<String> getLogMessages() {
		return logMessages;
	}

	private void printGlobals() {
		//List all the globals for this saved character
		for (String s: definedGlobals) {
			System.out.println(s);
		}
		System.out.println();
	}

	private void updateGlobals(boolean printGlobals) {
		if (printGlobals) {
			System.out.println("Old Globals:");
			printGlobals();
		}


		updateGlobals_from_2013_11_24_to_2014_01_30();
		updateGlobals_from_2014_01_30_to_2014_08_14();


		if (printGlobals) {
			System.out.println("New Globals:");
			printGlobals();
		}
	}

	private void replaceGlobals(String[][][] globalsToChange) {
		for (String[][] aGlobalsToChange : globalsToChange) {
			boolean containsGlobal = false;
			for (int prevGlobal = 0; prevGlobal < aGlobalsToChange[0].length; prevGlobal++) {
				containsGlobal |= definedGlobals.remove(aGlobalsToChange[0][prevGlobal]);
			}

			if (containsGlobal) {
				for (int newGlobal = 0; newGlobal < aGlobalsToChange[1].length; newGlobal++) {
					addGlobal(aGlobalsToChange[1][newGlobal]);
				}
			}
		}
	}

	// TODO: What is this? And is it still necessary?
	//Convert globals from final class demo to latest commit before map maker trigger update
	private void updateGlobals_from_2013_11_24_to_2014_01_30 () {
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
	private void updateGlobals_from_2014_01_30_to_2014_08_14 () {
		//If someone was in the Tom Town pokecenter before update.
		if (definedGlobals.contains("PC_TomTown")) {
			previousMapName = "Tom_Town";
		}

		//If someone was in the RSA Town pokecenter before update.
		if (definedGlobals.contains("PC_RSA")) {
			previousMapName = "RSATown";
		}

		//If someone was in the Horizontal Transition Building before update.
		if (definedGlobals.contains("TBH_DFS") || definedGlobals.contains("TBH_RSA_L")) {
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
