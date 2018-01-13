package pokemon;

import java.io.Serializable;

public enum GrowthRate implements Serializable {
    FAST("Fast", level -> (int)(.8*Math.pow(level, 3))),
    MEDIUM_FAST("Medium Fast", level -> (int)Math.pow(level, 3)),
    MEDIUM_SLOW("Medium Slow", level -> (int)(1.2*Math.pow(level, 3) - 15*level*level + 100*level - 140)),
    SLOW("Slow", level -> (int)(1.25*Math.pow(level, 3))),
    ERRATIC("Erratic", level -> {
        if (level <= 50) {
            return (int)(.02*Math.pow(level, 3)*(100 - level));
        } else if (level <= 68) {
            return (int)(.01*Math.pow(level, 3)*(150 - level));
        } else if (level <= 98) {
            return (int)(Math.pow(level, 3)*((1911 - 10*level)/3.0));
        } else {
            return (int)(.01*Math.pow(level, 3)*(160 - level));
        }
    }),
    FLUCTUATING("Fluctuating", level -> {
        if (level <= 15) {
            return (int)(.02*Math.pow(level, 3)*(((level + 1)/3.0) + 24));
        } else if (level <= 36) {
            return (int)(.02*Math.pow(level, 3)*(level + 14));
        } else {
            return (int)(.02*Math.pow(level, 3)*(level/2.0 + 32));
        }
    });

    private final String name;
    private final ExperienceGetter expGetter;

    GrowthRate(String name, ExperienceGetter expGetter) {
        this.name = name;
        this.expGetter = expGetter;
    }

    public int getEXP(int level) {
        return Math.max(0, this.expGetter.getEXP(level));
    }

    @FunctionalInterface
    private interface ExperienceGetter {
        int getEXP(int level);
    }
}
