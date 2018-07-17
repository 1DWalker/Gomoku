package gomoku.ai.mcts;

import gomoku.action.Action;
import gomoku.ai.mcts.Node;

public class ActionNodePair {
    Action action;
    Node node;

    ActionNodePair(Action action, Node node) {
        this.action = action;
        this.node = node;
    }
}