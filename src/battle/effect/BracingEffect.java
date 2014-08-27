package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface BracingEffect
{
	public boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth);
	public String braceMessage(ActivePokemon bracer);
}
