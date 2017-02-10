package battle.effect;

import battle.Battle;
import battle.effect.generic.CastSource;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;

public interface MessageGetter {
    String getGenericMessage(ActivePokemon p);
    String getSourceMessage(ActivePokemon p, String sourceName);

    default String getMessage(ActivePokemon p, CastSource source) {
        return this.getMessage(null, p, source);
    }

    default String getMessage(Battle b, ActivePokemon p, CastSource source) {
        if (source.hasSourceName()) {
            return this.getSourceMessage(p, source.getSourceName(b, p));
        } else {
            return this.getGenericMessage(p);
        }
    }

    default void addMessage(ActivePokemon p, CastSource source) {
        Messages.add(new MessageUpdate(this.getMessage(p, source)));
    }
}
