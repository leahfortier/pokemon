package battle.effect.attack;

import battle.Battle;
import pokemon.ActivePokemon;
import pokemon.ability.Ability;

public interface ChangeAbilityMove {
	Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim);
	String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim);
}
