package gomoku.ai.examplerandom;

import gomoku.ai.AI;
import gomoku.action.Action;
import gomoku.state.State;

import java.util.List;
import java.util.Random;

public class RandomAI implements AI {

    private final Random random = new Random();

    @Override
    public Action makeMove(State gameState, Action opponentAction) {
        List<Action> possibleActions = gameState.getPossibleActions();
        return possibleActions.get(random.nextInt(possibleActions.size()));
    }

    @Override
    public void reset() {
        // do nothing
        // this AI does not need to clear memory or anything of the sort
    }
}
