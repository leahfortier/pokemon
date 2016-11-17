package item.hold;

import pokemon.ActivePokemon;
import battle.Battle;

public interface HoldItem {
	int flingDamage();
	default void flingEffect(Battle b, ActivePokemon pelted) {}
}
