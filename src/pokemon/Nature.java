package pokemon;

import util.RandomUtils;

import java.awt.Color;
import java.io.Serializable;

public class Nature implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String[][] natures = {
            { "", "",       "",       "",        "",        ""        },
            { "", "Hardy",  "Lonely", "Adamant", "Naughty", "Brave"   },
            { "", "Bold",   "Docile", "Impish",  "Lax",     "Relaxed" },
            { "", "Modest", "Mild",   "Bashful", "Rash",    "Quiet"   },
            { "", "Calm",   "Gentle", "Careful", "Quirky",  "Sassy"   },
            { "", "Timid",  "Hasty",  "Jolly",   "Naive",   "Serious" }
    };

    private final int beneficial;
    private final int hindering;
    private final String name;

    public Nature() {
        this(getRandomNatureStatIndex(), getRandomNatureStatIndex());
    }

    public Nature(int beneficialStat, int hinderingStat) {
        this.beneficial = beneficialStat;
        this.hindering = hinderingStat;

        this.name = natures[beneficial][hindering];
    }

    public String getName() {
        return name;
    }

    public double getNatureVal(int stat) {
        if (beneficial == hindering) {
            return 1;
        } else if (beneficial == stat) {
            return 1.1;
        } else if (hindering == stat) {
            return .9;
        } else {
            return 1;
        }
    }

    public Color getColor(int statIndex) {
        if (beneficial == hindering) {
            return Color.BLACK;
        } else if (beneficial == statIndex) {
            return new Color(0, 190, 0);
        } else if (hindering == statIndex) {
            return new Color(200, 0, 0);
        } else {
            return Color.BLACK;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Nature)) {
            return false;
        }

        Nature that = (Nature)other;
        return this.getName().equals(that.getName());
    }

    private static int getRandomNatureStatIndex() {
        return RandomUtils.getRandomInt(1, Stat.NUM_STATS - 1);
    }
}
