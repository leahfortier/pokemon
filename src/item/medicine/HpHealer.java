package item.medicine;

import battle.ActivePokemon;
import battle.effect.MessageGetter;
import battle.effect.generic.CastSource;
import item.hold.HoldItem;
import item.use.PokemonUseItem;
import message.MessageUpdate;
import message.Messages;

public interface HpHealer extends MessageGetter, PokemonUseItem, HoldItem {
    int getAmountHealed(ActivePokemon p);

    @Override
    default String getGenericMessage(ActivePokemon p) {
        return p.getName() + "'s health was restored!";
    }

    @Override
    default String getSourceMessage(ActivePokemon p, String sourceName) {
        return p.getName() + " was healed by its " + this.getName() + "!";
    }

    @Override
    default boolean use(ActivePokemon p) {
        return this.use(p, CastSource.USE_ITEM);
    }

    default boolean use(ActivePokemon p, CastSource source) {
        if (p.isActuallyDead() || p.fullHealth()) {
            return false;
        }

        int amountHealed = this.getAmountHealed(p);
        if (amountHealed == 0) {
            return false;
        }

        Messages.add(new MessageUpdate(this.getMessage(p, source)).withPokemon(p));

        return true;
    }
}
