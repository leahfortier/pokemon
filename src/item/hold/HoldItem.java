package item.hold;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.Effect;
import battle.effect.holder.ItemHolder;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import item.ItemInterface;
import item.berry.Berry;
import message.Messages;
import pokemon.ability.AbilityNamesies;

public interface HoldItem extends ItemInterface, ItemHolder {
    @Override
    default HoldItem getItem() {
        return this;
    }

    default void consumeItemWithoutEffects(Battle b, ActivePokemon holder) {
        Effect.cast(PokemonEffectNamesies.CONSUMED_ITEM, b, holder, holder, CastSource.HELD_ITEM, false);
    }

    default void consumeItem(Battle b, ActivePokemon holder) {
        this.consumeItemWithoutEffects(b, holder);

        if (this instanceof Berry) {
            ((Berry)this).consumeBerry(holder, b);
        }

        ActivePokemon other = b.getOtherPokemon(holder);
        if (other.hasAbility(AbilityNamesies.PICKUP) && !other.isHoldingItem(b)) {
            other.giveItem(this);
            Messages.add(other.getName() + " picked up " + getName() + "'s " + this.getName() + "!");
        }
    }

    default int flingDamage() {
        return 30;
    }

    default void flingEffect(Battle b, ActivePokemon pelted) {}
}
