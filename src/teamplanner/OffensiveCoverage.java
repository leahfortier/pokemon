package teamplanner;

import type.Type;
import util.string.StringUtils;

class OffensiveCoverage {
    private static final Type[] types = Type.values();

    private int maxCoverage;
    private String[] coverageFrequencyList;
    private int[][] coverageCount;

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

        for (int i = 0; i < coverageCount.length; i++) {
            System.out.printf("%10s ", types[i].getName());
            for (int j = 0; j < coverageCount[i].length; j++) {
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

    private void addFrequency(int frequency, Type firstType, Type secondType) {
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
