package battle.effect.source;

import battle.ActivePokemon;
import battle.Battle;
import pokemon.ability.Ability;

public interface ChangeAbilitySource {
    Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim);
    String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim);
}
