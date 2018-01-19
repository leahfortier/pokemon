package trainer;

import battle.ActivePokemon;
import battle.Battle;

import java.io.Serializable;

public enum TrainerAction implements Serializable {
    FIGHT(Battle::getAttackPriority),
    SWITCH(6),
    ITEM(6),
    RUN(6);

    private final PriorityGetter priorityGetter;

    TrainerAction(int priority) {
        this((b, p) -> priority);
    }

    TrainerAction(PriorityGetter priorityGetter) {
        this.priorityGetter = priorityGetter;
    }

    public int getPriority(Battle b, ActivePokemon p) {
        return this.priorityGetter.getPriority(b, p);
    }

    @FunctionalInterface
    private interface PriorityGetter {
        int getPriority(Battle b, ActivePokemon p);
    }
}
