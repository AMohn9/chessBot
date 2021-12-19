package amohn.chess.search;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Data
@Slf4j
public class Node {
    final Move lastMove;

    boolean isMaxPlayer;
    double score = 0;
    Map<Move, Node> children = new HashMap<>();

    public Node(Board board, Move lastMove) {
        this.lastMove = lastMove;
        this.isMaxPlayer = board.getSideToMove().equals(Side.WHITE);
    }

    public void addChild(Move move, Node child) {
        children.put(move, child);
    }

    public Node getBestChild() {
        Comparator<Node> byScoreComparator = Comparator.comparing(Node::getScore);

        return children.values().stream()
                .max(isMaxPlayer ? byScoreComparator : byScoreComparator.reversed())
                .orElseThrow(NoSuchElementException::new);
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }
}
