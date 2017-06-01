package battle.ai;

import battle.Battle;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import message.Messages;
import message.Messages.MessageState;
import pokemon.ActivePokemon;
import trainer.PlayerTrainer;
import trainer.SimulatedPlayer;
import trainer.Trainer;
import trainer.TrainerAction;
import util.SerializationUtils;

import java.util.List;

public class DecisionTree {
    private static final int TREE_DEPTH = 2;

    private final Battle battle;
    private final List<Move> usable;

    private final int playerStartHealth;
    private final int opponentStartHealth;

    public DecisionTree(Battle battle, List<Move> usable) {
        this.battle = battle;
        this.usable = usable;

        playerStartHealth = battle.getPlayer().front().getHP();
        opponentStartHealth = battle.getOpponent().front().getHP();
    }

    public Move next() {
        Messages.clearMessages(MessageState.SIMULATION_STATION);
        Messages.setMessageState(MessageState.SIMULATION_STATION);

        List<Move> playerMoves = battle.getPlayer().front().getMoves(battle);
        BestMove best = go(0, battle, usable, playerMoves);

        Messages.setMessageState(MessageState.FIGHTY_FIGHT);

        return best.move;
    }

    private static class BestMove {
        private final Move move;
        private final long value;

        BestMove(Move move, long value) {
            this.move = move;
            this.value = value;
        }
    }

    private boolean isBattleDone(Battle b) {
        return b.getOpponent().front().isFainted(b) || b.getPlayer().front().isFainted(b);
    }

    private long evaluate(Battle b) {
        long points = 0;
        ActivePokemon playerPoke = b.getPlayer().front();
        ActivePokemon opponentPoke = b.getOpponent().front();

        if (playerPoke.isFainted(b)) {
            points += 1000;
        } else {
            points += 2 * (playerStartHealth - playerPoke.getHP());
        }

        if (opponentPoke.isFainted(b)) {
            points -= 500;
        } else {
            points -= opponentStartHealth - opponentPoke.getHP();
        }

        return points;
    }

    private void setupTrainer(Trainer trainer, Move move) {
        ActivePokemon front = (ActivePokemon) SerializationUtils.getSerializedCopy(trainer.front());
        trainer.replaceFront(front);

        // Need to set these manually since this field has to be transient because ActivePokemon and BattleAttributes store each other
        front.getAttributes().setAttributesHolder(front);

        trainer.setAction(TrainerAction.FIGHT);
        front.setMove(new Move(move.getAttack()));
    }

    private Battle simulateTurn(Battle b, Move opponentMove, Move playerMove) {
        // Mock the player object for serialization and set back afterwards
        PlayerTrainer playerTrainer = b.getPlayer();
        b.setPlayer(new SimulatedPlayer(b.getPlayer()));
        Battle simulated = (Battle) SerializationUtils.getSerializedCopy(b);
        b.setPlayer(playerTrainer);

        setupTrainer(simulated.getPlayer(), playerMove);
        setupTrainer((Trainer)simulated.getOpponent(), opponentMove);

        simulated.fight();

        return simulated;
    }

    private BestMove go(int level, Battle b, List<Move> opponentUsable, List<Move> playerUsable) {
        if (level == TREE_DEPTH || isBattleDone(b)) {
            return new BestMove(null, evaluate(b));
        }

        BestMove bestMove = new BestMove(opponentUsable.get(0), Integer.MIN_VALUE);
        for (Move opponentMove : opponentUsable) {
            BestMove current = new BestMove(playerUsable.get(0), Integer.MAX_VALUE);
            for (Move playerMove : playerUsable) {
                Battle simulated = simulateTurn(b, opponentMove, playerMove);
                BestMove currentBest = go(level + 1, simulated, opponentUsable, playerUsable);

                System.out.println(level + " " + opponentMove.getAttack().getName() + " " + playerMove.getAttack().getName() + " " +  currentBest.value);

                if (currentBest.value < current.value) {
                    current = new BestMove(opponentMove, currentBest.value);
                }
            }
            System.out.println();

            if (current.value > bestMove.value) {
                bestMove = new BestMove(opponentMove, current.value);
            }
        }

        System.out.println();
        System.out.println();

        return bestMove;
    }
}
