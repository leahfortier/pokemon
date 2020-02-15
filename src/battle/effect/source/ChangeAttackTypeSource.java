package battle.effect.source;

import battle.ActivePokemon;
import type.Type;

public interface ChangeAttackTypeSource {
    Type getAttackType(Type original);
    String getMessage(ActivePokemon caster, ActivePokemon victim);
}
