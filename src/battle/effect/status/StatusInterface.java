package battle.effect.status;

import battle.effect.InvokeEffect;

public interface StatusInterface extends InvokeEffect {
    StatusNamesies namesies();

    @Override
    default InvokeSource getSource() {
        return InvokeSource.EFFECT;
    }
}
