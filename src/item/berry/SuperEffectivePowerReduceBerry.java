package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.generic.EffectInterfaces.OpponentPowerChangeEffect;
import message.Messages;
import type.Type;
import type.TypeAdvantage;

public interface SuperEffectivePowerReduceBerry extends Berry, OpponentPowerChangeEffect {
    Type getType();

    @Override
    default double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
        if (user.isAttackType(this.getType()) && TypeAdvantage.isSuperEffective(user, victim, b)) {
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
}
