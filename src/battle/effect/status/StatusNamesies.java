package battle.effect.status;

import java.util.function.Supplier;

public enum StatusNamesies {
    // EVERYTHING BELOW IS GENERATED ###
    NO_STATUS(NoStatus::new),
    FAINTED(Fainted::new),
    PARALYZED(Paralyzed::new),
    POISONED(Poisoned::new),
    BADLY_POISONED(BadlyPoisoned::new),
    BURNED(Burned::new),
    ASLEEP(Asleep::new),
    FROZEN(Frozen::new);

    // EVERYTHING ABOVE IS GENERATED ###

    private final Supplier<Status> statusGetter;

    StatusNamesies(Supplier<Status> statusGetter) {
        this.statusGetter = statusGetter;
    }

    public Status getStatus() {
        return this.statusGetter.get();
    }
}

