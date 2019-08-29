package pokemon.ability;

import battle.effect.InvokeEffect;
import battle.effect.EffectInterfaces.AbilityHolder;

public interface AbilityInterface extends AbilityHolder, InvokeEffect {
    AbilityNamesies namesies();
    String getName();

    @Override
    default InvokeSource getSource() {
        return InvokeSource.ABILITY;
    }
}
