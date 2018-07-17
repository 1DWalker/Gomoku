package gomoku.state;

import gomoku.action.Action;
import gomoku.action.GomokuAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Define extra functions that are used in AI
// Or, override methods in superclass for possible efficiency boosts

public class GomokuFunctions extends Gomoku {

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

    // The union of neighbours[0] and neighbours[1]
    private List neighboursUnion = new GomokuActionList(CELL_COUNT);

    private List neighboursIntersection = new GomokuActionList(CELL_COUNT);

    private List[] threats = new GomokuActionList[]{new GomokuActionList(8), new GomokuActionList(8)};

    private List[] doubleThreats = new GomokuActionList[]{new GomokuActionList(3), new GomokuActionList(3)};

    // last move was a win?
    private boolean win = false;

    public GomokuFunctions() {
        super();
    }

    private GomokuFunctions(GomokuFunctions from) {
        super(from);
        hash = from.hash;
        actionIndexHistory = new ArrayList<>(from.actionIndexHistory);
        win = from.win;

        neighboursUnion = new GomokuActionList(from.neighboursUnion);
        neighboursIntersection = new GomokuActionList(from.neighboursIntersection);

        for (int i = 0; i < 2; i++) {
            neighbours[i] = new GomokuActionList(from.neighbours[i]);
            threats[i] = new GomokuActionList(from.threats[i]);
            doubleThreats[i] = new GomokuActionList(from.doubleThreats[i]);
        }
    }

    @Override
    public GomokuFunctions copy() {
        return new GomokuFunctions(this);
    }

    @Override
    public void makeMove(int x, int y) {
        // update hash
        hash ^= zobrist[playerToMove][x][y];

        super.makeMove(x, y);

        win = super.isWon();
        if (win) return;

        // add neighbours
        changePlayer();
        updateNeighbours(x, y);
        changePlayer();

        detectThreats(x, y);
    }

    // Keep track of extra information for backMove()
    @Override
    public void removePossibleAction(int index) {
        super.removePossibleAction(index);
        actionIndexHistory.add(index);
    }

    // Do not update possibleActions, hash, neighbours, or threats
    public void fastMakeMove(int x, int y) {
        grid[x][y] = playerToMove + 1;
        playerToMove = playerToMove == 0 ? 1 : 0;
        ply++;

        xHistory.add(x);
        yHistory.add(y);

        win = super.isWon();
    }

    public void fastMakeMove(Action a) {
        GomokuAction action = (GomokuAction) a;
        fastMakeMove(action.getX(), action.getY());
    }

    // Revert the last move, assume ply > 0
    // Copy and paste code rather than call super.backMove() for small efficiency boost
    // DOES NOT REVERT neighbours or threats
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

