package main;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import pokemon.Nature;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import type.Type;
import type.TypeAdvantage;
import util.FileIO;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TeamPlanner {
	private static Type[] types = Type.values();
	private static double[] coverageValues = { 4, 2, .5, 0 };
	
	public TeamPlanner() {
		List<TeamMember> team = readTeam();
		
		OffensiveCoverage offensiveCoverage = new OffensiveCoverage();
		
		AttackTypeCoverage[] coverage = new AttackTypeCoverage[types.length - 1];
		for (int i = 0; i < types.length - 1; i++) {
			coverage[i] = new AttackTypeCoverage(types[i]);
		}
		
		for (TeamMember member : team) {
			member.printAllCoverage();
			
			AttackTypeCoverage.addCoverage(coverage, member);
			offensiveCoverage.countCoverage(member);
		}
		System.out.println();
		
		AttackTypeCoverage.printCoverage(coverage);
		offensiveCoverage.printTableAndList();
		
//		moveMatching("Flamethrower", "Signal Beam");
//		moveMatching("Ice Beam", Type.GRASS);
		
		TeamMember.printTeam(team);
	}
	
	private static List<TeamMember> readTeam() {
		Scanner in = FileIO.openFile("teamPlanner.in");
		List<TeamMember> team = new ArrayList<>();
		
		System.out.println("Read Team");
		
		while (in.hasNextLine()) {
			String pokemonName = in.nextLine().trim();
			PokemonInfo pokemon = PokemonInfo.getPokemonInfo(PokemonNamesies.getValueOf(pokemonName));
			
			String ability = null;
			String nature = null;
			String item = null;
			
			List<String> moves = new ArrayList<>();
			
			while (true) {
				String line = in.nextLine().trim();
				if (line.equals("***")) {
					break;
				}
				
				String[] split = line.split(":");
				String key = split[0].trim();
				String value = split.length == 1 ? null : split[1].trim();
				
				switch (key) {
					case "Ability":
						if (ability != null) Global.error("Ability already defined for " + pokemonName);
						
						ability = value;
						break;
					case "Nature":
						if (nature != null) Global.error("Nature already defined for " + pokemonName);
						
						nature = value;
						break;
					case "Item":
						if (item != null) Global.error("Item already defined for " + pokemonName);
						
						item = value;
						break;	
					case "Moves":
						while (true) {
							String move = in.nextLine().trim();
							
							if (move.equals("*")) {
								break;
							}
							
							moves.add(move);
						}
						break;
					default:
						Global.error("Undefined command " + key + " for " + pokemonName);
						break;
				}
			}
			
			if (ability == null) {
				AbilityNamesies[] abilities = pokemon.getAbilities();
				ability = abilities[0].getName() + (abilities[1] == AbilityNamesies.NO_ABILITY ? "" : "/" + abilities[1].getName());
			}
			
			if (nature == null) {
				Stat decrease = pokemon.getStat(Stat.ATTACK.index()) < pokemon.getStat(Stat.SP_ATTACK.index()) ? Stat.ATTACK : Stat.SP_ATTACK;
				for (int i = 0; i < Stat.NUM_STATS; i++) {
					if (i == Stat.HP.index() || i == decrease.index()) {
						continue;
					}
					
					Nature n = new Nature(i, decrease.index());
					nature = (nature == null ? "" : nature + ", ") + n.getName();
				}
			}
			
			TeamMember member = new TeamMember(pokemonName, nature, ability, item, moves);
			team.add(member);
		}
		
		return team;
	}
	
	private static void moveMatching(String firstMoveName, Type type) {
		System.out.println("\nThe following Pokemon can learn " + firstMoveName + " and is type " + type);
		
		AttackNamesies firstMove = AttackNamesies.getValueOf(firstMoveName);
		
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
			PokemonInfo p = PokemonInfo.getPokemonInfo(i);
			Type[] types = p.getType();
			
			if (p.canLearnMove(firstMove) && (types[0] == type || types[1] == type)) {
				System.out.println("\t" + p.getName());
			}
		}
	}
	
	private static void moveMatching(String firstMoveName, String secondMoveName) {
		System.out.println("\nThe following Pokemon can learn " + firstMoveName + " and " + secondMoveName);

		AttackNamesies firstMove = AttackNamesies.getValueOf(firstMoveName);
		AttackNamesies secondMove = AttackNamesies.getValueOf(secondMoveName);
		
		for (int i = 1; i <= PokemonInfo.NUM_POKEMON; i++) {
			PokemonInfo p = PokemonInfo.getPokemonInfo(i);
			if (p.canLearnMove(firstMove) && p.canLearnMove(secondMove)) {
				System.out.println("\t" + p.getName());
			}
		}
	}
	
	private static void printCoverage(double coverageVal, double[][] coverage) {
		System.out.printf("%n%12s%3.2fx -- ", "", coverageVal);
		boolean comma = false;
		for (Type firstType : types) {
			if (firstType == Type.NO_TYPE) {
				continue;
			}
			
			for (Type secondType : types) {
				int first = firstType.getIndex();
				int second = secondType.getIndex();
				
				if (second <= first) {
					continue;
				}
				
				if (coverage[first][second] == coverageVal) {
					System.out.print((comma ? ", " : "") + firstType.getName() + (secondType == Type.NO_TYPE ? "" : "/" + secondType.getName()));
					comma = true;
				}
			}
		}
	}
	
	private class OffensiveCoverage {
		int maxCoverage;
		String[] coverageFrequencyList;
		int[][] coverageCount;
		
		OffensiveCoverage() {
			coverageCount = new int[types.length][types.length];
			maxCoverage = 0;
		}
		
		void printTableAndList() {
			System.out.printf("%10s ", "");
			for (Type type : types) {
				System.out.printf("%-10s ", type.getName());
			}
			System.out.println();
			
			for (int i = 0; i < coverageCount.length; i++)
			{
				System.out.printf("%10s ", types[i].getName());
				for (int j = 0; j < coverageCount[i].length; j++)
				{
					System.out.printf("%5d%5s ", coverageCount[i][j], "");
					this.addFrequency(coverageCount[i][j], types[i], types[j]);
				}
				System.out.println();
			}
			
			for (int i = 0; i < coverageFrequencyList.length; i++) {
				System.out.println(i + ": " + coverageFrequencyList[i]);
			}
		}
		
		void countCoverage(TeamMember member) {
			for (int i = 0; i < coverageCount.length; i++) {
				for (int j = 0; j < coverageCount[i].length; j++) {
					coverageCount[i][j] += member.coverageCount[i][j];
					maxCoverage = Math.max(maxCoverage, coverageCount[i][j]);
				}
			}
		}
		
		void addFrequency(int frequency, Type firstType, Type secondType) {
			if (firstType == Type.NO_TYPE || secondType.getIndex() <= firstType.getIndex()) {
				return;
			}
			
			if (coverageFrequencyList == null) {
				coverageFrequencyList = new String[maxCoverage + 1];
				for (int i = 0; i < coverageFrequencyList.length; i++) {
					coverageFrequencyList[i] = StringUtils.empty();
				}
			}
			
			if (coverageFrequencyList[frequency].length() > 0) {
				coverageFrequencyList[frequency] += ", ";
			}
			
			coverageFrequencyList[frequency] += firstType.getName() + (secondType == Type.NO_TYPE ? "" : "/" + secondType.getName());
		}
	}
	
	private static class AttackTypeCoverage {
		Type attackType;
		List<String> moves;
		
		AttackTypeCoverage(Type type) {
			this.attackType = type;
			this.moves = new ArrayList<>();
		}
		
		static void printCoverage(AttackTypeCoverage[] coverage) {
			for (AttackTypeCoverage type : coverage) {
				System.out.printf("%8s: %d ", type.attackType.getName(), type.moves.size());
				boolean first = true;
				for (String move : type.moves) {
					System.out.print((first ? "" : "\n            ") + move);
					first = false;
				}
				System.out.println();
			}
		}
		
		static void addCoverage(AttackTypeCoverage[] coverage, TeamMember member) {
			for (Attack attack : member.moveList) {
				if (!attack.isStatusMove()) {
					Type attackType = attack.getActualType();
					coverage[attackType.getIndex()].moves.add(member.pokemonSpecies.getName() + " - " + attack.getName());
				}
			}
		}
	}
	
	private static class TeamMember {
		private final PokemonInfo pokemonSpecies;
		private final List<Attack> moveList;
		private final String nature;
		private final String ability;
		private final String item;
		private final double[][] coverage;
		private final int[][] coverageCount;
		
		TeamMember(String pokemonName, String nature, String ability, String item, List<String> moves) {
			this.pokemonSpecies = PokemonInfo.getPokemonInfo(PokemonNamesies.getValueOf(pokemonName));
			this.nature = nature;
			this.ability = ability;
			this.item = item;
			
			this.moveList = new ArrayList<>();
			this.coverage = new double[types.length][types.length];
			this.coverageCount = new int[types.length][types.length];
			
			for (String moveName : moves) {
				Attack attack = AttackNamesies.getValueOf(moveName).getAttack();
				this.moveList.add(attack);
				
				Type attackType = attack.getActualType();
				for (Type firstType : types) {
					for (Type secondType : types) {
						int first = firstType.getIndex();
						int second = secondType.getIndex();
						
						double advantage = attackType.getAdvantage().getAdvantage(firstType, secondType);
						
						if (advantage > 1) {
							coverageCount[first][second]++;
						}
						
						coverage[first][second] = Math.max(coverage[first][second], advantage);
					}
				}
			}
		}
		
		void printAllCoverage() {
			System.out.printf("%10s: ", this.pokemonSpecies.getName());
			for (double coverageVal : coverageValues) {
				printCoverage(coverageVal, this.coverage);
			}
			System.out.println();
		}
		
		static void printTeam(List<TeamMember> team) {
			StringBuilder out = new StringBuilder();
			for (TeamMember member : team) {
				out.append(member.toString());
			}
			
			FileIO.writeToFile("teamPlanner.out", out);
		}
		
		public String toString() {
			StringBuilder out = new StringBuilder();
			
			out.append(pokemonSpecies.getName() + ":");
			
			Type[] type = pokemonSpecies.getType();
			out.append("\n\tType: " + type[0].getName() + (type[1] == Type.NO_TYPE ? "" : "/" + type[1].getName()));
			
			
			out.append("\n\tStats:");
			for (int i = 0; i < Stat.NUM_STATS; i++) {
				out.append(" " + pokemonSpecies.getStat(i));
			}
			
			out.append("\n\tNature: " + nature);
			out.append("\n\tAbility: " + ability);
			
			if (item != null) {
				out.append("\n\tItem: " + item);
			}
			
			out.append("\n\tMoves:");
			for (Attack attack : moveList) {
				out.append("\n\t\t" + attack.getName() + " -- ");
				List<String> learnMethods = new ArrayList<String>();

				AttackNamesies namesies = attack.namesies();
				
				int levelLearned = pokemonSpecies.levelLearned(namesies);
				if (levelLearned == 0) {
					learnMethods.add("Heart Scale");
				}
				else if (levelLearned != -1) {
					learnMethods.add("Level " + levelLearned);
				}
				
				if (pokemonSpecies.canLearnByBreeding(namesies)) {
					learnMethods.add("Egg Move/TM move/Move Tutor");
				}
				
				if (learnMethods.size() == 0) {
					learnMethods.add("???");
				}
				
				boolean first = true;
				for (String method : learnMethods) {
					out.append((first ? "" : " or ") + method);
					first = false;
				}
			}
			
			out.append("\n\n");
			
			return out.toString();
		}
	}
}
