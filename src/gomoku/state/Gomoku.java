package gomoku.state;

import gomoku.action.Action;
import gomoku.action.GomokuAction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Gomoku implements State {

    public static final int SIZE = 15;
    public static final int CELL_COUNT = SIZE * SIZE;
    public static final int IN_A_ROW = 5; // minimum number in a row for a win

    // User input
    private static final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    int[][] grid = new int[SIZE][SIZE];
    int playerToMove; // 0 for first player, 1 for second player

    int ply; // move number

    // History of moves played
    List<Integer> xHistory;
    List<Integer> yHistory;

    // Keep track of possible actions, remove from list as game goes on
//    List<Action> possibleActions;
//    int[][] actionIndex = new int[SIZE][SIZE]; // a way to find the index for any action efficiently

    List possibleActions;

    // Create a game
    public Gomoku() {
        playerToMove = 0;
        ply = 0;

        xHistory = new ArrayList<>(CELL_COUNT);
        yHistory = new ArrayList<>(CELL_COUNT);

        possibleActions = new GomokuActionList(CELL_COUNT);
//        int index = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                possibleActions.add(new GomokuAction(i, j));
//                actionIndex[i][j] = index++;
            }
        }
    }

    Gomoku(Gomoku from) {
        // 2d array copying
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(from.grid[i], 0, grid[i], 0, SIZE);
//            System.arraycopy(from.actionIndex[i], 0, actionIndex[i], 0, SIZE);
        }

        playerToMove = from.playerToMove;
        ply = from.ply;

        xHistory = new ArrayList<>(from.xHistory);
        yHistory = new ArrayList<>(from.yHistory);

        possibleActions = new GomokuActionList(from.possibleActions);
    }

    @Override
    public State copy() {
        return new Gomoku(this);
    }

    void makeMove(int x, int y) {
        // IMPORTANT: remove this test code later for a small performance increase
        if (grid[x][y] != 0) {
            printGame();
            System.out.println("NO. YOU ARE ATTEMPTING TO CHEAT! error occured in makeMove(int x, int y);");
            (new GomokuAction(x, y)).print();

            Thread.dumpStack();
            System.exit(-1);
        }

        // It is assumed that grid[x][y] = 0
        grid[x][y] = playerToMove + 1;

        playerToMove = playerToMove == 0 ? 1 : 0;
        ply++;

        xHistory.add(x);
        yHistory.add(y);
    }

    @Override
    public void makeMove(Action a) {
        GomokuAction action = (GomokuAction) a;
        makeMove(action.getX(), action.getY());

        // remove action from possibleMoves list
        removePossibleAction(possibleActions.indexOf(a));
    }

    @Override
    public void makeMove(int index) {
        GomokuAction action = (GomokuAction) possibleActions.get(index);
        makeMove(action.getX(), action.getY());

        // remove action from possibleMoves list
        removePossibleAction(index);
    }

    void removePossibleAction(int index) {
        possibleActions.remove(index);
    }

    @Override
    public List<Action> getPossibleActions() {
        return possibleActions;
    }

    @Override
    public boolean isFirstPlayer() {
        return playerToMove == 0;
    }

    @Override
    public int score() {
        // 1 if won for the first player, 0 if draw, -1 if won for the second player
        // assumes isTerminal() is true
        if (isWon()) return isFirstPlayer() ? -1 : 1;
        else return 0;
    }

    @Override
    public boolean isTerminal() {
        return isWon() || isDrawn();
    }

    public boolean isDrawn() {
        // It is assumed that isWon() is checked before this method is called
        return ply == CELL_COUNT;
    }

    // Check for a win based on the last move played
    public boolean isWon() {
        if (ply == 0) return false; // there is no last move played if ply is 0

        int player = playerToMove == 0 ? 2 : 1; // the number of the player on the grid (1 - first, 2 - second) for the last player that made a move
        int lastx = xHistory.get(ply - 1);
        int lasty = yHistory.get(ply - 1);

        // count horizontal -
        int count = 1;
        for (int x = lastx - 1; x >= 0; x--) { // left
            if (grid[x][lasty] == player) count++;
            else break;
        }

        for (int x = lastx + 1; x < SIZE; x++) { // right
            if (grid[x][lasty] == player) count++;
            else break;
        }

        if (count >= IN_A_ROW) return true;

        // count vertical |
        count = 1;
        for (int y = lasty - 1; y >= 0; y--) { // down
            if (grid[lastx][y] == player) count++;
            else break;
        }

        for (int y = lasty + 1; y < SIZE; y++) { // up
            if (grid[lastx][y] == player) count++;
            else break;
        }

        if (count >= IN_A_ROW) return true;

        // count diagonal /
        count = 1;
        int bound = Math.min(lastx, lasty) + 1;
        for (int i = 1; i < bound; i++) { // bottom left
            if (grid[lastx - i][lasty - i] == player) count++;
            else break;
        }

        bound = Math.min(SIZE - lastx, SIZE - lasty);
        for (int i = 1; i < bound; i++) { // top right
            if (grid[lastx + i][lasty + i] == player) count++;
            else break;
        }

        if (count >= IN_A_ROW) return true;

        // count diagonal \
        count = 1;
        bound = Math.min(lastx + 1, SIZE - lasty);
        for (int i = 1; i < bound; i++) { // top left
            if (grid[lastx - i][lasty + i] == player) count++;
            else break;
        }

        bound = Math.min(SIZE - lastx, lasty + 1);
        for (int i = 1; i < bound; i++) { // bottom right
            if (grid[lastx + i][lasty - i] == player) count++;
            else break;
        }

        return count >= IN_A_ROW;
    }

    // get user input
    @Override
    public Action userMove() {
        System.out.println("Input a move! Ex. A2");

        while (true) {
            String line;
            int x, y;

            try {
                line = in.readLine();

                if (line.contains(" ")) { // just in case the user inputs something like A 2
                    String[] tokens = line.split(" ");
                    x = tokens[0].toUpperCase().charAt(0) - 'A';
                    y = Integer.parseInt(tokens[1]) - 1;
                } else {
                    x = line.toUpperCase().charAt(0) - 'A';
                    y = Integer.parseInt(line.substring(1)) - 1;
                }

                if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
                    System.out.println("Out of bounds. Please select another action.");
                    continue;
                }

                if (!isEmpty(x, y)) {
                    System.out.println("You must choose an empty cell!");
                    continue;
                }
            } catch (Exception e) {
                System.out.println("Please follow the examplerandom format.");
                continue;
            }

            return new GomokuAction(x, y);
        }
    }

    // is a cell empty?
    private boolean isEmpty(int x, int y) {
        return grid[x][y] == 0;
    }

    // Print the game using the standard system of indexing Go boards
    @Override
    public void printGame() {
        String line = "";

        line += "  ";
        for (int i = 0; i < SIZE; i++) {
            line += "   " + (char) ('A' + i);
        }
        System.out.println(line);

        line = "";
        line += "   -";
        for (int j = 0; j < SIZE; j++) {
            line += "----";
        }
        System.out.println(line);

        for (int i = 0; i < SIZE; i++) {
            line = "";
            line += String.format("%2d", SIZE - i) + " |";
            for (int j = 0; j < SIZE; j++) {
                if (grid[j][SIZE - 1 - i] == 0) {
                    line += "   ";
                } else if (grid[j][SIZE - 1 - i] == 1) {
                    line += " X ";
                } else {
                    line += " O ";
                }
                line += "|";
            }
            line += " " + String.format("%2d", SIZE - i);
            System.out.println(line);

            line = "";
            line += "   -";
            for (int j = 0; j < SIZE; j++) {
                line += "----";
            }
            System.out.println(line);
        }

        line = "";
        line += "  ";
        for (int i = 0; i < SIZE; i++) {
            line += "   " + (char) ('A' + i);
        }
        System.out.println(line + "\n");
    }

    public int[][] getGrid() {
        int[][] copy = new int[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, SIZE);
        }

        return copy;
    }

    public int getPly() {
        return ply;
    }

    // This method helps for clarity regarding the indexing of the board
    // The horizontal axis is i in grid[i][j]
    // The vertical axis is j in grid[i][j]
    // (0, 0) is at the bottom left corner. (think the cartesian system)
    public void printGameCoordinates() {
        System.out.print("  ");
        for (int i = 0; i < SIZE; i++) {
            System.out.print("  " + String.format("%2d", i));
        }
        System.out.println();

        System.out.print("   ");
        for (int i = 0; i < SIZE; i++) {
            System.out.print("  - ");
        }
        System.out.println();

        for (int i = 0; i < SIZE; i++) {
            System.out.print(String.format("%2d", SIZE - 1 - i) + " |");
            for (int j = 0; j < SIZE; j++) {
                if (grid[j][SIZE - 1 - i] == 0) {
                    System.out.print("   ");
                } else if (grid[j][SIZE - 1 - i] == 1) {
                    System.out.print(" X ");
                } else {
                    System.out.print(" O ");
                }
                System.out.print("|");
            }
            System.out.println(" " + String.format("%2d", SIZE - 1 - i));

            System.out.print("   ");
            for (int j = 0; j < SIZE; j++) {
                System.out.print("  - ");
            }
            System.out.println();
        }

        System.out.print("  ");
        for (int i = 0; i < SIZE; i++) {
            System.out.print("  " + String.format("%2d", i));
        }
        System.out.println();
    }
}

