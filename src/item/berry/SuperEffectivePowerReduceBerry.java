package item.berry;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.OpponentPowerChangeEffect;
import message.Messages;
import pokemon.ActivePokemon;
import type.Type;
import type.TypeAdvantage;

public interface SuperEffectivePowerReduceBerry extends Berry, OpponentPowerChangeEffect {
    Type getType();
    
    default double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
        if (user.getAttackType() == this.getType() && TypeAdvantage.isSuperEffective(user, victim, b)) {
            Messages.add(victim.getName() + "'s " + this.getName() + " decreased " + user.getName() + "'s attack!");
            victim.consumeItem(b);
            return .5;
        }
        
        return 1;
    }
    
    default int naturalGiftPower() {
        return 80;
    }
    
    default Type naturalGiftType() {
        return this.getType();
    }
}
