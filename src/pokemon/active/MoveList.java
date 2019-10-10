package pokemon.active;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import main.Global;
import pokemon.species.LevelUpMove;
import util.RandomUtils;
import util.serialization.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoveList implements Iterable<Move>, Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_MOVES = 4;

    private List<Move> moves;

    public MoveList(List<Move> moves) {
        this.setMoves(moves);
    }

    MoveList(MoveList moves) {
        this.setMoves(moves.moves);
    }

    MoveList(PartyPokemon movesHolder) {
        moves = new ArrayList<>();
        List<LevelUpMove> levelUpMoves = movesHolder.getPokemonInfo().getLevelUpMoves();
        for (LevelUpMove levelUpMove : levelUpMoves) {
            AttackNamesies attackNamesies = levelUpMove.getMove();
            if (levelUpMove.getLevel() > movesHolder.getLevel()) {
                break;
            }

            if (this.hasMove(attackNamesies)) {
                continue;
            }

            moves.add(new Move(attackNamesies));

            // This can be an 'if' statement, but just to be safe...
            while (moves.size() > MAX_MOVES) {
                moves.remove(0);
            }
        }
    }

    void setMoves(List<Move> list) {
        if (list.isEmpty() || list.size() > MAX_MOVES) {
            Global.error("Invalid move list: " + list);
        }

        moves = list;
    }

    // Adds the move at the specified index if full and to the end otherwise
    // Does not handle evolution or anything else like that -- should be handled in subclasses
    public void add(Move m, int index) {
        if (moves.size() < MAX_MOVES) {
            moves.add(m);
        } else {
            moves.set(index, m);
        }
    }

    public void set(Move m, int index) {
        moves.set(index, m);
    }

    void restoreAllPP() {
        this.moves.forEach(Move::resetPP);
    }

    public void resetAttacks() {
        this.moves.forEach(Move::resetAttack);
    }

    public int size() {
        return this.moves.size();
    }

    public boolean hasMove(AttackNamesies name) {
        for (Move m : moves) {
            if (m.getAttack().namesies() == name) {
                return true;
            }
        }

        return false;
    }

    public Move get(int index) {
        return this.moves.get(index);
    }

    public Stream<Move> stream() {
        return this.moves.stream();
    }

    public List<Move> filter(Predicate<Move> filter) {
        return this.stream().filter(filter).collect(Collectors.toList());
    }

    public Move getRandomMove() {
        return this.get(RandomUtils.getRandomInt(this.size()));
    }

    @Override
    public Iterator<Move> iterator() {
        return this.moves.iterator();
    }
}
