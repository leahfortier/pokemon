package battle.effect.attack;

import battle.Battle;
import main.Type;
import pokemon.ActivePokemon;

public interface ChangeTypeSource {
	Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim);
}
