package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface NameChanger {
	String getNameChange();
	void setNameChange(Battle b, ActivePokemon victim);
}
