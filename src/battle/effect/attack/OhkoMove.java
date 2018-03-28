package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import message.Messages;

// One hit knock out
public interface OhkoMove extends AttackInterface {
    @Override
    default void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
        // Certain death
        o.reduceHealth(b, o.getHP());
        Messages.add("It's a One-Hit KO!");
    }
}
