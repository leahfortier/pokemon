package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;
import battle.effect.generic.Status.StatusCondition;

public interface StatusPreventionEffect {
	boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status);
	String statusPreventionMessage(ActivePokemon victim);
}
