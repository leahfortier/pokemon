package item.hold;

import battle.Battle;
import pokemon.ActivePokemon;

public interface HoldItem {
	int flingDamage();
	default void flingEffect(Battle b, ActivePokemon pelted) {}
}
