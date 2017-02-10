package item.hold;

import battle.effect.generic.CastSource;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import util.StringUtils;

public interface MessageGetter {
    String getSuccessMessage(ActivePokemon p);
    String getHoldSuccessMessage(ActivePokemon p);

    default String getMessage(ActivePokemon p, CastSource source) {
        switch (source) {
            case USE_ITEM:
                return this.getSuccessMessage(p);
            case HELD_ITEM:
                return this.getHoldSuccessMessage(p);
            default:
                Global.error("Invalid item source " + source);
                return StringUtils.empty();
        }
    }

    default void addMessage(ActivePokemon p, CastSource source) {
        Messages.add(new MessageUpdate(this.getMessage(p, source)));
    }
}
