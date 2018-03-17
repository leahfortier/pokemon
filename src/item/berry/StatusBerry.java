package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.interfaces.InvokeInterfaces.EndTurnEffect;
import battle.effect.interfaces.InvokeInterfaces.StatusReceivedEffect;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import item.medicine.StatusHealer;

public interface StatusBerry extends StatusHealer, GainableEffectBerry, StatusReceivedEffect, EndTurnEffect {
    default void consumeBerry(Battle b, ActivePokemon victim) {
        if (this.gainBerryEffect(b, victim, CastSource.HELD_ITEM)) {
            this.consumeItem(b, victim);
        }
    }

    @Override
    default void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies statusType) {
        this.consumeBerry(b, victim);
    }

    @Override
    default boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
        return use(b, user, source);
    }

    @Override
    default int naturalGiftPower() {
        return 80;
    }

    @Override
    default void applyEndTurn(ActivePokemon victim, Battle b) {
        this.consumeBerry(b, victim);
    }
}
