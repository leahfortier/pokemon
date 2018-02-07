package battle.attack;

import battle.ActivePokemon;
import battle.Battle;

public interface AttackInterface {
    AttackNamesies namesies();
    boolean isStatusMove();

    default void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {}
    default void endAttack(Battle b, ActivePokemon attacking, ActivePokemon defending, boolean attackHit, boolean success) {}

    default boolean shouldApplyDamage(Battle b, ActivePokemon user) {
        // Status moves default to no damage
        if (this.isStatusMove()) {
            return false;
        }

        return true;
    }

    default boolean shouldApplyEffects(Battle b, ActivePokemon user) {
        return true;
    }
}
