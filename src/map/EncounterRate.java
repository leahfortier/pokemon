package map;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum EncounterRate {
    VERY_COMMON(15),
    COMMON(12.75),
    SEMI_RARE(10.125),
    RARE(4.995),
    VERY_RARE(1.875);

    private double rate;

    EncounterRate(double rate) {
        this.rate = rate;
    }

    public static final String[] ENCOUNTER_RATE_NAMES =
            Arrays.stream(EncounterRate.values())
                    .map(Enum::name)
                    .collect(Collectors.toList())
                    .toArray(new String[0]);

    public double getRate() {
        return this.rate;
    }
}
