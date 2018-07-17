package gomoku.testing;

import gomoku.ai.AI;
import gomoku.ai.examplerandom.RandomAI;
import gomoku.ai.examplesmartrandom.SmartRandomAI;
import gomoku.action.Action;
import gomoku.ai.examplesmartrandom.SmartRandomAICopy;
import gomoku.ai.mcts.MCTS;
import gomoku.ai.uneven.Uneven;
import gomoku.state.Gomoku;
import gomoku.state.State;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AITest {

    static final AI AI_A = new MCTS();

    static final AI AI_B = new Uneven();

    static int N = 100000;
    static boolean swapEveryGame = true;
    static boolean printGame = true;
    static boolean printEnd = false; // print game when game over?
    static int printStatisticsFrequency = 1; // Print statistics every _ games

    static Statistics statistics = new Statistics(N);
    static boolean switchSides = false;

    // Parameters for SPRT test
    static final double elo0 = 0; // null hypothesis: the elo difference between AI_A and AI_B is 0. BTW do not change this parameter
    static double elo1 = 30; // alternative hypothesis: the elo difference between AI_A and AI_B is elo1 (elo0 < elo1)
    // Probability of rejecting the null hypothesis given that it is true
    static double alpha = 0.05;
    // Probability that the null hypothesis fails to be rejected given that it is false
    static double beta = 0.05;

    // continue sprt after completion? DO NOT SET AS TRUE
    static boolean continueSPRT = false;

    public static void main(String[] args) {
        sprt(elo0, elo1, alpha, beta);
    }

    private static boolean stop() {
        System.out.println("Continue playing games? (Y/N)");
        while (true) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String input = in.readLine().toLowerCase();

                if (input.equals("y")) return false;
                else if (input.equals("n")) return true;
                else throw new Exception();
            } catch (Exception e) {
                System.out.println("Input Y or N!");
            }
        }
    }

    // Self terminating test
    private static void sprt(double elo0, double elo1, double alpha, double beta) {
        long timeStart, timeEnd;
        timeStart = System.nanoTime();

        double a = Math.log(beta / (1 - alpha));
        double b = Math.log((1 - beta) / alpha);

        while (true) {
            playGame();

            if ((statistics.getCurrent() - 1) % printStatisticsFrequency == 0) statistics.printSPRT();

            double llr = llr(statistics.getWins(), statistics.getDraws(), statistics.getLosses(), elo0, elo1);

            System.out.println("LLR: " + String.format("%.2f", llr)  + " (" + String.format("%.2f", a) + ", " + String.format("%.2f", b) + ")");
            if (statistics.getWins() == 0) {
                System.out.println("LLR inaccurate because wins == 0.");
            } else if (statistics.getLosses() == 0) {
                System.out.println("LLR inaccurate because losses == 0.");
            }

            timeEnd = System.nanoTime();
            System.out.println("Games per second: " + statistics.getCurrent() / ((double) (timeEnd - timeStart) / 1000000000) + "\n");

            if (!continueSPRT) {
                if (llr >= b) {
                    // Accept H1
                    System.out.println("Accept H1: there is a " + elo1 + " elo difference between AI_A and AI_B.");
                    if (stop()) {
                        return;
                    } else {
                        timeStart += System.nanoTime() - timeEnd; // in between time
                        continueSPRT = true;
                    }
                } else if (llr <= a) {
                    // Accept H0
                    System.out.println("Accept H0: there is no elo difference between AI_A and AI_B.");
                    if (stop()) {
                        return;
                    } else {
                        timeStart += System.nanoTime() - timeEnd;
                        continueSPRT = true;
                    }
                }
            }
        }
    }

    // return the log-likelihood ratio
    // source https://chessprogramming.wikispaces.com/Match%20Statistics
    // functions properly when w > 0 and l > 0
    private static double llr(double w, double d, double l, double elo0, double elo1) {
        if (w == 0 || l == 0) return 0; // avoid division by 0

        double n = w + d + l;

        double wr = w / n;
        double dr = d / n;

        double s = wr + dr / 2;
        double m2 = wr + dr / 4;
        double var = m2 - s * s;
        double var_s = var / n;

        double s0 = ll(elo0);
        double s1 = ll(elo1);

        return (s1-s0)*(2*s-s0-s1)/var_s/2.0;
    }

    private static double ll(double elo) {
        return 1 /(1 + Math.pow(10, -elo/400));
    }

    private static void playGame() {
        State state = new Gomoku();
        AI_A.reset();
        AI_B.reset();

        int ply = 0;

        Action opponentAction = null;

        // Play a game
        while (!state.isTerminal()) {
            ply++;

            if (printGame) state.printGame();

            Action action;
            if (state.isFirstPlayer()) {
                if (!switchSides) {
                    action = AI_A.makeMove(state, opponentAction);
                } else {
                    action = AI_B.makeMove(state, opponentAction);
                }
            } else {
                if (!switchSides) {
                    action = AI_B.makeMove(state, opponentAction);
                } else {
                    action = AI_A.makeMove(state, opponentAction);
                }
            }

            if (printGame) {
                System.out.print("Player " + (state.isFirstPlayer() ? "X" : "O") + " plays ");
                action.print();
            }

            state.makeMove(action);
            opponentAction = action;
        }

        int score = state.score();

        if (printGame || printEnd) {
            state.printGame();

            if (score == 1) {
                System.out.print("Player X wins!");
            } else if (score == 0) {
                System.out.println("Draw!");
            } else {
                System.out.print("Player O wins!");
            }

            if ((score == 1 && !switchSides) || (score == -1 && switchSides)) {
                System.out.println(" (win for AI_A)\n");
            } else {
                if (score != 0) {
                    System.out.println(" (loss for AI_A)\n");
                }
            }
        }

        statistics.addData(score, switchSides, ply);

        if (swapEveryGame) switchSides = !switchSides;
    }
}
