package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import message.MessageUpdate;
import message.Messages;

public interface SelfHealingMove extends AttackInterface {
    double getHealFraction(Battle b, ActivePokemon victim);

    default void heal(Battle b, ActivePokemon victim) {
        // Heal yourself!
        victim.healHealthFraction(this.getHealFraction(b, victim));

        // TODO: Make sure the message is set up correctly for the hp change
        Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
    }
}
