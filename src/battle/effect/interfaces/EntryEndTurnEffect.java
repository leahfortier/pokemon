package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.interfaces.InvokeInterfaces.EndTurnEffect;
import battle.effect.interfaces.InvokeInterfaces.EntryEffect;

public interface EntryEndTurnEffect extends EntryEffect, EndTurnEffect {
    void applyEffect(Battle b, ActivePokemon p);

    @Override
    default void applyEndTurn(ActivePokemon victim, Battle b) {
        applyEffect(b, victim);
    }

    @Override
    default void enter(Battle b, ActivePokemon enterer) {
        applyEffect(b, enterer);
    }
}
