package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface FaintEffect 
{
	public void deathwish(Battle b, ActivePokemon dead, ActivePokemon murderer);
}
