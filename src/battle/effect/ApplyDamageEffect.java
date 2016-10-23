package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

// This is used when the user applies direct damage to an opponent, and has special effects associated
public interface ApplyDamageEffect {
	// b: The current battle
	// user: The user of that attack, the one who is probably implementing this effect
	// victim: The Pokemon that received the attack
	// damage: The amount of damage that was dealt to victim by the user
	void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, Integer damage);
}
