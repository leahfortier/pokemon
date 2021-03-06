package item.hold;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.EffectReceivedEffect;
import battle.effect.InvokeInterfaces.EndTurnEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import message.Messages;

import java.util.Set;

public interface EffectCurerItem extends HoldItem, EffectReceivedEffect, EndTurnEffect {
    Set<PokemonEffectNamesies> getCurableEffects();

    default boolean usesies(ActivePokemon user) {
        boolean used = false;
        for (PokemonEffectNamesies removableEffect : this.getCurableEffects()) {
            if (user.getEffects().remove(removableEffect)) {
                Messages.add(removableEffect.getEffect().getSourceRemoveMessage(user, this.getName()));
                used = true;
            }
        }

        return used;
    }

    @Override
    default void receiveEffect(Battle b, ActivePokemon caster, ActivePokemon victim, EffectNamesies effectType) {
        if (effectType instanceof PokemonEffectNamesies && this.getCurableEffects().contains(effectType)) {
            PokemonEffectNamesies pokemonEffectType = (PokemonEffectNamesies)effectType;
            Messages.add(pokemonEffectType.getEffect().getSourceRemoveMessage(victim, this.getName()));
            victim.getEffects().remove(pokemonEffectType);
            this.consumeItem(b, victim);
        }
    }

    @Override
    default void applyEndTurn(ActivePokemon victim, Battle b) {
        if (usesies(victim)) {
            this.consumeItem(b, victim);
        }
    }
}
