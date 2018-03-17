package battle.effect.source;

import battle.ActivePokemon;
import battle.Battle;
import type.Type;

public interface ChangeAttackTypeSource {
    Type getAttackType(Type original);
    String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim);
}
