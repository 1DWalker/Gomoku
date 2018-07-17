package gomoku.action;

public interface Action {
    // print something nice such as "A4". Refer to GomokuAction.java for an examplerandom
    void print();

    boolean equals(Action action);
}

