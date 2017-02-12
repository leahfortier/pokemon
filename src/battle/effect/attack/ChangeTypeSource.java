package battle.effect.attack;

import battle.Battle;
import pokemon.ActivePokemon;
import type.Type;

public interface ChangeTypeSource {
	Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim);
}
