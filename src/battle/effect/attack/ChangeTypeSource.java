package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import type.Type;

import java.io.Serializable;

public interface ChangeTypeSource extends Serializable {
    Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim);
}
