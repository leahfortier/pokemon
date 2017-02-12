package item.berry;

import battle.Battle;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.StatusReceivedEffect;
import battle.effect.status.StatusCondition;
import item.medicine.StatusHealer;
import pokemon.ActivePokemon;

public interface StatusBerry extends StatusHealer, GainableEffectBerry, StatusReceivedEffect {
    default void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType) {
        if (this.gainBerryEffect(b, victim, CastSource.HELD_ITEM)) {
            victim.consumeItem(b);
        }
    }

    default boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
        return use(user, source);
    }

    default int naturalGiftPower() {
        return 80;
    }
}
