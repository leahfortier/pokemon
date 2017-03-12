package battle.ai;

import battle.Battle;
import battle.attack.Move;
import message.Messages;
import message.Messages.MessageState;
import pokemon.ActivePokemon;
import trainer.Opponent;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.player.Player;
import util.RandomUtils;
import util.SerializationUtils;

import java.util.ArrayList;
import java.util.List;

public class MonteCarlo {
    private static final int BUDGET = 100;
    private static final int ROLLOUT_TURNS = 5;

    // TODO: Comment this with whatever Bryan just said but didn't necessarily hear correctly
    private static final double CONFIDENCE_BOUND_SIZE = 2;

    public Move next(Battle battle) {
        Messages.clearMessages(MessageState.SIMULATION_STATION);
        Messages.setMessageState(MessageState.SIMULATION_STATION);

        Node root = new Node(new ArrayList<>(), true);
        for (int i = 0; i < BUDGET; i++) {
            Node current = root;
            current.visitedCount++;

            List<Node> traversed = new ArrayList<>();
            traversed.add(current);

            while (current.hasChildren()) {
                Node bestChild = current.children.get(0);
                double bestUtil = current.isOpp ? 0 : 1;

                for (Node child : current.children) {
                    if (current.isOpp) {
                        if (child.ucb > bestUtil) {
                            bestChild = child;
                            bestUtil = child.ucb;
                        }
                    }
                    else {
                        if (child.lcb < bestUtil) {
                            bestChild = child;
                            bestUtil = child.lcb;
                        }
                    }
                }

                current = bestChild;
                traversed.add(current);
            }

            Battle simulated = (Battle)SerializationUtils.deserialize(SerializationUtils.serialize(battle));

            Player player = simulated.getPlayer();
            Opponent opponent = simulated.getOpponent();

            ActivePokemon playerPokemon = player.front();
            ActivePokemon opponentPokemon = opponent.front();

            // Need to set these manually since this field has to be transient because ActivePokemon and BattleAttributes store each other
            playerPokemon.getAttributes().setAttributesHolder(playerPokemon);
            opponentPokemon.getAttributes().setAttributesHolder(opponentPokemon);

            for (Move move : simulated.getTrainer(!current.isOpp).front().getMoves(simulated)) {
                Node childNode = new Node(new ArrayList<>(current.path), !current.isOpp);
                childNode.path.add(move);
                current.children.add(childNode);
            }

            current = RandomUtils.getRandomValue(current.children);
            traversed.add(current);

            List<Move> playerRolloutMoves = new ArrayList<>();
            List<Move> opponentRolloutMoves = new ArrayList<>();

            (current.isOpp ? opponentRolloutMoves : playerRolloutMoves).addAll(current.path);

            for (int j = 0; j < ROLLOUT_TURNS; j++) {
                // TODO: This should really be only the usable moves
                playerRolloutMoves.add(RandomUtils.getRandomValue(playerPokemon.getMoves(simulated)));
                opponentRolloutMoves.add(RandomUtils.getRandomValue(opponentPokemon.getMoves(simulated)));
            }

            Boolean oppWon = null;
            for (int j = 0; j < playerRolloutMoves.size() && j < opponentRolloutMoves.size(); j++) {
                player.setAction(TrainerAction.FIGHT);
                if (opponent instanceof Trainer) {
                    ((Trainer) opponent).setAction(TrainerAction.FIGHT);
                }

                playerPokemon.setMove(playerRolloutMoves.get(j));
                opponentPokemon.setMove(opponentRolloutMoves.get(j));

                simulated.fight();

                if (simulated.deadUser()) {
                    oppWon = true;
                    break;
                }
                else if (simulated.deadOpponent()) {
                    oppWon = false;
                    break;
                }
            }

            if (oppWon == null) {
                oppWon = simulated.getTrainer(false).front().getHPRatio() > simulated.getTrainer(true).front().getHPRatio();
            }

            for(int j = 1; j < traversed.size(); j++) {
                Node prev = traversed.get(j - 1);
                Node curr = traversed.get(j);

                curr.visitedCount++;
                if (oppWon) {
                    curr.totalWins++;
                }

                double expectedUtility = (double)curr.totalWins/curr.visitedCount;
                double confidenceIntervalSize = Math.sqrt(CONFIDENCE_BOUND_SIZE*Math.log(prev.visitedCount)/curr.visitedCount);
                if(confidenceIntervalSize > 0) {
                    curr.ucb = expectedUtility + confidenceIntervalSize;
                    curr.lcb = expectedUtility - confidenceIntervalSize;
                }
                curr.ucb = Math.min(1, curr.ucb);
                curr.lcb = Math.max(0, curr.lcb);
            }
        }

        Move bestMove = null;
        double bestVal = 0;
        for (int i  = 0; i < root.children.size(); i++) {
            Node child = root.children.get(i);
            System.out.println(i + " " + child.path.get(0).getAttack().getName() + " " + child.totalWins + " " + child.visitedCount + " " + child.lcb);

            if (child.lcb >= bestVal) {
                bestMove = child.path.get(0);
                bestVal = child.lcb;
            }
        }

        Messages.setMessageState(MessageState.FIGHTY_FIGHT);

        return bestMove;
    }

    private static class Node {
        private List<Move> path;
        private boolean isOpp;

        private double ucb;
        private double lcb;

        int visitedCount;
        int totalWins;

        List<Node> children;

        Node(List<Move> path, boolean isOpp) {
            this.path = path;
            this.isOpp = isOpp;

            this.totalWins = 0;
            this.visitedCount = 0;

            this.ucb = 1;
            this.lcb = 0;

            this.children = new ArrayList<>();
        }

        boolean hasChildren() {
            return this.children.size() > 0;
        }
    }
}
