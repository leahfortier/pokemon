package item.hold;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import message.Messages;
import pokemon.ActivePokemon;
import type.Type;

public interface SpecialTypeItem extends HoldItem {
    Type getType();

    interface DriveItem extends SpecialTypeItem {
        @Override
        default int flingDamage() {
            return 70;
        }
    }

    interface MemoryItem extends SpecialTypeItem {
        @Override
        default int flingDamage() {
            return 70;
        }
    }

    interface GemItem extends SpecialTypeItem, ConsumableItem, PowerChangeEffect {
        @Override
        default double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(getType())) {
                // Consume the item
                Messages.add(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
                user.consumeItem(b);

                // Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
                return 1.5;
            }

            return 1;
        }
    }

    interface PlateItem extends SpecialTypeItem, PowerChangeEffect {
        @Override
        default double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isAttackType(getType()) ? 1.2 : 1;
        }

        @Override
        default int flingDamage() {
            return 90;
        }
    }
}
