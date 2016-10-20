package battle.effect.holder;

import main.Type;
import pokemon.ActivePokemon;
import battle.Battle;

public interface TypeHolder {
	public Type[] getType(Battle b, ActivePokemon p, Boolean display);
}
