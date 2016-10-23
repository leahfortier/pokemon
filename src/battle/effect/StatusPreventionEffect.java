package battle.effect;

import battle.effect.status.StatusCondition;
import pokemon.ActivePokemon;
import battle.Battle;

public interface StatusPreventionEffect {
	boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status);
	String statusPreventionMessage(ActivePokemon victim);
}
