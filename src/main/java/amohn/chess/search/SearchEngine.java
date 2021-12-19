package amohn.chess.search;

public interface SearchEngine {
    Node getRoot();

    int getEvaluatedPositions();

    void constructTree(int depth);
}
