package teamplanner;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import type.Type;
import util.FileIO;
import util.StringAppender;
import util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class TeamMember {
    private static final Type[] types = Type.values();
    private static final List<Double> coverageValues = Arrays.asList(4.0, 2.0, 0.5, 0.0);

    final PokemonInfo pokemonSpecies;
    final List<AttackNamesies> moveList;
    final int[][] coverageCount;

    private final String nature;
    private final String ability;
    private final String item;
    private final double[][] coverage;

    TeamMember(String pokemonName, String nature, String ability, String item, List<String> moves) {
        this.pokemonSpecies = PokemonNamesies.getValueOf(pokemonName).getInfo();
        this.nature = nature;
        this.ability = ability;
        this.item = item;

        this.moveList = new ArrayList<>();
        this.coverage = new double[types.length][types.length];
        this.coverageCount = new int[types.length][types.length];

        for (String moveName : moves) {
            Attack attack = AttackNamesies.getValueOf(moveName).getNewAttack();
            this.moveList.add(attack.namesies());

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

    private String getCoverageString() {
        return new StringAppender()
                .appendFormat("%10s: ", this.pokemonSpecies.getName())
                .appendLine()
                .appendJoin("\n", coverageValues, this::getCoverageString)
                .toString();
    }

    private String getCoverageString(double coverageVal) {
        StringAppender types = new StringAppender();
        for (Type firstType : TeamMember.types) {
            if (firstType == Type.NO_TYPE) {
                continue;
            }

            for (Type secondType : TeamMember.types) {
                int first = firstType.getIndex();
                int second = secondType.getIndex();

                if (second <= first) {
                    continue;
                }

                if (coverage[first][second] == coverageVal) {
                    types.appendDelimiter(", ", firstType.getName())
                         .appendIf(secondType != Type.NO_TYPE, "/" + secondType.getName());
                }
            }
        }

        return String.format("%12s%3.2fx -- ", "", coverageVal) + types.toString();
    }

    @Override
    public String toString() {
        StringAppender out = new StringAppender();

        out.append(pokemonSpecies.getName() + ":");

        Type[] type = pokemonSpecies.getType();
        out.append("\n\tType: " + type[0].getName())
           .appendIf(type[1] != Type.NO_TYPE, "/" + type[1].getName());

        out.append("\n\tStats: ")
           .appendJoin(" ", Stat.NUM_STATS, i -> pokemonSpecies.getStat(i) + "");

        out.append("\n\tNature: " + nature);
        out.append("\n\tAbility: " + ability);
        out.appendIf(item != null, "\n\tItem: " + item);

        out.append("\n\tMoves:");
        for (AttackNamesies attack : moveList) {
            out.append("\n\t\t" + attack.getName() + " -- ");

            List<String> learnMethods = new ArrayList<>();

            Integer levelLearned = pokemonSpecies.levelLearned(attack);
            if (levelLearned != null) {
                if (levelLearned == 0) {
                    learnMethods.add("Heart Scale");
                } else if (levelLearned == PokemonInfo.EVOLUTION_LEVEL_LEARNED) {
                    learnMethods.add("Evolve");
                } else {
                    learnMethods.add("Level " + levelLearned);
                }
            }

            if (pokemonSpecies.canLearnByBreeding(attack)) {
                learnMethods.add("Egg Move/TM move/Move Tutor");
            }

            if (learnMethods.isEmpty()) {
                learnMethods.add("???");
            }

            out.appendJoin(" or ", learnMethods);
        }

        out.append("\n\n");

        return out.toString();
    }

    static void printTeam(List<TeamMember> team) {
        for (TeamMember member : team) {
            System.out.println(member.getCoverageString());
        }

        StringAppender out = new StringAppender()
                .appendJoin(StringUtils.empty(), team);

        FileIO.overwriteFile("teamPlanner.out", out.toString());
    }
}
