package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.Effect;
import battle.effect.InvokeInterfaces.StickyHoldEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import item.hold.HoldItem;
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
        Effect.cast(PokemonEffectNamesies.EATEN_BERRY, b, consumer, consumer, CastSource.HELD_ITEM, false);

        if (consumer.hasAbility(AbilityNamesies.CHEEK_POUCH) && consumer.canHeal()) {
            consumer.healHealthFraction(1/3.0, b, consumer.getName() + "'s " + consumer.getAbility().getName() + " restored its health!");
        }
    }

    // Okay for stealer and holder to be the same (eating your own berry)
    default void eatBerry(Battle b, ActivePokemon stealer, ActivePokemon holder) {
        // Can't steal sticky berries
        if (stealer != holder && StickyHoldEffect.containsStickyHoldEffect(b, stealer, holder)) {
            return;
        }

        String possessive = stealer == holder ? "its" : holder.getName() + "'s";
        Messages.add(stealer.getName() + " ate " + possessive + " " + this.getName() + "!");
        this.consumeItemWithoutEffects(b, holder);

        // Gain the effects if you can
        if (this instanceof GainableEffectBerry) {
            ((GainableEffectBerry)this).gainBerryEffect(b, stealer, CastSource.USE_ITEM);
        }

        // I think this is only supposed to be called if it is a gainable effect berry, but I think it makes sense for all
        this.consumeBerry(stealer, b);
    }
}
