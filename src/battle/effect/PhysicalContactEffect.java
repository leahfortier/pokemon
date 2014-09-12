package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface PhysicalContactEffect 
{
	// b: The current battle
	// user: The user of the attack that caused the physical contact
	// victim: The Pokemon that received the physical contact attack, probably the one who is implementing this effect
	public void contact(Battle b, ActivePokemon user, ActivePokemon victim);
}
