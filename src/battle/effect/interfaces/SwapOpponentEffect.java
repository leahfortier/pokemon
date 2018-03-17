package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.Battle;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import trainer.Team;
import trainer.Trainer;
import trainer.WildPokemon;

public interface SwapOpponentEffect {
    String getSwapMessage(ActivePokemon user, ActivePokemon victim);

    default void swapOpponent(Battle b, ActivePokemon user, ActivePokemon victim) {
        if (!user.canSwapOpponent(b, victim)) {
            return;
        }

        Messages.add(this.getSwapMessage(user, victim));

        Team opponent = b.getTrainer(victim);
        if (opponent instanceof WildPokemon) {
            // End the battle against a wild Pokemon
            Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.EXIT_BATTLE));
        } else {
            Trainer trainer = (Trainer)opponent;

            // Swap to a random Pokemon!
            trainer.switchToRandom(b);
            b.enterBattle(trainer.front(), enterer -> "...and " + enterer.getName() + " was dragged out!");
        }
    }
}
