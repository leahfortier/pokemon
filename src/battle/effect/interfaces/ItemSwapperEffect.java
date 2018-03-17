package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import item.hold.HoldItem;
import message.Messages;

public interface ItemSwapperEffect {
    String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem);

    default void swapItems(Battle b, ActivePokemon user, ActivePokemon victim) {
        HoldItem userItem = user.getHeldItem(b);
        HoldItem victimItem = victim.getHeldItem(b);

        Messages.add(this.getSwitchMessage(user, userItem, victim, victimItem));

        // For wild battles, an actual switch occurs
        if (b.isWildBattle()) {
            user.giveItem(victimItem);
            victim.giveItem(userItem);
        } else {
            user.setCastSource(victimItem);
            PokemonEffectNamesies.CHANGE_ITEM.getEffect().apply(b, user, user, CastSource.CAST_SOURCE, false);

            user.setCastSource(userItem);
            PokemonEffectNamesies.CHANGE_ITEM.getEffect().apply(b, user, victim, CastSource.CAST_SOURCE, false);
        }
    }
}
