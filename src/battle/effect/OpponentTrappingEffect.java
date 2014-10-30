package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface OpponentTrappingEffect 
{
	public boolean trapOpponent(Battle b, ActivePokemon p);
	public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper);
}
