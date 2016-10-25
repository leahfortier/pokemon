package pokemon;

import item.Item;
import item.hold.HoldItem;
import main.Global;
import main.Type;
import namesies.AbilityNamesies;
import namesies.AttackNamesies;
import namesies.PokemonNamesies;
import util.FileIO;
import util.StringUtils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class PokemonInfo implements Serializable, Comparable<PokemonInfo> {
	private static final long serialVersionUID = 1L;

	public static final int NUM_POKEMON = 719;
	public static final int EGG_IMAGE = 0x10000;
	
	private static Map<String, PokemonInfo> map;
	private static PokemonInfo[] info;
	private static List<PokemonInfo> baseEvolution;
	private static Set<PokemonNamesies> incenseBabies = new HashSet<>(); // TODO: lalala
	
	private int number;
	private String name;
	private PokemonNamesies namesies;
	private int[] baseStats;
	private int baseExp;
	private GrowthRate growthRate;
	private Type[] type;
	private Map<Integer, Set<AttackNamesies>> levelUpMoves;
	private Set<AttackNamesies> learnableMoves;
	private int catchRate;
	private int[] givenEVs;
	private Evolution evolution;
	private List<WildHoldItem> wildHoldItems;
	private AbilityNamesies[] abilities;
	private int maleRatio;
	private String classification;
	private int height;
	private double weight;
	private String flavorText;
	private int eggSteps;
	private String[] eggGroups;
	
	public PokemonInfo(int number, String name, int[] baseStats, int baseExp, String growthRate, 
			String type1, String type2, Map<Integer, Set<AttackNamesies>> levelUpMoves, Set<AttackNamesies> tmMoves,
			Set<AttackNamesies> eggMoves, Set<AttackNamesies> tutorMoves, int catchRate, int[] givenEVs, Evolution evolution,
			List<WildHoldItem> wildHoldItems, int genderRatio, String ability1, String ability2, String classification, 
			int height, double weight, String flavorText, int eggSteps, String eggGroup1, String eggGroup2) {
		this.number = number;
		this.name = name;
		this.namesies = PokemonNamesies.getValueOf(this.name);
		this.baseStats = baseStats;
		this.baseExp = baseExp;
		this.growthRate = GrowthRate.getRate(growthRate);
		this.type = new Type[] { Type.valueOf(type1.toUpperCase()), Type.valueOf(type2.toUpperCase()) };
		this.levelUpMoves = levelUpMoves;
		this.learnableMoves = new HashSet<>();
		this.learnableMoves.addAll(tmMoves);
		this.learnableMoves.addAll(eggMoves);
		this.learnableMoves.addAll(tutorMoves);
		this.catchRate = catchRate;
		this.givenEVs = givenEVs;
		this.evolution = evolution;
		this.wildHoldItems = wildHoldItems;
		this.abilities = new AbilityNamesies[] { AbilityNamesies.getValueOf(ability1), AbilityNamesies.getValueOf(ability2) };
		this.maleRatio = genderRatio;
		this.classification = classification;
		this.height = height;
		this.weight = weight;
		this.flavorText = flavorText;
		this.eggSteps = eggSteps;
		this.eggGroups = new String[] { eggGroup1, eggGroup2 };
	}
	
	public Type[] getType() {
		return type;
	}
	
	public Map<Integer, Set<AttackNamesies>> getLevelUpMoves() {
		return levelUpMoves;
	}
	
	public int getStat(int index) {
		return baseStats[index];
	}
	
	public GrowthRate getGrowthRate() {
		return growthRate;
	}
	
	public int getEggSteps() {
		return eggSteps;
	}
	
	public AbilityNamesies[] getAbilities() {
		return abilities;
	}
	
	public boolean hasAbility(AbilityNamesies s) {
		return abilities[0] == s || abilities[1] == s;
	}
	
	public int getCatchRate() {
		return catchRate;
	}
	
	public int getBaseEXP() {
		return baseExp;
	}
	
	public int[] getGivenEVs() {
		return givenEVs;
	}
	
	public int getMaleRatio() {
		return maleRatio;
	}
	
	public int getHeight() {
		return height;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public String getClassification() {
		return classification;
	}
	
	public String getFlavorText() {
		return flavorText;
	}
	
	public PokemonNamesies namesies() {
		return namesies;
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumber() {
		return number;
	}
	
	public Evolution getEvolution() {
		return evolution;
	}
	
	public List<WildHoldItem> getWildItems() {
		return wildHoldItems;
	}
	
	public int getImageNumber(boolean shiny) {
		return getImageNumber(number, shiny);
	}
	
	public static int getImageNumber(int number, boolean shiny) {
		if (shiny) {
			return 4*number + 2;
		}
		else {
			return 4*number;
		}
	}
	
	public static PokemonInfo getPokemonInfo(PokemonNamesies pokemon) {
		if (isPokemon(pokemon)) {
			return map.get(pokemon.getName());
		}

		Global.error("No such Pokemon " + pokemon.getName());
		return null;
	}
	
	public static PokemonInfo getPokemonInfo(int index) {
		if (info == null) {
			loadPokemonInfo();
		}
		
		if (index <= 0 || index > NUM_POKEMON) {
			Global.error("No such Pokemon Number " + index); 
		}

		return info[index];
	}
	
	// Pretend this comment has as much hate and passion as the similar methods in Attack.java and Item.java
	public static boolean isPokemonName(String name) {
		if (map == null) {
			loadPokemonInfo();
		}
		
		return map.containsKey(name);
	}
	
	public static boolean isPokemon(PokemonNamesies pokemon) {
		return isPokemonName(pokemon.getName());
	}

	// TODO: Move this into StuffGen
	public static void baseEvolutionGenerator() {
		if (info == null) {
			loadPokemonInfo();
		}
		
		Set<PokemonNamesies> set = new HashSet<>();
		for (int i = 1; i < info.length; i++) {
			set.add(info[i].namesies());
		}
		
		for (int i = 1; i < info.length; i++) {
			PokemonInfo p = info[i];
			
			if (!p.canBreed() && !p.getEvolution().canEvolve()) {
				set.remove(p.namesies());
			}
			
			for (PokemonNamesies evolutions : p.getEvolution().getEvolutions()) {
				set.remove(evolutions);
			}
		}
		
		PokemonInfo[] p = new PokemonInfo[set.size()];
		int i = 0;
		for (PokemonNamesies namesies : set) {
			p[i++] = getPokemonInfo(namesies);
		}
		
		Arrays.sort(p);
		
		StringBuilder out = new StringBuilder();
		for (PokemonInfo info : p) {
			StringUtils.appendLine(out, info.getName());
		}
		
		FileIO.writeToFile("BaseEvolutions.txt", out);
	}
	
	public static PokemonInfo getRandomBaseEvolution() {
		if (baseEvolution == null) {
			baseEvolution = new ArrayList<>();
			Scanner in = new Scanner(FileIO.readEntireFileWithReplacements(new File("BaseEvolutions.txt"), false));
			while (in.hasNext()) {
				PokemonNamesies namesies = PokemonNamesies.getValueOf(in.nextLine().trim());
				baseEvolution.add(PokemonInfo.getPokemonInfo(namesies));
			}
			
			in.close();
		}

		return Global.getRandomValue(baseEvolution);
	}
	
	public int compareTo(PokemonInfo p) {
		return number - p.number;
	}

	// Create and load the Pokemon info map if it doesn't already exist
	public static void loadPokemonInfo() {
		if (map != null) {
			return;
		}
		
		map = new HashMap<>();
		info = new PokemonInfo[NUM_POKEMON + 1];

		Scanner in = new Scanner(FileIO.readEntireFileWithReplacements(new File("pokemoninfo.txt"), false));
		while (in.hasNext()) {
			int num = in.nextInt();
			in.nextLine();
			
			info[num] = new PokemonInfo(num, in.nextLine().trim(), sixIntArray(in),
					in.nextInt(), in.nextLine().trim() + in.nextLine().trim(), in.next(), in.next(),
					createLevelUpMoves(in), createMovesSet(in), createMovesSet(in), createMovesSet(in),
					in.nextInt(), sixIntArray(in), Evolution.readEvolution(in), WildHoldItem.createList(in),
					in.nextInt(), in.nextLine().trim() + in.nextLine().trim(), in.nextLine().trim(), 
					in.nextLine().trim(), in.nextInt(), in.nextDouble(), in.nextLine().trim(), in.nextInt(), 
					in.nextLine().trim() + in.nextLine().trim(), in.nextLine().trim());
			
			map.put(info[num].getName(), info[num]);
		}
		
		in.close();
	}
	
	private static int[] sixIntArray(Scanner in) {
		int[] arr = new int[6];
		for (int i = 0; i < 6; i++) {
			arr[i] = in.nextInt();
		}
		
		return arr;
	}
	
	private static Map<Integer, Set<AttackNamesies>> createLevelUpMoves(Scanner in) {
		Map<Integer, Set<AttackNamesies>> levelUpMoves = new TreeMap<>();
		int numMoves = in.nextInt();
		
		for (int i = 0; i < numMoves; i++) {
			int level = in.nextInt();
			if (!levelUpMoves.containsKey(level)) {
				levelUpMoves.put(level, new TreeSet<>());
			}
			
			String attackName = in.nextLine().trim();
			AttackNamesies namesies = AttackNamesies.getValueOf(attackName);

			if (level < 0 || level > ActivePokemon.MAX_LEVEL) {
				Global.error("Cannot learn moves at level " + level + " (Move: " + attackName + ")");
			}

			levelUpMoves.get(level).add(namesies);
		}
		
		return levelUpMoves;
	}
	
	private static Set<AttackNamesies> createMovesSet(Scanner in) {
		Set<AttackNamesies> tmMoves = new HashSet<>();
		int numMoves = in.nextInt();
		in.nextLine();
		
		for (int i = 0; i < numMoves; i++) {
			String attackName = in.nextLine().trim();

			AttackNamesies namesies = AttackNamesies.getValueOf(attackName);
			tmMoves.add(namesies);
		}
		
		return tmMoves;
	}
	
	public Set<AttackNamesies> getMoves(int level) {
		if (levelUpMoves.containsKey(level)) {
			return levelUpMoves.get(level);
		}
		
		return new TreeSet<>();
	}
	
	public boolean canBreed() {
		return !eggGroups[0].equals("None") || !eggGroups[1].equals("None");
	}

	// TODO: new file
	public static class WildHoldItem implements Serializable {
		private static final long serialVersionUID = 1L;

		private HoldItem item;
		private int chance;
		
		public WildHoldItem(int chance, String itemName) {
			item = (HoldItem)Item.getItemFromName(itemName);
			this.chance = chance;
		}
		
		public static List<WildHoldItem> createList(Scanner in) {
			List<WildHoldItem> list = new ArrayList<>();
			int num = in.nextInt();
			for (int i = 0; i < num; i++) list.add(new WildHoldItem(in.nextInt(), in.nextLine().trim()));
			return list;
		}
		
		public static HoldItem getWildHoldItem(List<WildHoldItem> list) {
			int random = Global.getRandomInt(100);
			int sum = 0;

			for (WildHoldItem i : list) {
				sum += i.chance;
				if (random < sum) {
					return i.item;
				}
			}
			
			return (HoldItem)Item.noneItem();
		}
	}
	
	public PokemonInfo getBaseEvolution() {
		return getBaseEvolution(this);
	}

	// TODO: Instead of generating this on the fly should just be added to the text file and stored
	public static PokemonInfo getBaseEvolution(PokemonInfo targetPokes) {
		if (targetPokes.namesies() == PokemonNamesies.MANAPHY) {
			return PokemonInfo.getPokemonInfo(PokemonNamesies.PHIONE);
		}

		Set<String> allPokes = map.keySet();
		while (true) {
			boolean changed = false;
			for (String pokesName : allPokes) {
				PokemonInfo pokes = map.get(pokesName);
				PokemonNamesies[] evolutionNamesies = pokes.getEvolution().getEvolutions();
				for (PokemonNamesies namesies : evolutionNamesies) {
					if (namesies == targetPokes.namesies()) {
						targetPokes = pokes;
						changed = true;
						break;
					}
				}
				
				if (changed) {
					break;
				}
			}
			
			if (!changed) {
				return targetPokes;
			}
		}
	}
	
	public String[] getEggGroups() {
		return eggGroups;
	}
	
	public boolean isIncenseBaby() {
		return incenseBabies.contains(namesies);
	}
	
	public static void addIncenseBaby(PokemonNamesies incenseBaby) {
		incenseBabies.add(incenseBaby);
	}
	
	// Returns what level the Pokemon will learn the given attack, returns -1 if they cannot learn it by level up
	public int levelLearned(AttackNamesies attack) {
		for (Integer level : getLevelUpMoves().keySet()) {
			for (AttackNamesies levelUpMove : getLevelUpMoves().get(level)) {
				if (attack == levelUpMove) {
					return level;
				}
			}
		}
		
		return -1;
	}
	
	public boolean canLearnMove(AttackNamesies attack) {
		return levelLearned(attack) != -1 || canLearnByBreeding(attack);
	}

	public boolean canLearnByBreeding(AttackNamesies attack) {
		return this.learnableMoves.contains(attack);
	}
}
