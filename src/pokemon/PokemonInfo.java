package pokemon;

import gui.GameFrame;
import item.Item;
import item.hold.HoldItem;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import main.Global;
import main.StuffGen;
import main.Type;
import battle.Attack;

public class PokemonInfo implements Serializable, Comparable<PokemonInfo>
{
	private static final long serialVersionUID = 1L;

	public static int NUM_POKEMON = 649;
	
	private static String[] pokemonList;
	private static HashMap<String, PokemonInfo> map;
	private static PokemonInfo[] info;
	private static List<PokemonInfo> baseEvolution;
	
	private int number;
	private String name;
	private int[] baseStats;
	private int baseExp;
	private GrowthRate growthRate;
	private Type[] type;
	private TreeMap<Integer, List<String>> levelUpMoves; 
	private int catchRate;
	private int[] givenEVs;
	private Evolution evolution;
	private List<WildHoldItem> wildHoldItems;
	private String[] abilities;
	private int maleRatio;
	private String classification;
	private int height;
	private double weight;
	private String flavorText;
	private int eggSteps;
	private String eggGroup1;
	private String eggGroup2;
	
	public PokemonInfo (int num, String name, int[] bStats, int bExp, String gRate, String t1, String t2, 
			TreeMap<Integer, List<String>> luMoves, int cRate, int[] gEVs, Evolution evo, List<WildHoldItem> items, 
			int gRatio, String ab1, String ab2, String classif, int height, double weight, String flavText,
			int steps, String group1, String group2)
	{	
		number = num;
		this.name = name;
		baseStats = bStats;
		baseExp = bExp;
		growthRate = GrowthRate.getRate(gRate);
		type = new Type[] {Type.valueOf(t1.toUpperCase()), Type.valueOf(t2.toUpperCase())};
		levelUpMoves = luMoves;
		catchRate = cRate;
		givenEVs = gEVs;
		evolution = evo;
		wildHoldItems = items;
		abilities = new String[] {ab1, ab2};
		maleRatio = gRatio;
		classification = classif;
		this.height = height;
		this.weight = weight;
		flavorText = flavText;
		eggSteps = steps;
		eggGroup1 = group1;
		eggGroup2 = group2;
		
		verifyAbilities();
	}
	
	private void verifyAbilities()
	{
		if (GameFrame.GENERATE_STUFF)
		{
			for (int i = 0; i < 2; i++)
			{
				if (abilities[i].equals("Gluttony")
						|| abilities[i].equals("Multitype")
						|| abilities[i].equals("Forecast")) continue;
				Ability.getAbility(abilities[i]);				
			}
		}
	}
	
	public Type[] getType()
	{
		return type;
	}
	
	public TreeMap<Integer, List<String>> getLevelUpMoves()
	{
		return levelUpMoves;
	}
	
	public int getStat(int index)
	{
		return baseStats[index];
	}
	
	public GrowthRate getGrowthRate()
	{
		return growthRate;
	}
	
	public int getEggSteps()
	{
		return eggSteps;
	}
	
	public String[] getAbilities()
	{
		return abilities;
	}
	
	public boolean hasAbility(String s)
	{
		return abilities[0].equals(s) || abilities[1].equals(s);
	}
	
	public int getCatchRate()
	{
		return catchRate;
	}
	
	public int getBaseEXP()
	{
		return baseExp;
	}
	
	public int[] getGivenEVs()
	{
		return givenEVs;
	}
	
