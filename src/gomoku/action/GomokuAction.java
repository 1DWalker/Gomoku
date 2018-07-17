package gomoku.action;

public class GomokuAction implements Action {

    private final int x, y;

    public GomokuAction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public void print() {
        System.out.println((char) ('A' + x) + "" + (y + 1));
    }

    @Override
    public boolean equals(Action action) {
        return x == ((GomokuAction) action).x && y == ((GomokuAction) action).y;
    }

    public void printCoordinates() {
        System.out.println("(" + x + ", " + y + ")");
    }
}
