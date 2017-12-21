package pokemon;

import battle.attack.AttackNamesies;

public class LevelUpMove {
    private final int level;
    private final AttackNamesies move;

    public LevelUpMove(int level, AttackNamesies move) {
        this.level = level;
        this.move = move;
    }

    public int getLevel() {
        return this.level;
    }

    public AttackNamesies getMove() {
        return this.move;
    }
}
