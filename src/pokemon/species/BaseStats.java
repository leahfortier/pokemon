package pokemon.species;

import pokemon.Stat;
import util.string.StringAppender;

public class BaseStats {
    private final int[] baseStats;

    public BaseStats(int[] baseStats) {
        this.baseStats = baseStats;
    }

    public int get(int statIndex) {
        return baseStats[statIndex];
    }

    public int get(Stat stat) {
        return this.get(stat.index());
    }

    @Override
    public String toString() {
        return new StringAppender()
                .appendJoin(" ", Stat.NUM_STATS, stat -> Integer.toString(this.get(stat)))
                .toString();
    }
}
