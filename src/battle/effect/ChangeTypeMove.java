package battle.effect;

import main.Type;
import pokemon.ActivePokemon;
import battle.Battle;

public interface ChangeTypeMove 
{
	public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim);
}
