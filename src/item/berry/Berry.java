package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import item.hold.HoldItem;
import message.MessageUpdate;
import message.Messages;
import pokemon.ability.AbilityNamesies;
import type.Type;

public interface Berry extends HoldItem {
    Type naturalGiftType();
    int naturalGiftPower();

    default int getHarvestHours() {
        return 24;
    }

    default int getHarvestAmount() {
        return 3;
    }

    @Override
    default int flingDamage() {
        return 10;
    }

    default void consumeBerry(ActivePokemon consumer, Battle b) {
        // Eat dat berry!!
        PokemonEffectNamesies.EATEN_BERRY.getEffect().cast(b, consumer, consumer, CastSource.HELD_ITEM, false);

        if (consumer.hasAbility(AbilityNamesies.CHEEK_POUCH) && !consumer.fullHealth() && !consumer.hasEffect(PokemonEffectNamesies.HEAL_BLOCK)) {
            Messages.add(consumer.getName() + "'s " + consumer.getAbility().getName() + " restored its health!");
            consumer.healHealthFraction(1/3.0);
            Messages.add(new MessageUpdate().updatePokemon(b, consumer));
        }
    }

    default void stealBerry(Battle b, ActivePokemon stealer, ActivePokemon holder) {
        // Can't steal sticky berries
        if (holder.hasAbility(AbilityNamesies.STICKY_HOLD)) {
            return;
        }

        Messages.add(stealer.getName() + " ate " + holder.getName() + "'s " + this.getName() + "!");
        this.consumeItemWithoutEffects(b, holder);

        if (this instanceof GainableEffectBerry) {
            ((GainableEffectBerry)this).gainBerryEffect(b, stealer, CastSource.USE_ITEM);
        }

        // I think this is only supposed to be called if it is a gainable effect berry, but I think it makes sense for all
        this.consumeBerry(stealer, b);
    }
}
