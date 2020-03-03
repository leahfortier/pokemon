package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.MoveCategory;
import battle.effect.InvokeInterfaces.OpponentApplyDamageEffect;
import battle.effect.InvokeInterfaces.TakeDamageEffect;
import battle.effect.source.CastSource;
import battle.stages.StageModifier;
import pokemon.stat.Stat;

public interface CategoryBerry extends Berry {
    MoveCategory getCategory();

    @Override
    default int naturalGiftPower() {
        return 100;
    }

    @Override
    default int getHarvestHours() {
        return 72;
    }

    default boolean isCategory(ActivePokemon user) {
        return user.getAttack().getCategory() == this.getCategory();
    }

    interface CategoryIncreaseBerry extends CategoryBerry, TakeDamageEffect {
        Stat getStat();

        @Override
        default void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (!this.isCategory(user)) {
                return;
            }

            // Increases stat by 1 (2 with Ripen) when hit by a move a specified category
            if (new StageModifier(this.ripen(victim), this.getStat()).modify(b, victim, victim, CastSource.HELD_ITEM)) {
                this.consumeItem(b, victim);
            }
        }
    }

    interface CategoryDamageBerry extends CategoryBerry, OpponentApplyDamageEffect {
        @Override
        default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
            // If hit by a move of a specific category, the user will also be hurt
            String message = user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!";
            if (this.isCategory(user) && user.reduceHealthFraction(b, this.ripen(victim)/8.0, message) > 0) {
                this.consumeItem(b, victim);
            }
        }
    }
}
