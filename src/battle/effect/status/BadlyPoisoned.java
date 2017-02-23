package battle.effect.status;

import pokemon.ActivePokemon;

public class BadlyPoisoned extends Poisoned {
    private int turns;

    public BadlyPoisoned() {
        this.turns = 1;
    }

    @Override
    public boolean isType(StatusCondition statusCondition) {
        return statusCondition == StatusCondition.POISONED || statusCondition == StatusCondition.BADLY_POISONED;
    }

    @Override
    protected int getTurns() {
        return this.turns;
    }

    @Override
    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " was badly poisoned!";
    }

    @Override
    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + " badly poisoned " + victim.getName() + "!";
    }
}
