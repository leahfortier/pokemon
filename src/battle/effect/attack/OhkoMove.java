package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import message.Messages;

// One hit knock out
public interface OhkoMove extends AttackInterface {
    default int baseAccuracy(Battle b, ActivePokemon user) {
        return this.getBaseAccuracy();
    }

    @Override
    default void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
        // Certain death
        o.reduceHealth(b, o.getHP());
        Messages.add("It's a One-Hit KO!");
    }

    @Override
    default boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
        return user.getLevel() >= victim.getLevel();
    }

    @Override
    default int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
        return this.baseAccuracy(b, me) + (me.getLevel() - o.getLevel());
    }
}
