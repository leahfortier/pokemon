package item.medicine;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import battle.effect.MessageGetter;
import item.hold.HoldItem;
import item.use.BattlePokemonUseItem;
import message.MessageUpdate;
import message.Messages;

public interface HpHealer extends MessageGetter, BattlePokemonUseItem, HoldItem {
    int getAmountHealed(ActivePokemon p);

    @Override
    default String getGenericMessage(ActivePokemon p) {
        return p.getName() + "'s health was restored!";
    }

    @Override
    default String getSourceMessage(ActivePokemon p, String sourceName) {
        return p.getName() + " was healed by its " + sourceName + "!";
    }

    @Override
    default boolean use(ActivePokemon p, Battle b) {
        return this.use(b, p, CastSource.USE_ITEM);
    }

    default boolean use(Battle b, ActivePokemon p, CastSource source) {
        if (p.isActuallyDead() || p.fullHealth()) {
            return false;
        }

        int amountHealed = this.getAmountHealed(p);
        if (amountHealed == 0) {
            return false;
        }

        Messages.add(new MessageUpdate(this.getMessage(b, p, source)).withPokemon(p));
        return true;
    }
}
