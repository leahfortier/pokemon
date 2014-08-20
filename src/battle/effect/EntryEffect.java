package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface EntryEffect 
{
	public void enter(Battle b, ActivePokemon victim);
}
