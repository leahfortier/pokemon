package battle.effect;

import battle.Battle;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.ability.AbilityNamesies;

public interface SapHealthEffect {
    default double sapPercentage() {
        return .5;
    }

    default int getSapAmount(ActivePokemon victim, int damageAmount) {
        return (int)Math.ceil(damageAmount*this.sapPercentage());
    }

    default String getSapMessage(ActivePokemon victim) {
        return victim.getName() + "'s health was sapped!";
    }

    default void sapHealth(Battle b, ActivePokemon user, ActivePokemon victim, int damageAmount, boolean print) {
        int sapAmount = this.getSapAmount(victim, damageAmount);

        // Sap message
        if (print) {
            Messages.add(new MessageUpdate(this.getSapMessage(victim)));
        }

        if (victim.hasAbility(AbilityNamesies.LIQUID_OOZE)) {
            Messages.add(new MessageUpdate(victim.getName() + "'s " + AbilityNamesies.LIQUID_OOZE.getName() + " caused " + user.getName() + " to lose health instead!"));
            user.reduceHealth(b, damageAmount);
            return;
        }

        // Big Root heals an additional 30%
        if (user.isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
            sapAmount *= 1.3;
        }

        // Healers gon' heal
        if (!user.hasEffect(EffectNamesies.HEAL_BLOCK)) {
            user.heal(sapAmount);
        }

        Messages.add(new MessageUpdate().updatePokemon(b, victim));
        Messages.add(new MessageUpdate().updatePokemon(b, user));
    }
}
