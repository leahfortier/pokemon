package battle.effect;

import java.util.List;

import battle.Battle;
import main.Global;
import pokemon.ActivePokemon;

public final class EffectInterfaces
{
	// Class to hold interfaces -- should not be instantiated
	private EffectInterfaces() { 
		Global.error("EffectInterfaces class cannot be instantiated.");
	}
	
	// This is used when the user applies direct damage to an opponent, and has special effects associated
	public interface ApplyDamageEffect  
	{
		// b: The current battle
		// user: The user of that attack, the one who is probably implementing this effect
		// victim: The Pokemon that received the attack
		// damage: The amount of damage that was dealt to victim by the user
		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage);
		
		public static void performApplyDamageEffect(List<Object> invokees, Battle b, ActivePokemon user, ActivePokemon victim, Integer damage) {
			
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
	
	// EVERYTHING BELOW IS GENERATED ###
}

