package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import type.PokeType;
import util.Serializable;

public interface ChangeTypeSource extends Serializable {
    PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim);
}
