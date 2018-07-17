package gomoku.ai.test;

import gomoku.action.Action;
import gomoku.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Node {

    private static final double c = 1;
    private static final Random random = new Random();

    // link to parent and parents
    Node parent;
    List<Node> children;

    // values that this node carries
    int visits = 0;
    double totalScore = 0;

    void expand(State state) {
        List<Action> possibleActions = state.getPossibleActions();

        children = new ArrayList<>(possibleActions.size());

        for (int i = 0; i < possibleActions.size(); i++) {
            Node child = new Node();
            child.parent = this;

            children.add(child);
        }
    }

    // Best child according to UCB formula
    IntegerNodePair bestChild() {
        int bestChildIndex = -1;
        double bestScore = -Double.MAX_VALUE;

        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);

            if (child.visits == 0) {
                // return a random child with 0 visits
                List<Node> candidates = new ArrayList<>(children.size());
                candidates.add(child);
                for (int k = i + 1; k < children.size(); k++) {
                    if (children.get(k).visits == 0) candidates.add(children.get(k));
                }
                int randomIndex = random.nextInt(candidates.size());
                return new IntegerNodePair(randomIndex, candidates.get(randomIndex));

//                return new IntegerNodePair(i, child);
            }

            // calculate score using the UCB formula
            double uctScore = child.totalScore / child.visits + c * Math.sqrt(Math.log(visits) / child.visits);
            uctScore += random.nextDouble() / 10000; // very small tiebreaker

            // best score so far?
            if (uctScore >= bestScore) {
                bestChildIndex = i;
                bestScore = uctScore;
            }
        }

        return new IntegerNodePair(bestChildIndex, children.get(bestChildIndex));
    }

    // Best action from this node
    int bestMove() {
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

        return bestChildIndex;
    }

    boolean hasChildren() {
        return children != null;
    }
}
