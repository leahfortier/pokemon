package battle.effect.status;

import java.io.Serializable;

public enum StatusCondition implements Serializable {
    NO_STATUS("", 1, NoStatus::new),
    FAINTED("FNT", 1, Fainted::new),
    PARALYZED("PRZ", 1.5, Paralyzed::new),
    POISONED("PSN", 1.5, Poisoned::new),
    BURNED("BRN", 1.5, Burned::new),
    ASLEEP("SLP", 2.5, Asleep::new),
    FROZEN("FRZ", 2.5, Frozen::new);

    private final String name;
    private final double catchModifier;
    private final GetStatus getStatus;

    StatusCondition(String name, double catchModifier, GetStatus getStatus) {
        this.name = name;
        this.catchModifier = catchModifier;
        this.getStatus = getStatus;
    }

    public String getName() {
        return name;
    }

    public double getCatchModifier() {
        return catchModifier;
    }

    public Status getStatus() {
        return this.getStatus.getStatus();
    }

    private interface GetStatus {
        Status getStatus();
    }
}
