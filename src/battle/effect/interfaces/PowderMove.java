package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import battle.effect.interfaces.InvokeInterfaces.SelfAttackBlocker;
import type.Type;

public interface PowderMove extends AttackInterface, SelfAttackBlocker {

    @Override
    default boolean block(Battle b, ActivePokemon user) {
        // Powder moves don't work against Grass-type Pokemon
        return b.getOtherPokemon(user).isType(b, Type.GRASS);
    }
}
