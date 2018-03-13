package item.berry;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.MoveCategory;
import battle.effect.CastSource;
import battle.effect.EffectInterfaces.OpponentApplyDamageEffect;
import battle.effect.EffectInterfaces.TakeDamageEffect;
import message.Messages;
import pokemon.Stat;

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

    interface CategoryIncreaseBerry extends CategoryBerry, TakeDamageEffect {
        Stat getStat();

        @Override
        default void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Increases stat by 1 when hit by a move a specified category
            if (user.getAttack().getCategory() == this.getCategory()
                    && victim.getStages().modifyStage(victim, 1, this.getStat(), b, CastSource.HELD_ITEM)) {
                this.consumeItem(b, victim);
            }
        }
    }

    interface CategoryDamageBerry extends CategoryBerry, OpponentApplyDamageEffect {
        @Override
        default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
            // If hit by a move of a specific category, the user will also be hurt
            if (user.getAttack().getCategory() == this.getCategory()) {
                Messages.add(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!");
                user.reduceHealthFraction(b, 1/8.0);
                this.consumeItem(b, victim);
            }
        }
    }
}
