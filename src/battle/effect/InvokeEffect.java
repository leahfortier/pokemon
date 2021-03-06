package battle.effect;

import battle.effect.source.CastSource;
import util.serialization.Serializable;

public interface InvokeEffect extends Serializable {
    InvokeSource getSource();

    default boolean isActiveEffect() {
        return true;
    }

    // This is separate from CastSource because USE_ITEM and CAST_SOURCE are invalid
    enum InvokeSource {
        ATTACK(CastSource.ATTACK),
        ABILITY(CastSource.ABILITY),
        ITEM(CastSource.HELD_ITEM),
        EFFECT(CastSource.EFFECT);

        private final CastSource castSource;

        InvokeSource(CastSource castSource) {
            this.castSource = castSource;
        }

        public CastSource getCastSource() {
            return this.castSource;
        }
    }
}
