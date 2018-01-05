package item.berry;

import battle.Battle;
import battle.effect.generic.CastSource;
import pokemon.ActivePokemon;

public interface GainableEffectBerry extends Berry {
    boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source);
    
    default void flingEffect(Battle b, ActivePokemon pelted) {
        gainBerryEffect(b, pelted, CastSource.USE_ITEM);
    }
}
