package item.berry;

import battle.Battle;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectNamesies;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.ability.AbilityNamesies;
import util.StringUtils;

public interface GainableEffectBerry extends Berry {
	String getSuccessMessage(ActivePokemon p);
	String getHoldSuccessMessage(Battle b, ActivePokemon p);
	boolean beginGainBerryEffect(Battle b, ActivePokemon user, CastSource source);

	default boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
		if (!this.beginGainBerryEffect(b, user, source)) {
			return false;
		}

		String message = StringUtils.empty();
		switch (source) {
			case USE_ITEM:
				message = this.getSuccessMessage(user);
				break;
			case HELD_ITEM:
				message = this.getHoldSuccessMessage(b, user);
				break;
			default:
				Global.error("Use item and held item are the only valid cast sources for berries.");
		}

		Messages.add(new MessageUpdate(message).updatePokemon(b, user));

		if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
			Messages.add(new MessageUpdate(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!"));
			user.healHealthFraction(1/3.0);
			Messages.add(new MessageUpdate().updatePokemon(b, user));
		}

		// Eat dat berry!!
		EffectNamesies.EATEN_BERRY.getEffect().cast(b, user, user, source, false);

		return true;
	}
}
