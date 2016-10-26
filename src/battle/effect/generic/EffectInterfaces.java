package battle.effect.generic;

import battle.Battle;
import main.Global;
import pokemon.ActivePokemon;

import java.util.List;

public final class EffectInterfaces {

	// Class to hold interfaces -- should not be instantiated
	private EffectInterfaces() {
		Global.error("EffectInterfaces class cannot be instantiated.");
	}

	// EVERYTHING BELOW IS GENERATED ###

	// This is used when the user applies direct damage to an opponent, and has special effects associated
	public interface ApplyDamageEffect {

		// b: The current battle
		// user: The user of that attack, the one who is probably implementing this effect
		// victim: The Pokemon that received the attack
		// damage: The amount of damage that was dealt to victim by the user
		void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage);

		static void invokeApplyDamageEffect(List<Object> invokees, Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			
			for (Object invokee : invokees) {
				if (invokee instanceof ApplyDamageEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
					ApplyDamageEffect effect = (ApplyDamageEffect)invokee;
					effect.applyDamageEffect(b, user, victim, damage);
				}
			}
		}
	}
}
