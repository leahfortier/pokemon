package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import battle.effect.EffectInterfaces.SapHealthEffect;
import battle.effect.InvokeInterfaces.ApplyDamageEffect;

public interface SapHealthMove extends AttackInterface, SapHealthEffect, ApplyDamageEffect {

    @Override
    default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
        this.sapHealth(b, user, victim, user.getDamageDealt(), true);
    }
}
