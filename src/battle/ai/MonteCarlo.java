package battle.ai;

import battle.Battle;
import battle.attack.AttackNamesies;
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
    private static final int BUDGET = 50;
    private static final int ROLLOUT_TURNS = 5;

    private static final double CONFIDENCE_BOUND_SIZE = 1;

    public Move next(Battle battle) {
        Messages.clearMessages(MessageState.SIMULATION_STATION);
        Messages.setMessageState(MessageState.SIMULATION_STATION);

        Player player = (Player)SerializationUtils.getSerializedCopy(battle.getPlayer());

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
                        if ((child.ucb > bestUtil) || (child.ucb == bestUtil && RandomUtils.chanceTest(50))) {
                            bestChild = child;
                            bestUtil = child.ucb;
                        }
                    } else {
                        if ((child.lcb < bestUtil) || (child.lcb == bestUtil && RandomUtils.chanceTest(50))) {
                            bestChild = child;
                            bestUtil = child.lcb;
                        }
                    }
                }

                current = bestChild;
                traversed.add(current);
            }

            Battle simulated = (Battle)SerializationUtils.getSerializedCopy(battle);

            ActivePokemon playerPokemon = (ActivePokemon)SerializationUtils.getSerializedCopy(battle.getPlayer().front());
            player.replaceFront(playerPokemon);
            simulated.setPlayer(player);

            Opponent opponent = simulated.getOpponent();
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

            List<Move> rolloutMoves = new ArrayList<>(current.path);
            boolean isOpp = !current.isOpp;
            for (int j = 0; j < ROLLOUT_TURNS || rolloutMoves.size()%2 != 0; j++) {
                // TODO: This should really be only the usable moves
                rolloutMoves.add(RandomUtils.getRandomValue(simulated.getTrainer(!isOpp).front().getMoves(simulated)));
                isOpp = !isOpp;
            }

            Boolean oppWon = null;
            double discountFactor = .1;
            double utility = 0;
            for (int j = 0; j < rolloutMoves.size(); j += 2) {
                player.setAction(TrainerAction.FIGHT);
                if (opponent instanceof Trainer) {
                    ((Trainer)opponent).setAction(TrainerAction.FIGHT);
                }

                simulated.getTrainer(false).front().setMove(simulated, rolloutMoves.get(j));
                simulated.getTrainer(true).front().setMove(simulated, rolloutMoves.get(j + 1));

                simulated.fight();

                double factor = opponentPokemon.getHPRatio() - playerPokemon.getHPRatio();

                System.out.println(i + " " + rolloutMoves.get(j).getAttack().getName() + " " + factor);
//                factor += 1;
//                factor /= 2;

                utility += Math.pow(discountFactor, j)*factor;

                if (playerPokemon.isFainted(simulated)) {
                    oppWon = true;
                    break;
                } else if (opponentPokemon.isFainted(simulated)) {
                    oppWon = false;
                    break;
                }
            }

            System.out.println(rolloutMoves.get(0).getAttack().getName() + " " + utility);

            if (rolloutMoves.get(0).getAttack().namesies() == AttackNamesies.QUICK_ATTACK) {
                System.out.println("QUICK ATTACK: " + root.children.get(1).visitedCount + " " + root.children.get(1).totalWins);
            }

            if (oppWon == null) {
                oppWon = opponentPokemon.getHPRatio() > playerPokemon.getHPRatio();
            }

            for (int j = 1; j < traversed.size(); j++) {
                Node prev = traversed.get(j - 1);
                Node curr = traversed.get(j);

                curr.visitedCount++;
//                if (oppWon) {
//                    curr.totalWins++;
//                }
                curr.totalWins += utility;

                double expectedUtility = (double)curr.totalWins/curr.visitedCount;
                double confidenceIntervalSize = Math.sqrt(CONFIDENCE_BOUND_SIZE*Math.log(prev.visitedCount)/curr.visitedCount);
                if (confidenceIntervalSize > 0) {
                    curr.ucb = expectedUtility + confidenceIntervalSize;
                    curr.lcb = expectedUtility - confidenceIntervalSize;
                }
            }
        }

        Move bestMove = null;
        double bestVal = -Integer.MAX_VALUE;
        for (int i = 0; i < root.children.size(); i++) {
            Node child = root.children.get(i);
            System.out.println(i + " " + child.path.get(0).getAttack().getName() + " " + child.visitedCount + " " + child.totalWins/child.visitedCount);

            if (child.totalWins/child.visitedCount >= bestVal) {
                bestMove = child.path.get(0);
                bestVal = child.totalWins/child.visitedCount;
            }
        }

        Messages.setMessageState(MessageState.FIGHTY_FIGHT);

        return bestMove;
    }

    private static class Node {
        private final boolean isOpp;
        private List<Move> path;

        private int visitedCount;
        private double totalWins;

        private double ucb;
        private double lcb;

        private List<Node> children;

        Node(List<Move> path, boolean isOpp) {
            this.path = path;
            this.isOpp = isOpp;

            this.visitedCount = 0;
            this.totalWins = 0;

            this.ucb = 1;
            this.lcb = 0;

            this.children = new ArrayList<>();
        }

        boolean hasChildren() {
            return this.children.size() > 0;
        }
    }
}
