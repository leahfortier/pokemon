package item.medicine;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.EffectInterfaces.MessageGetter;
import battle.effect.source.CastSource;
import item.hold.HoldItem;
import item.use.BattlePokemonUseItem;

public interface HpHealer extends MessageGetter, BattlePokemonUseItem, HoldItem {
    // This should only return the amount to heal but should NOT handle actual healing
    int getHealAmount(ActivePokemon p);

    @Override
    default String getGenericMessage(ActivePokemon p) {
        return p.getName() + "'s health was restored!";
    }

    @Override
    default String getSourceMessage(ActivePokemon p, String sourceName) {
        return p.getName() + " was healed by its " + sourceName + "!";
    }

    @Override
    default boolean use(ActivePokemon p, Battle b) {
        return this.use(b, p, CastSource.USE_ITEM);
    }

    default boolean use(Battle b, ActivePokemon p, CastSource source) {
        if (p.isActuallyDead()) {
            return false;
        }

        // Success if healed a positive amount of health
        // Only check effects for held items
        return p.heal(this.getHealAmount(p), source == CastSource.HELD_ITEM, b, this.getMessage(p, source)) > 0;
    }
}
