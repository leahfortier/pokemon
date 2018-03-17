package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.interfaces.InvokeInterfaces.ApplyDamageEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import item.ItemNamesies;
import message.MessageUpdate;
import message.Messages;
import pokemon.ability.AbilityNamesies;

public interface SapHealthEffect extends ApplyDamageEffect {

    default double sapPercentage() {
        return .5;
    }

    default String getSapMessage(ActivePokemon victim) {
        return victim.getName() + "'s health was sapped!";
    }

    default void sapHealth(Battle b, ActivePokemon user, ActivePokemon victim, int damageAmount, boolean print) {
        int sapAmount = (int)Math.ceil(damageAmount*this.sapPercentage());

        // Sap message
        if (print) {
            Messages.add(this.getSapMessage(victim));
        }

        // Big Root heals an additional 30%
        if (user.isHoldingItem(b, ItemNamesies.BIG_ROOT)) {
            sapAmount *= 1.3;
        }

        if (victim.hasAbility(AbilityNamesies.LIQUID_OOZE)) {
            Messages.add(victim.getName() + "'s " + AbilityNamesies.LIQUID_OOZE.getName() + " caused " + user.getName() + " to lose health instead!");
            user.reduceHealth(b, sapAmount, false);
            return;
        }

        // Healers gon' heal
        if (!user.hasEffect(PokemonEffectNamesies.HEAL_BLOCK)) {
            user.heal(sapAmount);
        }

        Messages.add(new MessageUpdate().updatePokemon(b, victim));
        Messages.add(new MessageUpdate().updatePokemon(b, user));
    }

    @Override
    default void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
        this.sapHealth(b, user, victim, damage, true);
    }
}
