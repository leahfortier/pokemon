package battle.effect.status;

import battle.ActivePokemon;
import battle.Battle;

public class BadlyPoisoned extends Poisoned {
    private int turns;

    // TODO: Confirm that it's okay that the type is POISONED instead of BADLY_POISONED
    public BadlyPoisoned() {
        this.turns = 1;
    }

    @Override
    public void applyEndTurn(ActivePokemon victim, Battle b) {
        super.applyEndTurn(victim, b);
        this.turns++;
    }

    // TODO: Confirm that this works -- I don't see where it is getting incremented??
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
