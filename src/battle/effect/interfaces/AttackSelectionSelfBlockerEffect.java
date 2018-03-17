package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.interfaces.InvokeInterfaces.AttackSelectionEffect;
import battle.effect.interfaces.InvokeInterfaces.SelfAttackBlocker;

public interface AttackSelectionSelfBlockerEffect extends AttackSelectionEffect, SelfAttackBlocker {

    @Override
    default boolean block(Battle b, ActivePokemon user) {
        return !this.usable(b, user, user.getMove());
    }
}
