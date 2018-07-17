package gomoku.state;

import gomoku.action.Action;
import gomoku.action.GomokuAction;

import java.util.ArrayList;
import java.util.List;

/*
    List storing unique Gomoku Actions

    Pros:
    - Fast indexed add
    - Fast removal

    Cons:
    - Order not preserved (whatever order means in terms of Gomoku Actions)
    - Slow list initialization -> extra 2D array to copy
    - More memory usage -> extra 2D array
 */

public class GomokuActionList extends ArrayList<GomokuAction> {

    private static final int[][] empty = new int[Gomoku.SIZE][Gomoku.SIZE];
    static {
        for (int i = 0; i < Gomoku.SIZE; i++) {
            for (int j = 0; j < Gomoku.SIZE; j++) {
                empty[i][j] = -1;
            }
        }
    }

    private int[][] actionIndex = new int[Gomoku.SIZE][Gomoku.SIZE];

    public GomokuActionList(int N) {
        super(N);

        for (int i = 0; i < Gomoku.SIZE; i++) {
            System.arraycopy(empty[i], 0, actionIndex[i], 0, Gomoku.SIZE);
        }
    }

    public GomokuActionList(List from) {
        super(from);

        GomokuActionList copy = (GomokuActionList) from;
        for (int i = 0; i < Gomoku.SIZE; i++) {
            System.arraycopy(copy.actionIndex[i], 0, actionIndex[i], 0, Gomoku.SIZE);
        }
    }

    @Override
    public boolean add(GomokuAction action) {
        if (actionIndex[action.getX()][action.getY()] != -1) return false; // Do not allow duplicates

        actionIndex[action.getX()][action.getY()] = size();
        return super.add(action);
    }

    // fast add
    // if indexed location is occupied, move its object to the end of list
    @Override
    public void add(int index, GomokuAction action) {
        // get the action occupying the spot to place the last action
        if (super.size() - 1 >= index) {
            GomokuAction toMove = super.get(index);

            // move this action to the end of the list
            actionIndex[toMove.getX()][toMove.getY()] = super.size();
            super.add(toMove);

            // finally, add last action
            super.set(index, action);
            actionIndex[action.getX()][action.getY()] = index;
        } else {
            // Nothing to move in this case as action belongs in the back
            actionIndex[action.getX()][action.getY()] = super.size();
            super.add(action);
        }
    }

    // fast remove, but does not preserve list order
    @Override
    public GomokuAction remove(int index) {
        int lastIndex = super.size() - 1;
        GomokuAction e = super.get(index);
        actionIndex[e.getX()][e.getY()] = -1;

        GomokuAction last = super.remove(lastIndex);

        if (index != lastIndex) {
            super.set(index, last);
            actionIndex[last.getX()][last.getY()] = index;
        }
        return e;
    }

    // Remove an action from this list
    @Override
    public boolean remove(Object o) {
        return remove(indexOf((GomokuAction) o)) != null;
    }

    @Override
    public boolean contains(Object o) {
        GomokuAction action = (GomokuAction) o;
        return actionIndex[action.getX()][action.getY()] != -1;
    }

    @Override
    public int indexOf(Object o) {
        return indexOf((GomokuAction) o);
    }

    public int indexOf(GomokuAction action) {
        return actionIndex[action.getX()][action.getY()];
    }

    @Override
    public void clear() {
        super.clear();

        for (int i = 0; i < Gomoku.SIZE; i++) {
            System.arraycopy(empty[i], 0, actionIndex[i], 0, Gomoku.SIZE);
        }
    }

    public static List<Action> union(List<Action> A, List<Action> B) {
        List<Action> union = new ArrayList<>(A.size() + B.size());
        union.addAll(A);

        for (Action action : B) {
            if (!A.contains(action)) {
                union.add(action);
            }
        }
        return union;
    }
}
