package battle.effect;

import pokemon.ActivePokemon;
import battle.Battle;

public interface BeforeTurnEffect {
	boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b);
}
