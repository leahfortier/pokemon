package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.generic.EffectInterfaces.OpponentPowerChangeEffect;
import message.Messages;
import type.Type;
import type.TypeAdvantage;

public interface TypedPowerReduceBerry extends Berry, OpponentPowerChangeEffect {
    Type getType();

    default boolean shouldReducePower(Battle b, ActivePokemon user, ActivePokemon victim) {
        return true;
    }

    @Override
    default double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
        if (user.isAttackType(this.getType()) && this.shouldReducePower(b, user, victim)) {
            Messages.add(victim.getName() + "'s " + this.getName() + " decreased " + user.getName() + "'s attack!");
            victim.consumeItem(b);
            return .5;
        }

        return 1;
    }

    @Override
    default int naturalGiftPower() {
        return 80;
    }

    @Override
    default Type naturalGiftType() {
        return this.getType();
    }

    @Override
    default int getHarvestHours() {
        return 48;
    }

    // Only reduce if the move is super-effective
    interface SuperEffectiveTypedPowerReduceBerry extends TypedPowerReduceBerry {
        @Override
        default boolean shouldReducePower(Battle b, ActivePokemon user, ActivePokemon victim) {
            return TypeAdvantage.isSuperEffective(user, victim, b);
        }
    }
}