package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface OpponentTrappingEffect 
{
	public boolean isTrapped(Battle b, ActivePokemon p);
	public String trappingMessage(ActivePokemon escaper, ActivePokemon trapper);
}
