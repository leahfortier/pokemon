package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface OpponentTrappingEffect 
{
	public boolean trapOpponent(Battle b, ActivePokemon p);
	public String trappingMessage(ActivePokemon escaper, ActivePokemon trapper);
}
