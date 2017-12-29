package battle.effect.attack;

import battle.Battle;
import pokemon.ActivePokemon;
import type.Type;

public interface ChangeAttackTypeSource {
    Type getAttackType(Type original);
    String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim);
}
