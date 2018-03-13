package item.hold;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import battle.effect.pokemon.PokemonEffectNamesies;
import item.ItemInterface;
import item.berry.Berry;
import message.Messages;
import pokemon.ability.AbilityNamesies;

public interface HoldItem extends ItemInterface {
    default void consumeItemWithoutEffects(Battle b, ActivePokemon holder) {
        PokemonEffectNamesies.CONSUMED_ITEM.getEffect().cast(b, holder, holder, CastSource.HELD_ITEM, false);
    }

    default void consumeItem(Battle b, ActivePokemon holder) {
        this.consumeItemWithoutEffects(b, holder);

        if (this instanceof Berry) {
            holder.consumeBerry((Berry)this, b);
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
