package gomoku.ai;

import gomoku.state.State;
import gomoku.action.Action;

// an AI for a two player game

public interface AI {

    // Current state, last action played (opponent's move)
    Action makeMove(State gameState, Action opponentAction);

    // Tell the AI that there will be a new game
    // Useful for clearing memory instead of having the computer reallocate memory
    void reset();
}
