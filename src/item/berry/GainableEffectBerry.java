package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;

public interface GainableEffectBerry extends Berry {
    boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source);

    @Override
    default void flingEffect(Battle b, ActivePokemon pelted) {
        gainBerryEffect(b, pelted, CastSource.USE_ITEM);
    }
}
