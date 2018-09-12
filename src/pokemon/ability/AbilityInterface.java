package pokemon.ability;

import battle.effect.InvokeEffect;
import battle.effect.holder.AbilityHolder;

public interface AbilityInterface extends AbilityHolder, InvokeEffect {
    AbilityNamesies namesies();

    @Override
    default InvokeSource getSource() {
        return InvokeSource.ABILITY;
    }
}
