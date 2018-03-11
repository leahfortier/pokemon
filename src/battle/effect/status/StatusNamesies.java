package battle.effect.status;

import java.util.function.Supplier;

public enum StatusNamesies {
    // EVERYTHING BELOW IS GENERATED ###
    NO_STATUS("", 1, NoStatus::new),
    FAINTED("FNT", 1, Fainted::new),
    PARALYZED("PRZ", 1.5, Paralyzed::new),
    POISONED("PSN", 1.5, Poisoned::new),
    BADLY_POISONED("PSN", 1.5, BadlyPoisoned::new),
    BURNED("BRN", 1.5, Burned::new),
    ASLEEP("SLP", 2.5, Asleep::new),
    FROZEN("FRZ", 2.5, Frozen::new);

    // EVERYTHING ABOVE IS GENERATED ###

    private final String name;
    private final double catchModifier;
    private final Supplier<Status> statusGetter;

    StatusNamesies(String name, double catchModifier, Supplier<Status> statusGetter) {
        this.name = name;
        this.catchModifier = catchModifier;
        this.statusGetter = statusGetter;
    }

    public String getName() {
        return name;
    }

    public double getCatchModifier() {
        return catchModifier;
    }

    public Status getStatus() {
        return this.statusGetter.get();
    }
}
