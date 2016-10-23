package item.hold;

import pokemon.ActivePokemon;
import battle.Battle;

public interface HoldItem {
	int flingDamage();
	void flingEffect(Battle b, ActivePokemon pelted); // TODO: Make this default to empty
}
