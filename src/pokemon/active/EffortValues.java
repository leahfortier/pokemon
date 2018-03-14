package pokemon.active;

import pokemon.Stat;
import util.serialization.Serializable;

public class EffortValues implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_EVS = 510;
    public static final int MAX_STAT_EVS = 255;

    private int[] EVs;

    EffortValues() {
        this.EVs = new int[Stat.NUM_STATS];
    }

    public int getEV(int index) {
        return EVs[index];
    }

    public int totalEVs() {
        int sum = 0;
        for (int EV : EVs) {
            sum += EV;
        }
        return sum;
    }

    // Adds Effort Values to a Pokemon, returns true if they were successfully added
    boolean addEVs(int[] vals) {
        if (this.totalEVs() == MAX_EVS) {
            return false;
        }

        boolean added = false;
        for (int i = 0; i < EVs.length; i++) {
            if (vals[i] > 0 && EVs[i] < MAX_STAT_EVS) {
                added = true;
                EVs[i] = Math.min(MAX_STAT_EVS, EVs[i] + vals[i]); // Don't exceed stat EV amount

                // Don't exceed total EV amount
                if (this.totalEVs() > MAX_EVS) {
                    EVs[i] -= (totalEVs() - MAX_EVS);
                    break;
                }
            } else if (vals[i] < 0 && EVs[i] > 0) {
                added = true;
                EVs[i] = Math.max(0, EVs[i] + vals[i]); // Don't drop below zero
            }
        }

        return added;
    }
}
