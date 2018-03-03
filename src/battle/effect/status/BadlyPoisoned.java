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

    @Override
    public int getTurns() {
        return this.turns;
    }

    @Override
    public String getGenericCastMessage(ActivePokemon p) {
        return p.getName() + " was badly poisoned!";
    }

    @Override
    public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
        return sourcerer.getName() + "'s " + sourceName + " badly poisoned " + victim.getName() + "!";
    }
}