	public int getMaleRatio()
	{
		return maleRatio;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public double getWeight()
	{
		return weight;
	}
	
	public String getClassification()
	{
		return classification;
	}
	
	public String getFlavorText()
	{
		return flavorText;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public Evolution getEvolution()
	{
		return evolution;
	}
	
	public List<WildHoldItem> getWildItems()
	{
		return wildHoldItems;
	}
	
	public int getImageNumber(boolean shiny)
	{
		return getImageNumber(number, shiny);
	}
	
	public static int getImageNumber(int number, boolean shiny)
	{
		if (shiny) return 4*number + 2;
		return 4*number;
	}
	
	public static String[] getPokemonList()
	{
		if (pokemonList == null) loadPokemonList();
		return pokemonList;
	}
	
	public static void loadPokemonList()
	{
		if (pokemonList != null) return;
		Scanner in = new Scanner(Global.readEntireFile(new File("pokedex.txt"), false));
		
		pokemonList = new String[in.nextInt()];
		in.nextLine();
		for (int i = 0; i < pokemonList.length; i++)
		{
			pokemonList[i] = in.nextLine().trim();
		}
		
		in.close();
	}
	
	public static PokemonInfo getPokemonInfo(String p)
	{
		if (map == null) loadPokemonInfo();
		if (map.containsKey(p)) return map.get(p);

		Global.error("No such Pokemon " + p);
		return null;
	}
	
	public static PokemonInfo getPokemonInfo(int index)
	{
		if (info == null) loadPokemonInfo();
		if (index <= 0 || index > NUM_POKEMON) Global.error("No such Pokemon Number " + index); 

		return info[index];
	}
	
	public static boolean isPokemon(String p)
	{
		if (map == null) loadPokemonInfo();
		if (map.containsKey(p)) return true;
		return false;
	}
	
	public static void baseEvolutionGenerator()
	{
		if (info == null) loadPokemonInfo();
		
		Set<String> set = new HashSet<>();
		for (int i = 1; i < info.length; i++) set.add(info[i].getName());
		
		for (int i = 1; i < info.length; i++)
		{
			PokemonInfo p = info[i];
			
			if (!p.canBreed() && !p.getEvolution().canEvolve()) set.remove(p.getName());
			for (String s : p.getEvolution().getEvolutions()) set.remove(s);
		}
		
		PokemonInfo[] p = new PokemonInfo[set.size()];
		int i = 0;
		for (String s : set) p[i++] = getPokemonInfo(s);
		
		Arrays.sort(p);
		
		StringBuilder out = new StringBuilder();
		for (PokemonInfo info : p) out.append(info.getName() + "\n");
		StuffGen.printToFile("BaseEvolutions.txt", out);
	}
	
	public static PokemonInfo getRandomBaseEvolution()
	{
		if (baseEvolution == null)
		{
			baseEvolution = new ArrayList<>();
			Scanner in = new Scanner(Global.readEntireFile(new File("BaseEvolutions.txt"), false));
			while (in.hasNext())
			{
				baseEvolution.add(PokemonInfo.getPokemonInfo(in.nextLine().trim()));
			}
			in.close();
		}
		
		return baseEvolution.get((int)(Math.random()*baseEvolution.size()));
	}
	
	public int compareTo(PokemonInfo p)
	{
		return number - p.number;
	}

	// Create and load the Pokemon info map if it doesn't already exist
	public static void loadPokemonInfo()
	{
		if (map != null) return;
		map = new HashMap<String, PokemonInfo>();
		info = new PokemonInfo[NUM_POKEMON + 1];

		Scanner in = new Scanner(Global.readEntireFile(new File("pokemoninfo.txt"), false));
		while (in.hasNext())
		{
			int num = in.nextInt();
			in.nextLine();
			info[num] = new PokemonInfo(num, in.nextLine().trim(), sixIntArray(in),
					in.nextInt(), in.nextLine().trim() + in.nextLine().trim(), in.next(), in.next(), 
					createLevelUpMoves(in), in.nextInt(), sixIntArray(in), Evolution.readEvolution(in),
					WildHoldItem.createList(in), in.nextInt(), in.nextLine().trim() + in.nextLine().trim(), 
					in.nextLine().trim(), in.nextLine().trim(), in.nextInt(), in.nextDouble(), 
					in.nextLine().trim(), in.nextInt(), in.nextLine().trim() + in.nextLine().trim(), in.nextLine().trim());
			map.put(info[num].getName(), info[num]);
		}
		
		in.close();
	}
	
	private static int[] sixIntArray(Scanner in)
	{
		int[] arr = new int[6];
		for (int i = 0; i < 6; i++) arr[i] = in.nextInt();
		return arr;
	}
	
	private static TreeMap<Integer, List<String>> createLevelUpMoves(Scanner in)
	{
		TreeMap<Integer, List<String>> lum = new TreeMap<>();
		int numMoves = in.nextInt();
		for (int i = 0; i < numMoves; i++)
		{
			int level = in.nextInt();
			if (!lum.containsKey(level)) lum.put(level, new ArrayList<String>());
			lum.get(level).add(in.nextLine().trim());
			
			String name = lum.get(level).get(lum.get(level).size() - 1);
			if (GameFrame.GENERATE_STUFF) Attack.getAttack(name);
		}
		return lum;
	}
	
	public List<String> getMoves(int level)
	{
		if (levelUpMoves.containsKey(level)) return levelUpMoves.get(level);
		return new ArrayList<>();
	}
	
	public boolean canBreed()
	{
		return !eggGroup1.equals("None") || !eggGroup2.equals("None");
	}
	
	public static class WildHoldItem implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private HoldItem item;
		private int chance;
		
		public WildHoldItem(int c, String i)
		{
			item = (HoldItem)Item.getItem(i);
			chance = c;
		}
		
		public static List<WildHoldItem> createList(Scanner in)
		{
			List<WildHoldItem> list = new ArrayList<WildHoldItem>();
			int num = in.nextInt();
			for (int i = 0; i < num; i++) list.add(new WildHoldItem(in.nextInt(), in.nextLine().trim()));
			return list;
		}
		
		public static HoldItem getWildHoldItem(List<WildHoldItem> list)
		{
			int random = (int)(Math.random()*100), sum = 0;
			for (WildHoldItem i : list)
			{
				sum += i.chance;
				if (random < sum) return i.item;
			}
			return (HoldItem)Item.noneItem();
		}
	}
}
