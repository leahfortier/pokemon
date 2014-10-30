package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface NameChanger
{
	public String getNameChange();
	public void setNameChange(Battle b, ActivePokemon victim);
}
