package item.medicine;

import battle.ActivePokemon;
import battle.effect.CastSource;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.hold.HoldItem;
import item.use.PokemonUseItem;

public interface StatusHealer extends HoldItem, PokemonUseItem {
    boolean shouldHeal(StatusCondition statusCondition);

    default boolean use(ActivePokemon p, CastSource source) {
        if (!shouldHeal(p.getStatus().getType())) {
            return false;
        }

        Status.removeStatus(null, p, source);
        return true;
    }

    @Override
    default boolean use(ActivePokemon p) {
        return use(p, CastSource.USE_ITEM);
    }
}
