package battle.effect;

import main.Type;
import pokemon.ActivePokemon;
import battle.Battle;

public interface DamageBlocker 
{
	public boolean block(Type attacking, ActivePokemon victim);
	public void alternateEffect(Battle b, ActivePokemon victim);
}
