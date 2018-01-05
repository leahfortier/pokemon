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
    
    public boolean isDefaultLevel() {
        return isDefaultLevel(this.level);
    }
    
    public static boolean isDefaultLevel(int level) {
        return level == 0 || level == PokemonInfo.EVOLUTION_LEVEL_LEARNED;
    }
}
