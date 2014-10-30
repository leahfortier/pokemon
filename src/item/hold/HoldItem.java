package item.hold;

import pokemon.ActivePokemon;
import battle.Battle;

public interface HoldItem 
{
	public int flingDamage();
	public void flingEffect(Battle b, ActivePokemon pelted);
}
