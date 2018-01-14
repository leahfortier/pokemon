package item.berry;

import battle.Battle;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.StatusReceivedEffect;
import battle.effect.status.StatusCondition;
import item.medicine.StatusHealer;
import battle.ActivePokemon;

public interface StatusBerry extends StatusHealer, GainableEffectBerry, StatusReceivedEffect {
    @Override
    default void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType) {
        if (this.gainBerryEffect(b, victim, CastSource.HELD_ITEM)) {
            victim.consumeItem(b);
        }
    }

    @Override
    default boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
        return use(user, source);
    }

    @Override
    default int naturalGiftPower() {
        return 80;
    }
}
