package battle.effect.attack;

import pokemon.Ability;
import pokemon.ActivePokemon;
import battle.Battle;

public interface ChangeAbilityMove {
	Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim);
	String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim);
}
