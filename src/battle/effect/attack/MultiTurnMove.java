package battle.effect.attack;

import pokemon.ActivePokemon;
import battle.Battle;

public interface MultiTurnMove 
{
	public void charge(ActivePokemon user, Battle b);
	public boolean chargesFirst();
	public boolean semiInvulnerability();
}
