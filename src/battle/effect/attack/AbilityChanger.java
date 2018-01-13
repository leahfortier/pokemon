package battle.effect.attack;

import battle.Battle;
import battle.ActivePokemon;
import pokemon.ability.Ability;

public interface AbilityChanger {
    Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim);
    String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim);
}
