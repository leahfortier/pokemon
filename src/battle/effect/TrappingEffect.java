package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

// Any effect that implements this will prevent a Pokemon with said effect from escaping battle
public interface TrappingEffect {
	boolean isTrapped(Battle b, ActivePokemon p);
	String trappingMessage(ActivePokemon trapped);
}
