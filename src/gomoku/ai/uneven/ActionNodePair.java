package gomoku.ai.uneven;

import gomoku.action.Action;

public class ActionNodePair {
    Action action;
    Node node;

    ActionNodePair(Action action, Node node) {
        this.action = action;
        this.node = node;
    }
}