package battle.effect;

import battle.ActivePokemon;
import battle.Battle;
import message.Messages;

import java.io.Serializable;

public interface MessageGetter extends Serializable {
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
        Messages.add(this.getMessage(p, source));
    }
}
