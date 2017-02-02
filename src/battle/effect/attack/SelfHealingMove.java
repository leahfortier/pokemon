package battle.effect.attack;

import battle.Battle;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;

public interface SelfHealingMove {
	double getHealFraction(Battle b, ActivePokemon victim);

	// Heal yourself!!
	default void heal(Battle b, ActivePokemon victim) {
		victim.healHealthFraction(this.getHealFraction(b, victim));

		Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
	}
}
