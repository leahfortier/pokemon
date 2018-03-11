package battle.effect.status;

import battle.effect.status.Status.Asleep;
import battle.effect.status.Status.BadlyPoisoned;
import battle.effect.status.Status.Burned;
import battle.effect.status.Status.Fainted;
import battle.effect.status.Status.Frozen;
import battle.effect.status.Status.NoStatus;
import battle.effect.status.Status.Paralyzed;
import battle.effect.status.Status.Poisoned;

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

