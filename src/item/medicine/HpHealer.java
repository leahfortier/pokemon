package item.medicine;

import battle.effect.MessageGetter;
import battle.effect.generic.CastSource;
import item.hold.HoldItem;
import item.use.PokemonUseItem;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;

public interface HpHealer extends MessageGetter, PokemonUseItem, HoldItem {
    int getAmountHealed(ActivePokemon p);

    default String getGenericMessage(ActivePokemon p) {
        return p.getName() + "'s health was restored!";
    }

    default String getSourceMessage(ActivePokemon p, String sourceName) {
        return p.getName() + " was healed by its " + this.getName() + "!";
    }

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

        Messages.add(new MessageUpdate(this.getMessage(p, source)).withHp(p.getHP(), p.isPlayer()));

        return true;
    }
}
