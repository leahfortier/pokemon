package battle.effect;

import battle.ActivePokemon;

public interface EffectInterface extends InvokeEffect {
    EffectNamesies namesies();

    String getSubsideMessage(ActivePokemon victim);

    int getTurns();
    boolean isActive();
    void deactivate();

    @Override
    default InvokeSource getSource() {
        return InvokeSource.EFFECT;
    }
}
