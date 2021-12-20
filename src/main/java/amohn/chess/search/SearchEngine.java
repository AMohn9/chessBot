package amohn.chess.search;

import java.util.Set;

public interface SearchEngine {
    Node getRoot();

    int getEvaluatedPositions();

    void constructTree(int depth);
    void constructTreeKeepCapturing(int minDepth, int maxDepth);

    Set<String> getSeenPositions();
}
