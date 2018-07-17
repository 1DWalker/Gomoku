package gomoku.ai.mcts;

import gomoku.ai.AI;
import gomoku.action.Action;
import gomoku.state.GomokuFunctions;
import gomoku.state.State;

import java.util.List;
import java.util.Random;

public class MCTS implements AI {

    private final int N = 10000; // number of simulations

    private static final Random random = new Random();

    private int iteration;

    private Node rootNode;
    private GomokuFunctions rootState;

    // Used when traversing the tree
    private Node currentNode;
    private GomokuFunctions currentState;

    // Time
    long timeStart, timeEnd;

    @Override
    public void reset() {
        rootNode = new Node();
        rootState = new GomokuFunctions();
    }

    @Override
    public Action makeMove(State gameState, Action opponentAction) {
        if (opponentAction != null) {
            rootState.makeMove(opponentAction);
            rootNode = rootNode.nextNode(opponentAction);
//            System.out.println(rootNode.visits);
            rootNode.parent = null;
        }

        iteration = 0;
        timeStart = System.currentTimeMillis();
        while (timeRemaining()) {
            iterate();
        }

        System.out.println("MCTS Speed: " + (double) iteration / (timeEnd - timeStart) * 1000);

        ActionNodePair pair = rootNode.bestMove();
        rootState.makeMove(pair.action);
        rootNode = pair.node;
        return pair.action;
    }

    private boolean timeRemaining() {
        iteration++;

        timeEnd = System.currentTimeMillis();
//        return timeEnd - timeStart < 1000;
        return iteration != N;
    }

    private void iterate() {
        currentNode = rootNode;
        currentState = rootState.copy();

        treePolicy();

        // Get the score of a simulation in the perspective of the player at the leaf node (higher number means better)
        boolean leafPlayer = !currentState.isFirstPlayer();
        int score = simulate(currentState);
        if (!leafPlayer) score = -score;

        backpropagate(currentNode, score);
    }

    private void treePolicy() {
        while (!currentState.isTerminal()) {
            if (currentNode.visits == 0) {
                return;
            }

            if (!currentNode.hasChildren()) {
                currentNode.expand(currentState);

                // Select next node and return
                ActionNodePair bestChild = currentNode.bestChild();
                currentState.makeMove(bestChild.action);
                currentNode = bestChild.node;
                return;
            }

            // Select next node
            ActionNodePair bestChild = currentNode.bestChild();
            currentState.makeMove(bestChild.action);
            currentNode = bestChild.node;
        }
    }

    private int simulate(GomokuFunctions state) {
        while (!state.isTerminal()) {
            state.makeMove(defaultPolicy(state));
        }

        return state.score();
    }

    // Return a move to play in simulation
    private Action defaultPolicy(GomokuFunctions state) {
        // Play a winning move
        if (!state.getPlayerThreats().isEmpty()) {
            return state.getPlayerThreats().get(0);
        }

        // Block an opponent's winning move
        if (!state.getEnemyThreats().isEmpty()) {
            return state.getEnemyThreats().get(0);
        }

        if (!state.getPlayerDoubleThreats().isEmpty()) {
            return state.getPlayerDoubleThreats().get(0);
        }

        if (!state.getEnemyDoubleThreats().isEmpty()) {
            return state.getEnemyDoubleThreats().get(random.nextInt(state.getEnemyDoubleThreats().size()));
        }

        List<Action> possibleActions = state.getNeighboursUnion();
        if (possibleActions.size() > 0) {
            return possibleActions.get(random.nextInt(possibleActions.size()));
        }

        possibleActions = state.getPossibleActions();
        return possibleActions.get(random.nextInt(possibleActions.size()));
    }

    private void backpropagate(Node node, int score) {
        do {
            node.totalScore += score;
            node.visits++;

            // Go to parent node and reverse scores for opposite player
            node = node.parent;
            score = -score;
        } while (node != null);
    }
}
