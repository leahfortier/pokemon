package battle.effect.generic;

import battle.Battle;
import main.Global;
import pokemon.Ability;
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

		static void invokeApplyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			List<Object> invokees = b.getEffectsList(user);
			
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

	public interface EndTurnEffect {
		void applyEndTurn(ActivePokemon victim, Battle b);

		static void invokeEndTurnEffect(ActivePokemon victim, Battle b) {
			if (victim.isFainted(b)) {
				return;
			}
			
			// Weather is handled separately
			List<Object> invokees = b.getEffectsList(victim);
			invokees.remove(b.getWeather());
			
			for (Object invokee : invokees) {
				if (invokee instanceof EndTurnEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
					EndTurnEffect effect = (EndTurnEffect)invokee;
					effect.applyEndTurn(victim, b);
					
					if (victim.isFainted(b)) {
						return;
					}
				}
			}
		}
	}
}
