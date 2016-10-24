package pokemon;

import item.Item;
import item.hold.HoldItem;
import main.Global;
import main.Namesies;
import main.Namesies.NamesiesType;
import main.Type;
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
	private static Set<Namesies> incenseBabies = new HashSet<>(); // TODO: lalala
	
	private int number;
	private String name;
	private Namesies namesies;
	private int[] baseStats;
	private int baseExp;
	private GrowthRate growthRate;
	private Type[] type;
	private Map<Integer, Set<Namesies>> levelUpMoves;
	private Set<Namesies> learnableMoves;
	private int catchRate;
	private int[] givenEVs;
	private Evolution evolution;
	private List<WildHoldItem> wildHoldItems;
	private Namesies[] abilities;
	private int maleRatio;
	private String classification;
	private int height;
	private double weight;
	private String flavorText;
	private int eggSteps;
	private String[] eggGroups;
	
	public PokemonInfo(int number, String name, int[] baseStats, int baseExp, String growthRate, 
			String type1, String type2, Map<Integer, Set<Namesies>> levelUpMoves, Set<Namesies> tmMoves,
			Set<Namesies> eggMoves, Set<Namesies> tutorMoves, int catchRate, int[] givenEVs, Evolution evolution,
			List<WildHoldItem> wildHoldItems, int genderRatio, String ability1, String ability2, String classification, 
			int height, double weight, String flavorText, int eggSteps, String eggGroup1, String eggGroup2) {
		this.number = number;
		this.name = name;
		this.namesies = Namesies.getValueOf(this.name, NamesiesType.POKEMON);
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
		this.abilities = new Namesies[] { Namesies.getValueOf(ability1, NamesiesType.ABILITY), Namesies.getValueOf(ability2, NamesiesType.ABILITY) };
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
	
	public Map<Integer, Set<Namesies>> getLevelUpMoves() {
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
	
	public Namesies[] getAbilities() {
		return abilities;
	}
	
	public boolean hasAbility(Namesies s) {
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
	
	public Namesies namesies() {
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
	
	public static PokemonInfo getPokemonInfo(Namesies p) {
		if (isPokemon(p)) {
			return map.get(p.getName());
		}

		Global.error("No such Pokemon " + p.getName());
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
	
	public static boolean isPokemon(Namesies p) {
		return isPokemonName(p.getName());
	}

	// TODO: Move this into StuffGen
	public static void baseEvolutionGenerator() {
		if (info == null) {
			loadPokemonInfo();
		}
		
		Set<Namesies> set = new HashSet<>();
		for (int i = 1; i < info.length; i++) {
			set.add(info[i].namesies());
		}
		
		for (int i = 1; i < info.length; i++) {
			PokemonInfo p = info[i];
			
			if (!p.canBreed() && !p.getEvolution().canEvolve()) {
				set.remove(p.namesies());
			}
			
			for (Namesies s : p.getEvolution().getEvolutions()) {
				set.remove(s);
			}
		}
		
		PokemonInfo[] p = new PokemonInfo[set.size()];
		int i = 0;
		for (Namesies s : set) {
			p[i++] = getPokemonInfo(s);
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
				Namesies namesies = Namesies.getValueOf(in.nextLine().trim(), NamesiesType.POKEMON);
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
					createLevelUpMoves(in), createMovesHashSet(in), createMovesHashSet(in), createMovesHashSet(in),
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
	
	private static Map<Integer, Set<Namesies>> createLevelUpMoves(Scanner in) {
		Map<Integer, Set<Namesies>> levelUpMoves = new TreeMap<>();
		int numMoves = in.nextInt();
		
		for (int i = 0; i < numMoves; i++) {
			int level = in.nextInt();
			if (!levelUpMoves.containsKey(level)) {
				levelUpMoves.put(level, new TreeSet<>());
			}
			
			String attackName = in.nextLine().trim();
			Namesies namesies = Namesies.getValueOf(attackName, NamesiesType.ATTACK);

			if (level < 0 || level > ActivePokemon.MAX_LEVEL) {
				Global.error("Cannot learn moves at level " + level + " (Move: " + attackName + ")");
			}

			levelUpMoves.get(level).add(namesies);
		}
		
		return levelUpMoves;
	}
	
	private static Set<Namesies> createMovesHashSet(Scanner in) {
		Set<Namesies> tmMoves = new HashSet<>();
		int numMoves = in.nextInt();
		in.nextLine();
		
		for (int i = 0; i < numMoves; i++) {
			String attackName = in.nextLine().trim();
			
			Namesies namesies = Namesies.getValueOf(attackName, NamesiesType.ATTACK);
			tmMoves.add(namesies);
		}
		
		return tmMoves;
	}
	
	public Set<Namesies> getMoves(int level) {
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
		
		public WildHoldItem(int c, String i) {
			if (!Item.isItem(i)) {
				Global.error("Invalid wild hold item name " + i);
			}
			
			item = (HoldItem)Item.getItemFromName(i);
			chance = c;
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
		if (targetPokes.namesies() == Namesies.MANAPHY_POKEMON) {
			return PokemonInfo.getPokemonInfo(Namesies.PHIONE_POKEMON);
		}

		Set<String> allPokes = map.keySet();
		while (true) {
			boolean changed = false;
			for (String pokesName : allPokes) {
				PokemonInfo pokes = map.get(pokesName);
				Namesies[] evolutionNamesies = pokes.getEvolution().getEvolutions();
				for (Namesies namesies : evolutionNamesies) {
					if (namesies.equals(targetPokes.namesies())) {
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
	
	public static void addIncenseBaby(Namesies incenseBaby) {
		incenseBabies.add(incenseBaby);
	}
	
	// Returns what level the Pokemon will learn the given attack, returns -1 if they cannot learn it by level up
	public int levelLearned(Namesies attack) {
		for (Integer level : getLevelUpMoves().keySet()) {
			for (Namesies levelUpMove : getLevelUpMoves().get(level)) {
				if (attack == levelUpMove) {
					return level;
				}
			}
		}
		
		return -1;
	}
	
	public boolean canLearnMove(Namesies attack) {
		return levelLearned(attack) != -1 || canLearnByBreeding(attack);
	}

	public boolean canLearnByBreeding(Namesies attack) {
		return this.learnableMoves.contains(attack);
	}
}
