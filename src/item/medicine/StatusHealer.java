package item.medicine;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.hold.HoldItem;
import item.use.BattlePokemonUseItem;

public interface StatusHealer extends HoldItem, BattlePokemonUseItem {
    boolean shouldHeal(StatusCondition statusCondition);

    default boolean use(Battle b, ActivePokemon p, CastSource source) {
        if (!shouldHeal(p.getStatus().getType())) {
            return false;
        }

        Status.removeStatus(b, p, source);
        return true;
    }

    @Override
    default boolean use(ActivePokemon p, Battle b) {
        return use(b, p, CastSource.USE_ITEM);
    }
}
