package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;

public interface FixedDamageMove extends AttackInterface {
    int getFixedDamageAmount(ActivePokemon me, ActivePokemon o);

    @Override
    default void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
        o.reduceHealth(b, this.getFixedDamageAmount(me, o));
    }
}
