package item.berry;

import battle.Battle;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.StatusReceivedEffect;
import battle.effect.status.StatusCondition;
import item.use.PokemonUseItem;
import pokemon.ActivePokemon;

public interface StatusBerry extends GainableEffectBerry, PokemonUseItem, StatusReceivedEffect {
    default void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition statusType) {
        if (this.gainBerryEffect(b, victim, CastSource.HELD_ITEM)) {
            victim.consumeItem(b);
        }
    }
}
