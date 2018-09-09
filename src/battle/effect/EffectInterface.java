package battle.effect;

import battle.ActivePokemon;

public interface EffectInterface {
    EffectNamesies namesies();

    String getSubsideMessage(ActivePokemon victim);

    int getTurns();
    boolean isActive();
    void deactivate();
}
