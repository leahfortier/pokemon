package battle.effect;

import main.Type;
import pokemon.ActivePokemon;
import battle.Battle;

public interface TypeCondition 
{
	public Type[] getType(Battle b, ActivePokemon p, Boolean display);
}
