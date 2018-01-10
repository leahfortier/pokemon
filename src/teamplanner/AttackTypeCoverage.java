package teamplanner;

import battle.attack.Attack;
import type.Type;
import util.StringAppender;

import java.util.ArrayList;
import java.util.List;

class AttackTypeCoverage {
    private final Type attackType;
    private final List<String> moves;

    AttackTypeCoverage(Type type) {
        this.attackType = type;
        this.moves = new ArrayList<>();
    }

    @Override
    public String toString() {
        return new StringAppender()
                .appendFormat("%8s: %d ", this.attackType.getName(), this.moves.size())
                .appendJoin("\n            ", this.moves)
                .toString();
    }

    static void printCoverage(AttackTypeCoverage[] coverage) {
        for (AttackTypeCoverage attackTypeCoverage : coverage) {
            System.out.println(attackTypeCoverage);
        }
        System.out.println();
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