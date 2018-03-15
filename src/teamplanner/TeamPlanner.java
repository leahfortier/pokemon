package teamplanner;

import battle.attack.AttackNamesies;
import main.Global;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Nature;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import type.PokeType;
import type.Type;
import util.file.FileIO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TeamPlanner {
    private static final Type[] types = Type.values();

    public static void main(String[] args) {
        new TeamPlanner();
    }

    private TeamPlanner() {
        List<TeamMember> team = readTeam();

        OffensiveCoverage offensiveCoverage = new OffensiveCoverage();

        AttackTypeCoverage[] coverage = Arrays.stream(types)
                                              .filter(type -> type != Type.NO_TYPE)
                                              .map(AttackTypeCoverage::new)
                                              .toArray(AttackTypeCoverage[]::new);

        for (TeamMember member : team) {
            AttackTypeCoverage.addCoverage(coverage, member);
            offensiveCoverage.countCoverage(member);
        }

        TeamMember.printTeam(team);
        AttackTypeCoverage.printCoverage(coverage);
        offensiveCoverage.printTableAndList();

//        moveMatching("Surf", "Thunderbolt");
//        moveMatching("Flamethrower", Type.WATER);
    }

    private static List<TeamMember> readTeam() {
        Scanner in = FileIO.openFile("teamPlanner.in");
        List<TeamMember> team = new ArrayList<>();

        System.out.println("Read Team");

        while (in.hasNextLine()) {
            String pokemonName = in.nextLine().trim();
            PokemonInfo pokemon = PokemonNamesies.getValueOf(pokemonName).getInfo();

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
                        if (ability != null) {
                            Global.error("Ability already defined for " + pokemonName);
                        }
                        ability = value;
                        break;
                    case "Nature":
                        if (nature != null) {
                            Global.error("Nature already defined for " + pokemonName);
                        }
                        nature = value;
                        break;
                    case "Item":
                        if (item != null) {
                            Global.error("Item already defined for " + pokemonName);
                        }
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
            PokeType types = p.getType();

            if (p.canLearnMove(firstMove) && types.isType(type)) {
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
}
