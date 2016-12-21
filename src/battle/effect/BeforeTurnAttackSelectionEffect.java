package battle.effect;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.AttackSelectionEffect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;

public interface BeforeTurnAttackSelectionEffect extends AttackSelectionEffect, BeforeTurnEffect {
    String getFailMessage(Battle b, ActivePokemon p, ActivePokemon opp);

    @Override
    default boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
        return checkUsable(b, p, opp);
    }

    default boolean checkUsable(Battle b, ActivePokemon p, ActivePokemon opp) {
        if (!usable(b, p, p.getMove())) {
            b.printAttacking(p);
            Messages.add(new MessageUpdate(this.getFailMessage(b, p, opp)));
            return false;
        }

        return true;
    }
}
