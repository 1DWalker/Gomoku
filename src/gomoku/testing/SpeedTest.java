package gomoku.testing;

import gomoku.action.Action;
import gomoku.ai.AI;
import gomoku.ai.examplerandom.RandomAI;
import gomoku.ai.examplesmartrandom.SmartRandomAI;
import gomoku.state.GomokuFunctions;
import gomoku.state.State;

import java.util.List;
import java.util.Random;

public class SpeedTest {

    static final int N = 40000;
    static final Random random = new Random();

    //    static final AI AI_A = new MCTS();
    static final AI AI_A = new SmartRandomAI();
//    static final AI AI_A = new RandomAI();

    //    static final AI AI_B = new MCTS();
//    static final AI AI_B = new Uneven();
//    static final AI AI_B = new Test();
//    static final AI AI_B = new SmartRandomAI();
    static final AI AI_B = new RandomAI();

    public static void main(String[] args) {
//        Gomoku state = new Gomoku();
        GomokuFunctions state = new GomokuFunctions();

        // warm up
        for (int i = 0; i < N / 10; i++) {
//            State copy = state.copy();
            GomokuFunctions copy = state.copy();

            while (!copy.isTerminal()) {
                copy.makeMove(makeMove(copy));
//                copy.printGame();
            }
        }

        long timeStart, timeEnd;
        timeStart = System.nanoTime();

        for (int i = 0; i < N; i++) {
//            Gomoku copy = new Gomoku(state);
//            GomokuFunctions copy = state.copy();
            GomokuFunctions copy = state.copy();

            while (!(copy.isTerminal() || copy.isEffectivelyOver())) {
//                copy.printGame();
                copy.makeMove(makeMove(copy));
            }
//
//            if (copy.isEffectivelyOver() && !copy.isTerminal()) {
//                copy.printGame();
//                System.out.println("Current player threats");
//                for (Action action : copy.getPlayerThreats()) {
//                    action.print();
//                    System.exit(0);
//                }
//                System.out.println("Enemy player threats");
//                for (Action action : copy.getEnemyThreats()) {
//                    action.print();
//                }
//                System.out.println("Effectively Over");
//            }

//            copy.printGame();
        }

        timeEnd = System.nanoTime();

        double diff = (double) (timeEnd - timeStart) / 1000000000;
        System.out.println("Time elapsed: " + diff);
        System.out.println("Simulations per second: " + N / diff);
    }

    // Return a move to play in simulation
    private static Action makeMove(State s) {
        GomokuFunctions state = (GomokuFunctions) s;

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

        // For every move, play and check if win
//        for (Action action : state.getPlayerNeighbours()) {
//            state.fastMakeMove(action);
//            if (state.isWon()) {
//                state.fastBackMove();
//                return action;
//            }
//            state.fastBackMove();
//        }

////         For every enemy move, play and check if win
//        state.changePlayer();
//        for (Action action : state.getEnemyNeighbours()) {
//            state.fastMakeMove(action);
//            if (state.isWon()) {
//                state.fastBackMove();
//                state.changePlayer();
//
//                if (!state.getEnemyThreats().contains(action)) {
//                    System.exit(-1);
//                }
//                return action;
//            }
//            state.fastBackMove();
//        }
//        state.changePlayer();

////         For every move, play and check if it creates a double threat
//        for (Action action : state.getPlayerNeighbours()) {
//            state.fastMakeMove(action);
//            if (state.isDoubleThreat()) {
//                state.fastBackMove();
//
//                if (!state.getPlayerDoubleThreats().contains(action)) {
//                    state.printGame();
//                    action.print();
//                    System.out.println("-----");
//                    for (Action action2 : state.getPlayerDoubleThreats()) {
//                        action2.print();
//                    }
//                    System.out.println("-----");
//                    for (Action action2 : state.getEnemyDoubleThreats()) {
//                        action2.print();
//                    }
//                    System.exit(-1);
//                }
//                return action;
//            }
//            state.fastBackMove();
//        }

//        // If an enemy can create a double threat, play on that square
//        state.changePlayer();
//        for (Action action : state.getEnemyNeighbours()) {
//            state.fastMakeMove(action);
//            if (state.isDoubleThreat()) {
//                state.fastBackMove();
//                state.changePlayer();
//
//                if (!state.getEnemyDoubleThreats().contains(action)) {
//                    state.printGame();
//                    action.print();
//                    System.out.println("-----");
//                    for (Action action2 : state.getPlayerDoubleThreats()) {
//                        action2.print();
//                    }
//                    System.out.println("-----");
//                    for (Action action2 : state.getEnemyDoubleThreats()) {
//                        action2.print();
//                    }
//                    System.out.println("Enemy double Threat");
//                    System.exit(-1);
//                }
//                return action;
//            }
//            state.fastBackMove();
//        }
//        state.changePlayer();

//        state.printGame();
//        printList(state.getPlayerNeighbours());
//        printList(state.getEnemyNeighbours());
//        printList(state.getNeighboursUnion());
//        printList(state.getNeighboursIntersection());

        List<Action> possibleActions = state.getNeighboursIntersection();
        if (possibleActions.size() > 0) {
            return possibleActions.get(random.nextInt(possibleActions.size()));
        }

        possibleActions = state.getNeighboursUnion();
        if (possibleActions.size() > 0) {
            return possibleActions.get(random.nextInt(possibleActions.size()));
        }

//
//        List<Action> possibleActions = state.getPlayerNeighbours();
//        if (possibleActions.size() > 0) {
//            return possibleActions.get(random.nextInt(possibleActions.size()));
//        }

        possibleActions = state.getPossibleActions();
        return possibleActions.get(random.nextInt(possibleActions.size()));
    }

    public static void printList(List<Action> list) {
        System.out.println("---------------");
        for (Action action : list) {
            action.print();
        }
    }
}
