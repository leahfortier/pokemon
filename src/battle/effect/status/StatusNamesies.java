package battle.effect.status;

import battle.effect.status.StatusCondition.Asleep;
import battle.effect.status.StatusCondition.BadlyPoisoned;
import battle.effect.status.StatusCondition.Burned;
import battle.effect.status.StatusCondition.Fainted;
import battle.effect.status.StatusCondition.Frozen;
import battle.effect.status.StatusCondition.NoStatus;
import battle.effect.status.StatusCondition.Paralyzed;
import battle.effect.status.StatusCondition.Poisoned;

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

    private final Supplier<StatusCondition> statusGetter;

    StatusNamesies(Supplier<StatusCondition> statusGetter) {
        this.statusGetter = statusGetter;
    }

    public StatusCondition getStatus() {
        return this.statusGetter.get();
    }
}
