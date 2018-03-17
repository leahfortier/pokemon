package battle.effect.source;

import battle.ActivePokemon;
import battle.Battle;
import type.PokeType;
import util.serialization.Serializable;

public interface ChangeTypeSource extends Serializable {
    PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim);
}
