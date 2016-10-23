package battle.effect.attack;

import main.Type;
import pokemon.ActivePokemon;
import battle.Battle;

public interface ChangeTypeMove {
	Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim);
}