        win = false;
    }

    // revert fastMakeMove()
    public void fastBackMove() {
        ply--;
        grid[xHistory.remove(ply)][yHistory.remove(ply)] = 0;
        playerToMove = playerToMove == 0 ? 1 : 0;

        win = false;
    }

    public void changePlayer() {
        playerToMove = playerToMove == 0 ? 1 : 0;
    }

    // Get the neighbours of the current player
    public List<Action> getPlayerNeighbours() {
        return neighbours[playerToMove];
    }

    // Get the neighbours of the opposing player
    public List<Action> getEnemyNeighbours() {
        return neighbours[playerToMove == 0 ? 1 : 0];
    }

    // Get the set of all neighbours of all stones on the board
    public List<Action> getNeighboursUnion() {
        return neighboursUnion;
    }

    // Get the intersection of two players' neighbours
    public List<Action> getNeighboursIntersection() {
        return neighboursIntersection;
    }

    public List<Action> getPlayerThreats() {
        return threats[playerToMove == 0 ? 1 : 0];
    }

    public List<Action> getEnemyThreats() {
        return threats[playerToMove];
    }

    public List<Action> getPlayerDoubleThreats() {
        return doubleThreats[playerToMove == 0 ? 1 : 0];
    }

    public List<Action> getEnemyDoubleThreats() {
        return doubleThreats[playerToMove];
    }

    // Determines if the game is effectively over based on threats present on the board
    public boolean isEffectivelyOver() {
        if (getPlayerThreats().size() != 0) return true;
        if (getEnemyThreats().size() >= 2) return true;
        return false;
    }

    // Check for a double threat based on the last move played
    // Generally a double threat means a player will win
    public boolean isDoubleThreat() {
        return isDoubleThreat(xHistory.get(ply - 1), yHistory.get(ply - 1));
    }

    public long getHash() {
        return hash;
    }

    @Override
    public boolean isWon() {
        return win;
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
                    if (!neighbours[playerToMove].contains(action)) {
                        neighbours[playerToMove].add(action);

                        if (neighbours[playerToMove == 0 ? 1 : 0].contains(action)) {
                            neighboursIntersection.add(action);
                        } else {
                            neighboursUnion.add(action);
                        }
                    }
                }
            }
        }

        GomokuAction action = new GomokuAction(x, y);
        if (neighbours[playerToMove].contains(action)) neighbours[playerToMove].remove(action);
        if (neighbours[playerToMove == 0 ? 1 : 0].contains(action)) neighbours[playerToMove == 0 ? 1 : 0].remove(action);
        if (neighboursUnion.contains(action)) neighboursUnion.remove(action);
        if (neighboursIntersection.contains(action)) neighboursIntersection.remove(action);
    }

    // Determines if the move played at (x, y) creates immediate threats or double threats
    private void detectThreats(int lastx, int lasty) {

        // Filter enemy threats and double threats based on move played
        GomokuAction action = new GomokuAction(lastx, lasty);
        if (threats[playerToMove == 0 ? 1 : 0].contains(action)) threats[playerToMove == 0 ? 1 : 0].remove(action);

        // Remove enemy double threats no longer present
        if (doubleThreats[playerToMove == 0 ? 1 : 0].contains(action)) doubleThreats[playerToMove == 0 ? 1 : 0].remove(action);

        changePlayer();
        List<Action> toRemove = new ArrayList<>(4);
        for (Action doubleThreatAction : getEnemyDoubleThreats()) {
            if (!isDoubleThreat(((GomokuAction) doubleThreatAction).getX(), ((GomokuAction) doubleThreatAction).getY())) {
                toRemove.add(doubleThreatAction);
            }
        }
        for (Action doubleThreatAction : toRemove) {
            doubleThreats[playerToMove].remove(doubleThreatAction);
        }
        changePlayer();

        // Detect threats
        int player = playerToMove == 0 ? 2 : 1;

        // count horizontal -
        int count = 1; // count number in a row as of right now that is connected to the last move
        boolean found = false; // is a potential threat seen?
        int extra1 = 0;
        int empty1 = 1;
        for (int x = lastx - 1; x >= 0; x--) { // left
            if (grid[x][lasty] == player) {
                if (!found) count++;
                else extra1++;
            } else if (!found && grid[x][lasty] == 0) { // empty cell
                extra1++;
                found = true;
                empty1 = x;
            } else {
                if (isEmpty(x, lasty) && isDoubleThreat(x, lasty)) {
                    doubleThreats[playerToMove].add(new GomokuAction(x, lasty));
                }
                break;
            }
        }

        boolean found2 = false;
        int extra2 = 0;
        int empty2 = 0;
        for (int x = lastx + 1; x < SIZE; x++) { // right
            if (grid[x][lasty] == player) {
                if (!found2) count++;
                else extra2++;
            } else if (!found2 && grid[x][lasty] == 0) { // empty cell
                extra2++;
                found2 = true;
                empty2 = x;
            } else {
                if (isEmpty(x, lasty) && isDoubleThreat(x, lasty)) {
                    doubleThreats[playerToMove].add(new GomokuAction(x, lasty));
                }
                break;
            }
        }

        if (count + extra1 >= IN_A_ROW) {
            threats[playerToMove].add(new GomokuAction(empty1, lasty));
        } else if (found && isDoubleThreat(empty1, lasty)) {
            doubleThreats[playerToMove].add(new GomokuAction(empty1, lasty));
        }
        if (count + extra2 >= IN_A_ROW) {
            threats[playerToMove].add(new GomokuAction(empty2, lasty));
        } else if (found2 && isDoubleThreat(empty2, lasty)) {
            doubleThreats[playerToMove].add(new GomokuAction(empty2, lasty));
        }

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
                empty1 = y;
            } else {
                if (isEmpty(lastx, y) && isDoubleThreat(lastx, y)) {
                    doubleThreats[playerToMove].add(new GomokuAction(lastx, y));
                }
                break;
            }
        }

        found2 = false;
        extra2 = 0;
        for (int y = lasty + 1; y < SIZE; y++) { // up
            if (grid[lastx][y] == player) {
                if (!found2) count++;
                else extra2++;
            } else if (!found2 && grid[lastx][y] == 0) { // empty cell
                extra2++;
                found2 = true;
                empty2 = y;
            } else {
                if (isEmpty(lastx, y) && isDoubleThreat(lastx, y)) {
                    doubleThreats[playerToMove].add(new GomokuAction(lastx, y));
                }
                break;
            }
        }

        if (count + extra1 >= IN_A_ROW) {
            threats[playerToMove].add(new GomokuAction(lastx, empty1));
        } else if (found && isDoubleThreat(lastx, empty1)) {
            doubleThreats[playerToMove].add(new GomokuAction(lastx, empty1));
        }
        if (count + extra2 >= IN_A_ROW) {
            threats[playerToMove].add(new GomokuAction(lastx, empty2));
        } else if (found2 && isDoubleThreat(lastx, empty2)) {
            doubleThreats[playerToMove].add(new GomokuAction(lastx, empty2));
        }

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
                empty1 = i;
            } else {
                if (isEmpty(lastx - i, lasty - i) && isDoubleThreat(lastx - i, lasty - i)) {
                    doubleThreats[playerToMove].add(new GomokuAction(lastx - i, lasty - i));
                }
                break;
            }
        }

        found2 = false;
        extra2 = 0;
        bound = Math.min(SIZE - lastx, SIZE - lasty);
        for (int i = 1; i < bound; i++) { // top right
            if (grid[lastx + i][lasty + i] == player) {
                if (!found2) count++;
                else extra2++;
            } else if (!found2 && grid[lastx + i][lasty + i] == 0) { // empty cell
                extra2++;
                found2 = true;
                empty2 = i;
            } else {
                if (isEmpty(lastx + i, lasty + i) && isDoubleThreat(lastx + i, lasty + i)) {
                    doubleThreats[playerToMove].add(new GomokuAction(lastx + i, lasty + i));
                }
                break;
            }
        }

        if (count + extra1 >= IN_A_ROW) {
            threats[playerToMove].add(new GomokuAction(lastx - empty1, lasty - empty1));
        } else if (found && isDoubleThreat(lastx - empty1, lasty - empty1)) {
            doubleThreats[playerToMove].add(new GomokuAction(lastx - empty1, lasty - empty1));
        }
        if (count + extra2 >= IN_A_ROW) {
            threats[playerToMove].add(new GomokuAction(lastx + empty2, lasty + empty2));
        } else if (found2 && isDoubleThreat(lastx + empty2, lasty + empty2)) {
            doubleThreats[playerToMove].add(new GomokuAction(lastx + empty2, lasty + empty2));
        }

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
                empty1 = i;
            } else {
                if (isEmpty(lastx - i, lasty + i) && isDoubleThreat(lastx - i, lasty + i)) {
                    doubleThreats[playerToMove].add(new GomokuAction(lastx - i, lasty + i));
                }
                break;
            }
        }

        found2 = false;
        extra2 = 0;
        bound = Math.min(SIZE - lastx, lasty + 1);
        for (int i = 1; i < bound; i++) { // bottom right
            if (grid[lastx + i][lasty - i] == player) {
                if (!found2) count++;
                else extra2++;
            } else if (!found2 && grid[lastx + i][lasty - i] == 0) { // empty cell
                extra2++;
                found2 = true;
                empty2 = i;
            } else {
                if (isEmpty(lastx + i, lasty - i) && isDoubleThreat(lastx + i, lasty - i)) {
                    doubleThreats[playerToMove].add(new GomokuAction(lastx + i, lasty - i));
                }
                break;
            }
        }

        if (count + extra1 >= IN_A_ROW) {
            threats[playerToMove].add(new GomokuAction(lastx - empty1, lasty + empty1));
        } else if (found && isDoubleThreat(lastx - empty1, lasty + empty1)) {
            doubleThreats[playerToMove].add(new GomokuAction(lastx - empty1, lasty + empty1));
        }
        if (count + extra2 >= IN_A_ROW) {
            threats[playerToMove].add(new GomokuAction(lastx + empty2, lasty - empty2));
        } else if (found2 && isDoubleThreat(lastx + empty2, lasty - empty2)) {
            doubleThreats[playerToMove].add(new GomokuAction(lastx + empty2, lasty - empty2));
        }
    }

    private boolean isDoubleThreat(int lastx, int lasty) {
        // there must be at least one move played on the board for this method to work
        int player = playerToMove == 0 ? 2 : 1;

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
}
