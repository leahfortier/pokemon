package trainer;

import battle.ActivePokemon;

public enum TrainerAction {
    FIGHT(ActivePokemon::getAttackPriority),
    SWITCH(6),
    ITEM(6),
    RUN(6);

    private final PriorityGetter priorityGetter;

    TrainerAction(int priority) {
        this(p -> priority);
    }

    TrainerAction(PriorityGetter priorityGetter) {
        this.priorityGetter = priorityGetter;
    }

    public int getPriority(ActivePokemon p) {
        return this.priorityGetter.getPriority(p);
    }

    @FunctionalInterface
    private interface PriorityGetter {
        int getPriority(ActivePokemon p);
    }
}
