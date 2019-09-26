package pokemon.ability;

import battle.effect.EffectInterfaces.AbilityHolder;
import battle.effect.InvokeEffect;

public interface AbilityInterface extends AbilityHolder, InvokeEffect {
    AbilityNamesies namesies();
    String getName();

    @Override
    default InvokeSource getSource() {
        return InvokeSource.ABILITY;
    }
}
