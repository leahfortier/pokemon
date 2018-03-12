package item.medicine;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import item.hold.HoldItem;
import item.use.BattlePokemonUseItem;

public interface StatusHealer extends HoldItem, BattlePokemonUseItem {
    boolean shouldHeal(ActivePokemon p);

    default boolean use(Battle b, ActivePokemon p, CastSource source) {
        if (!shouldHeal(p)) {
            return false;
        }

        p.removeStatus(b, source);
        return true;
    }

    @Override
    default boolean use(ActivePokemon p, Battle b) {
        return use(b, p, CastSource.USE_ITEM);
    }
}
