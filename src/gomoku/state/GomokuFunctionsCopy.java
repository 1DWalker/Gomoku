package gomoku.state;

import gomoku.action.Action;
import gomoku.action.GomokuAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Define extra functions that are used in AI
// Or, override methods in superclass for possible efficiency boosts

public class GomokuFunctionsCopy extends Gomoku {

    // Zobrist Hashing - https://en.wikipedia.org/wiki/Zobrist_hashing
    // Used (hopefully) for transposition tables. Advanced material - HARD.
    // http://mediocrechess.blogspot.com/2007/01/guide-transposition-tables.html
    // ^ blog is for alpha beta search btw. (WARNING)
    // UCT application: http://alum.wpi.edu/~jbrodeur/cig08.pdf
    static private final long[][][] zobrist = new long[2][SIZE][SIZE];

    static {
        // Initialize Zobrist Keys
        Random random = new Random();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < SIZE; j++) {
                for (int k = 0; k < SIZE; k++) {
                    zobrist[i][j][k] = random.nextLong();
                }
            }
        }
    }

    private long hash = 0L; // mapped value of current game state to a long using zobrist hashing
    private List<Integer> actionIndexHistory = new ArrayList<>(50); // helper for backMove();

    // Neighbours of occupied squares. Unordered. One list for each player
    private List[] neighbours = new GomokuActionList[]{new GomokuActionList(CELL_COUNT), new GomokuActionList(CELL_COUNT)};

    // Size of lines
    private int[][] verticalSize = new int[SIZE][SIZE]; // |
    private int[][] horizontalSize = new int[SIZE][SIZE]; // -
    private int[][] positiveSize = new int[SIZE][SIZE]; // /
    private int[][] negativeSize = new int[SIZE][SIZE]; // \

    // whether playing at a certain square for a particular player produces a threat
    // used for double threat detection
    private boolean[][][] createsThreat = new boolean[2][SIZE][SIZE];

    public GomokuFunctionsCopy() {
        super();
    }

    private GomokuFunctionsCopy(GomokuFunctionsCopy from) {
        super(from);
        hash = from.hash;
        actionIndexHistory = new ArrayList<>(from.actionIndexHistory);
        neighbours[0] = new GomokuActionList(from.neighbours[0]);
        neighbours[1] = new GomokuActionList(from.neighbours[1]);

        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(from.verticalSize[i], 0, verticalSize[i], 0, SIZE);
            System.arraycopy(from.horizontalSize[i], 0, verticalSize[i], 0, SIZE);
            System.arraycopy(from.positiveSize[i], 0, verticalSize[i], 0, SIZE);
            System.arraycopy(from.negativeSize[i], 0, verticalSize[i], 0, SIZE);

            System.arraycopy(from.createsThreat[0][i], 0, createsThreat[0][i], 0, SIZE);
            System.arraycopy(from.createsThreat[1][i], 0, createsThreat[1][i], 0, SIZE);
        }
    }

    @Override
    public GomokuFunctionsCopy copy() {
        return new GomokuFunctionsCopy(this);
    }

    @Override
    public void makeMove(int x, int y) {
        // update hash
        hash ^= zobrist[playerToMove][x][y];

        super.makeMove(x, y);

        // add neighbours
        changePlayer();
        updateNeighbours(x, y);
        updateSize(x, y);
        changePlayer();
    }

    // Keep track of extra information for backMove()
    @Override
    public void removePossibleAction(int index) {
        super.removePossibleAction(index);
        actionIndexHistory.add(index);
    }

    // Do not update possibleActions, hash, or neighbours
    public void fastMakeMove(int x, int y) {
        grid[x][y] = playerToMove + 1;
        playerToMove = playerToMove == 0 ? 1 : 0;
        ply++;

        xHistory.add(x);
        yHistory.add(y);
    }

    public void fastMakeMove(Action a) {
        GomokuAction action = (GomokuAction) a;
        fastMakeMove(action.getX(), action.getY());
    }

    // Revert the last move, assume ply > 0
    // Copy and paste code rather than call super.backMove() for small efficiency boost
    // DOES NOT REVERT neighbours
    public void backMove() {
        ply--;
        int x = xHistory.remove(ply);
        int y = yHistory.remove(ply);

        // re-add previous action to possible actions list and preserve previous order
        possibleActions.add(actionIndexHistory.remove(actionIndexHistory.size() - 1), new GomokuAction(x, y));

        grid[x][y] = 0;
        playerToMove = playerToMove == 0 ? 1 : 0;

        // revert hash
        hash ^= zobrist[playerToMove][x][y];
    }

    // revert fastMakeMove()
    public void fastBackMove() {
        ply--;
        grid[xHistory.remove(ply)][yHistory.remove(ply)] = 0;
        playerToMove = playerToMove == 0 ? 1 : 0;
    }

    public void changePlayer() {
        playerToMove = playerToMove == 0 ? 1 : 0;
    }

    public List<Action> getCurrentPlayerNeighbours() {
        return neighbours[playerToMove];
    }

    public List<Action> getEnemyPlayerNeighbours() {
        return neighbours[playerToMove == 0 ? 1 : 0];
    }

    // Check for a double threat based on the last move played
    // Generally a double threat means a player will win
    public boolean isDoubleThreat() {
        // there must be at least one move played on the board for this method to work
        int player = playerToMove == 0 ? 2 : 1;
        int lastx = xHistory.get(ply - 1);
        int lasty = yHistory.get(ply - 1);

        // check if threats exist coming from the last move played
        int threatCount = 0; // count the number of threats so far

        // count horizontal -
        int count = 1; // count number in a row as of right now that is connected to the last move
        boolean found = false; // is a potential threat seen?
        int extra1 = 0;
        for (int x = lastx - 1; x >= 0; x--) { // left
            if (grid[x][lasty] == player) {
                if (!found) count++;
                else extra1++;
            } else if (!found && grid[x][lasty] == 0) { // empty cell
                extra1++;
                found = true;
            } else {
                break;
            }
        }

        found = false;
        int extra2 = 0;
        for (int x = lastx + 1; x < SIZE; x++) { // right
            if (grid[x][lasty] == player) {
                if (!found) count++;
                else extra2++;
            } else if (!found && grid[x][lasty] == 0) { // empty cell
                extra2++;
                found = true;
            } else {
                break;
            }
        }

        if (count + extra1 >= IN_A_ROW) threatCount++;
        if (count + extra2 >= IN_A_ROW) threatCount++;

        if (threatCount > 1) return true;

        // count vertical |
        count = 1;
        found = false;
        extra1 = 0;
        for (int y = lasty - 1; y >= 0; y--) { // down
            if (grid[lastx][y] == player) {
                if (!found) count++;
                else extra1++;
            } else if (!found && grid[lastx][y] == 0) { // empty cell
                extra1++;
                found = true;
            } else {
                break;
            }
        }

        found = false;
        extra2 = 0;
        for (int y = lasty + 1; y < SIZE; y++) { // up
            if (grid[lastx][y] == player) {
                if (!found) count++;
                else extra2++;
            } else if (!found && grid[lastx][y] == 0) { // empty cell
                extra2++;
                found = true;
            } else {
                break;
            }
        }

        if (count + extra1 >= IN_A_ROW) threatCount++;
        if (count + extra2 >= IN_A_ROW) threatCount++;

        if (threatCount > 1) return true;

        // count diagonal /
        count = 1;
        found = false;
        extra1 = 0;
        int bound = Math.min(lastx, lasty) + 1;
        for (int i = 1; i < bound; i++) { // bottom left
            if (grid[lastx - i][lasty - i] == player) {
                if (!found) count++;
                else extra1++;
            } else if (!found && grid[lastx - i][lasty - i] == 0) { // empty cell
                extra1++;
                found = true;
            } else {
                break;
            }
        }

        found = false;
        extra2 = 0;
        bound = Math.min(SIZE - lastx, SIZE - lasty);
        for (int i = 1; i < bound; i++) { // top right
            if (grid[lastx + i][lasty + i] == player) {
                if (!found) count++;
                else extra2++;
            } else if (!found && grid[lastx + i][lasty + i] == 0) { // empty cell
                extra2++;
                found = true;
            } else {
                break;
            }
        }

        if (count + extra1 >= IN_A_ROW) threatCount++;
        if (count + extra2 >= IN_A_ROW) threatCount++;

        if (threatCount > 1) return true;

        // count diagonal \
        count = 1;
        found = false;
        extra1 = 0;
        bound = Math.min(lastx + 1, SIZE - lasty);
        for (int i = 1; i < bound; i++) { // top left
            if (grid[lastx - i][lasty + i] == player) {
                if (!found) count++;
                else extra1++;
            } else if (!found && grid[lastx - i][lasty + i] == 0) { // empty cell
                extra1++;
                found = true;
            } else {
                break;
            }
        }

        found = false;
        extra2 = 0;
        bound = Math.min(SIZE - lastx, lasty + 1);
        for (int i = 1; i < bound; i++) { // bottom right
            if (grid[lastx + i][lasty - i] == player) {
                if (!found) count++;
                else extra2++;
            } else if (!found && grid[lastx + i][lasty - i] == 0) { // empty cell
                extra2++;
                found = true;
            } else {
                break;
            }
        }

        if (count + extra1 >= IN_A_ROW) threatCount++;
        if (count + extra2 >= IN_A_ROW) threatCount++;

        return threatCount > 1;
    }

    public long getHash() {
        return hash;
    }

    // is a cell empty?
    public boolean isEmpty(int x, int y) {
        return grid[x][y] == 0;
    }

    // is a cell empty?
    public boolean isEmpty(Action a) {
        GomokuAction action = (GomokuAction) a;
        return isEmpty(action.getX(), action.getY());
    }

    public boolean inGrid(int x, int y) {
        return !(x < 0 || x >= SIZE || y < 0 || y >= SIZE);
    }

    public boolean occupiedByCurrentPlayer(int x, int y) {
        return grid[x][y] == (playerToMove == 1 ? 2 : 1);
    }

    // Prints the hash as a binary form
    public void printHashAsBinary() {
        printLongAsBinary(hash);
    }

    public static void printLongAsBinary(long num) {
        for (int i = 0; i < Long.numberOfLeadingZeros(num); i++) {
            System.out.print('0');
        }

        System.out.println(Long.numberOfLeadingZeros(num) != 64 ? Long.toBinaryString(num) : "");
    }

    private void updateNeighbours(int x, int y) {
        for (int i = Math.max(0, x - 1); i <= Math.min(x + 1, Gomoku.SIZE - 1); i++) {
            for (int j = Math.max(0, y - 1); j <= Math.min(y + 1, Gomoku.SIZE - 1); j++) {
                if (isEmpty(i, j)) {
                    GomokuAction action = new GomokuAction(i, j);
                    if (!neighbours[playerToMove].contains(action)) neighbours[playerToMove].add(action);
                }
            }
        }

        GomokuAction action = new GomokuAction(x, y);
        if (neighbours[playerToMove].contains(action)) neighbours[playerToMove].remove(action);
        if (neighbours[playerToMove == 0 ? 1 : 0].contains(action)) neighbours[playerToMove == 0 ? 1 : 0].remove(action);
    }

    private void updateSize(int x, int y) {
        // Up |

        int l = 0;
        int u = 0;
        if (inGrid(x, y - 1) && occupiedByCurrentPlayer(x, y - 1)) {
            l = verticalSize[x][y - 1];
        }
        if (inGrid(x, y + 1) && occupiedByCurrentPlayer(x, y + 1)) {
            u = verticalSize[x][y + 1];
        }
        // Combine and update
        int newSize = l + u + 1;

        for (int pointY = y - l; pointY <= y + u; pointY++) {
            verticalSize[x][pointY] = newSize;
        }

        // Detect threats

        List<Action> newThreats = new ArrayList<>(2);
        // group of 4 or connects to another group to create 5+
        if (inGrid(x, y - l - 1) && isEmpty(x, y - l - 1)) {
            if (newSize == IN_A_ROW - 1) {
                newThreats.add(new GomokuAction(x, y - l - 1));
            } else if (inGrid(x, y - l - 2) && occupiedByCurrentPlayer(x, y - l - 2)) {
                if (newSize + verticalSize[x][y - l - 2] >= IN_A_ROW - 1) {
                    newThreats.add(new GomokuAction(x, y - l - 1));
                }
            }
        }
        if (inGrid(x, y + u + 1) && isEmpty(x, y + u + 1)) {
            if (newSize == IN_A_ROW - 1) {
                newThreats.add(new GomokuAction(x, y + u + 1));
            } else if (inGrid(x, y + u + 2) && occupiedByCurrentPlayer(x, y + u + 2)) {
                if (newSize + verticalSize[x][y + u + 2] >= IN_A_ROW - 1) {
                    newThreats.add(new GomokuAction(x, y + u + 1));
                }
            }
        }

        if (newThreats.size() >= 2) { // Double threat exists
            printGame();
            new GomokuAction(x, y).print();
            System.out.println("A double threat has been produced.");
        }

        // Determine which squares would create a threat with (x, y)
        // ..?XXA, ?.XXA, X.?XA, X?.XA - example cases where A is (x, y)
        // .?XXA would create a DOUBLE THREAT
        if (newSize == IN_A_ROW - 2) {
            if (inGrid(x, y - l - 1) && isEmpty(x, y - l - 1)) {
                addCurrentThreat(x, y - l - 1);
                if (inGrid(x, y - l - 2) && isEmpty(x, y - l - 2)) {
                    addCurrentThreat(x, y - l - 2);
                }
            }
            if (inGrid(x, y + u + 1) && isEmpty(x, y + u + 1)) {
                addCurrentThreat(x, y + u + 1);
                if (inGrid(x, y + u + 2) && isEmpty(x, y + u + 2)) {
                    addCurrentThreat(x, y + u + 2);
                }
            }
        }
    }

    private void addCurrentThreat(int x, int y) {
        if (createsThreat[playerToMove][x][y]) {
            printGame();
            System.out.print("DOUBLE THREAT BY PLAYING ");
            new GomokuAction(x, y).print();
        } else {

            createsThreat[playerToMove][x][y] = true;
            printGame();
            System.out.print("SINGLE THREAT BY PLAYING ");
            new GomokuAction(x, y).print();
        }
    }
}
