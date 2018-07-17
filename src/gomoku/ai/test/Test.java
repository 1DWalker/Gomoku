package gomoku.ai.test;

import gomoku.action.Action;
import gomoku.ai.AI;
import gomoku.state.State;

import java.util.List;
import java.util.Random;

public class Test implements AI {

    private final int N = 500; // number of simulations

    private static final Random random = new Random();

    private int iteration;

    private Node rootNode;
    private State rootState;

    // Used when traversing the tree
    private Node currentNode;
    private State currentState;

    // Time
    long timeStart, timeEnd;

    @Override
    public void reset() {
        rootNode = new Node();
    }

    @Override
    public Action makeMove(State state, Action lastMove) {
        rootState = state;

        // perhaps you wouldn't want to make a new tree every time the AI makes a move
        // something like rootNode = rootNode.child would be nicer than this
        rootNode = new Node();

        iteration = 0;
        timeStart = System.currentTimeMillis();
        while (timeRemaining()) {
            iterate();
        }

//        System.out.println("Speed: " + (double) iteration / (timeEnd - timeStart) * 1000);

        return rootState.getPossibleActions().get(rootNode.bestMove());
    }

    private boolean timeRemaining() {
        iteration++;

        timeEnd = System.currentTimeMillis();
//        return timeEnd - timeStart < 2000;
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
                IntegerNodePair bestChild = currentNode.bestChild();
                currentState.makeMove(bestChild.actionIndex);
                currentNode = bestChild.node;
                return;
            }

            // Select next node
            IntegerNodePair bestChild = currentNode.bestChild();
            currentState.makeMove(bestChild.actionIndex);
            currentNode = bestChild.node;
        }
    }

    private int simulate(State state) {
        while (!state.isTerminal()) {
            List<Action> possibleActions = state.getPossibleActions();
            state.makeMove(possibleActions.get(random.nextInt(possibleActions.size()))); // random action
        }

        return state.score();
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
