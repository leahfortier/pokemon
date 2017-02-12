package item.hold;

import battle.Battle;
import item.ItemInterface;
import pokemon.ActivePokemon;

public interface HoldItem extends ItemInterface {
	int flingDamage();
	default void flingEffect(Battle b, ActivePokemon pelted) {}
}
