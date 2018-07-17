package gomoku.ai.examplesmartrandom;

import gomoku.action.Action;
import gomoku.ai.AI;
import gomoku.state.GomokuFunctions;
import gomoku.state.State;

import java.util.List;
import java.util.Random;

// Play randomly unless there is a winning move, the opponent has a winning move, or there is a move that creates a double threat

public class SmartRandomAICopy implements AI {

    private final Random random = new Random();

    // Keep a GomokuFunctions object to use the extra functions provided
    private GomokuFunctions state;

    @Override
    public void reset() {
        state = new GomokuFunctions();
    }

    @Override
    public Action makeMove(State gameState, Action opponentAction) {
        if (opponentAction != null) { // play opponents move
            state.makeMove(opponentAction);
        }

        return play(policy(state));
    }

    private Action policy(GomokuFunctions state) {
        // Play a winning move
        if (state.getPlayerThreats().size() != 0) {
            return state.getPlayerThreats().get(0);
        }

        // Block an opponent's winning move
        if (state.getEnemyThreats().size() != 0) {
            return state.getEnemyThreats().get(0);
        }

        if (state.getPlayerDoubleThreats().size() != 0) {
            return state.getPlayerDoubleThreats().get(0);
        }

        if (state.getEnemyDoubleThreats().size() != 0) {
            return state.getEnemyDoubleThreats().get(random.nextInt(state.getEnemyDoubleThreats().size()));
        }

        List<Action> possibleActions = state.getNeighboursIntersection();
        if (possibleActions.size() > 0) {
            return possibleActions.get(random.nextInt(possibleActions.size()));
        }

        possibleActions = state.getNeighboursUnion();
        if (possibleActions.size() > 0) {
            return possibleActions.get(random.nextInt(possibleActions.size()));
        }

        possibleActions = state.getPossibleActions();
        return possibleActions.get(random.nextInt(possibleActions.size()));
    }

    // Update state and return action
    private Action play(Action action) {
        state.makeMove(action);
        return action;
    }
}
