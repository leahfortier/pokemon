package pokemon.active;

import pokemon.stat.Stat;
import util.RandomUtils;
import util.serialization.Serializable;

public class IndividualValues implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_IV = 31;

    private static final String[][] characteristics =
            {{"Loves to eat",            "Proud of its power",      "Sturdy body",            "Highly curious",        "Strong willed",     "Likes to run"},
             {"Takes plenty of siestas", "Likes to thrash about",   "Capable of taking hits", "Mischievous",           "Somewhat vain",     "Alert to sounds"},
             {"Nods off a lot",          "A little quick tempered", "Highly persistent",      "Thoroughly cunning",    "Strongly defiant",  "Impetuous and silly"},
             {"Scatters things often",   "Likes to fight",          "Good endurance",         "Often lost in thought", "Hates to lose",     "Somewhat of a clown"},
             {"Likes to relax",          "Quick tempered",          "Good perseverance",      "Very finicky",          "Somewhat stubborn", "Quick to flee"}};

    private int[] IVs;
    private String characteristic;

    IndividualValues() {
        this.setIVs();
    }

    IndividualValues(IndividualValues other) {
        this.setIVs(other.IVs);
    }

    public IndividualValues(int[] IVs) {
        this.setIVs(IVs);
    }

    // Random value between 0 and 31
    private void setIVs() {
        int[] IVs = new int[Stat.NUM_STATS];
        for (int i = 0; i < IVs.length; i++) {
            IVs[i] = getRandomIv();
        }

        this.setIVs(IVs);
    }

    // Values between 0 and 31
    private void setIVs(int[] IVs) {
        this.IVs = IVs;

        int maxIndex = 0;
        for (int i = 0; i < this.IVs.length; i++) {
            if (this.IVs[i] > this.IVs[maxIndex]) {
                maxIndex = i;
            }
        }

        this.characteristic = characteristics[this.IVs[maxIndex]%5][maxIndex];
    }

    void setIVs(IndividualValues IVs) {
        this.setIVs(IVs.IVs);
    }

    public int get(int index) {
        return IVs[index];
    }

    public int get(Stat stat) {
        return this.get(stat.index());
    }

    public String getCharacteristic() {
        return this.characteristic;
    }

    public static int getRandomIv() {
        return RandomUtils.getRandomInt(MAX_IV + 1);
    }
}
