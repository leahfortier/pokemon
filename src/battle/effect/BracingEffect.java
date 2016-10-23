package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface BracingEffect {
	boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth);
	String braceMessage(ActivePokemon bracer);
}
