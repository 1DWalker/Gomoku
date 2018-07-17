package gomoku.state;

import gomoku.action.Action;

import java.util.List;

public interface State {

    // return an copy
    State copy();

    // Make a move based on an Action
    void makeMove(Action action);

    // Make a move based on the index in which the Action appears on getPossibleActions()
    void makeMove(int index);

    // Game over?
    boolean isTerminal();

    // Assuming game over, return the score in the perspective of the first player. 1 for a win, 0 for a draw, -1 for a loss
    int score();

    // Get all possible actions the current player can make
    List<Action> getPossibleActions();

    // Is it the first player?
    boolean isFirstPlayer();

    // Get an action from the user
    Action userMove();

    void printGame();
}


