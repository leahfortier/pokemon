package battle.effect;

import pokemon.Ability;
import pokemon.ActivePokemon;
import battle.Battle;

public interface ChangeAbilityMove 
{
	public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim);
	public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim);
}
