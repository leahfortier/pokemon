package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;
import battle.effect.Status.StatusCondition;

public interface StatusPreventionEffect 
{
	public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status);
	public String statusPreventionMessage(ActivePokemon victim);
}
