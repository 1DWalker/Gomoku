package gomoku.ai.mcts;

import gomoku.action.Action;
import gomoku.state.GomokuActionList;
import gomoku.state.GomokuFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Node {

    private static final double c = 0.5;
    private static final Random random = new Random();

    // link to parent and parents
    Node parent;
    List<Node> children;
    List<Action> edges;

    // values that this node carries
    int visits = 0;
    double totalScore = 0;
    double heuristic;

    void expand(GomokuFunctions state) {
        List<Action> possibleActions = state.getPlayerThreats();
        if (possibleActions.size() == 0) possibleActions = state.getEnemyThreats();
//        if (possibleActions.size() == 0) possibleActions = GomokuActionList.union(state.getNeighboursUnion(), state.getEnemyDoubleThreats());
        if (possibleActions.size() == 0) possibleActions = state.getEnemyDoubleThreats();
        if (possibleActions.size() == 0) possibleActions = state.getNeighboursUnion();
        if (possibleActions.size() == 0) possibleActions = state.getPossibleActions();

        children = new ArrayList<>(possibleActions.size());
        edges = new ArrayList<>(possibleActions.size());

        for (int i = 0; i < possibleActions.size(); i++) {
            Node child = new Node();
            child.parent = this;

            children.add(child);
            edges.add(possibleActions.get(i));
        }
    }

    Node nextNode(Action edge) {
        if (edges == null) return new Node();

        for (int i = 0; i < edges.size(); i++) {
            if (edge.equals(edges.get(i))) {
                return children.get(i);
            }
        }

        return new Node();
    }

    // Best child according to UCB formula
    ActionNodePair bestChild() {
        int bestChildIndex = -1;
        double bestScore = -Double.MAX_VALUE;

        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);

            if (child.visits == 0) {
                // return a random child with 0 visits
                List<ActionNodePair> candidates = new ArrayList<>(children.size());
                candidates.add(new ActionNodePair(edges.get(i), child));
                for (int k = i + 1; k < children.size(); k++) {
                    if (children.get(k).visits == 0) candidates.add(new ActionNodePair(edges.get(k), children.get(k)));
                }
                return candidates.get(random.nextInt(candidates.size()));
            }

            // calculate score using the UCB formula
            double uctScore = child.totalScore / child.visits + c * Math.sqrt(Math.log(visits) / child.visits) + heuristic / child.visits;
            uctScore += random.nextDouble() / 10000; // very small tiebreaker

            // best score so far?
            if (uctScore >= bestScore) {
                bestChildIndex = i;
                bestScore = uctScore;
            }
        }

        return new ActionNodePair(edges.get(bestChildIndex), children.get(bestChildIndex));
    }

    // Best action from this node
    ActionNodePair bestMove() {
        int bestChildIndex = -1;
        double bestScore = -Double.MAX_VALUE;

        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);

            // If child has not been visited once, do not select this child
            if (child.visits == 0) continue;

            // calculate average score
            double score = child.totalScore / child.visits;
            score += random.nextDouble() / 10000; // very small tiebreaker

            // best score so far?
            if (score >= bestScore) {
                bestChildIndex = i;
                bestScore = score;
            }
        }

        System.out.println(bestScore);
        return new ActionNodePair(edges.get(bestChildIndex), children.get(bestChildIndex));
    }

    boolean hasChildren() {
        return children != null;
    }
}
