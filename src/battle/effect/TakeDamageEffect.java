package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface TakeDamageEffect {
	// b: The current battle
	// user: The user of the attack
	// victim: The Pokemon who is taking damage, they are the one's probably implementing this
	void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim);
}
