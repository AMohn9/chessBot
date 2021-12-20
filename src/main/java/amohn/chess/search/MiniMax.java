package amohn.chess.search;

import amohn.chess.BoardMoveCache;
import amohn.chess.evaluation.BoardScorer;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class MiniMax implements SearchEngine {
    private final Board board;

    @Getter
    private final Node root;

    @Getter
    private int evaluatedPositions = 0;

    @Getter
    public Set<String> seenPositions = new HashSet<>();

    public MiniMax (Board board) {
        this.board = board;
        this.root = new Node(board, null);
        root.setScore(BoardScorer.scoreBoard(board));
    }

    public void constructTree(int depth) {
        constructTree(root, depth);
    }

    public void constructTreeKeepCapturing(int minDepth, int maxDepth) {
        constructTreeKeepCapturing(root, minDepth, maxDepth);
    }

    private void constructTree(Node node, int remainingDepth) {
        if (remainingDepth == 0) {
            node.setScore(BoardScorer.scoreBoard(board));
            return;
        }

        BoardMoveCache.getMovesForBoard(board).forEach(move -> {
            evaluatedPositions++;

            board.doMove(move, false);
            seenPositions.add(board.getFen().split(" " )[0]);

            Node newNode = new Node(board, move);
            node.addChild(move, newNode);
            constructTree(newNode, remainingDepth - 1);

            board.undoMove();
        });

        if (node.hasChildren()) {
            node.setScore(node.getBestChild().getScore());
        } else {
            node.setScore(BoardScorer.scoreBoard(board));
        }
    }

    private void constructTreeKeepCapturing(Node node, int remainingDepth, int remainingMaxDepth) {
        if (remainingMaxDepth == 0) {
            node.setScore(BoardScorer.scoreBoard(board));
            return;
        }

        for (Move move :  BoardMoveCache.getMovesForBoard(board)) {
            if (remainingDepth <= 0 && !Utils.isCapture(board, move)) {
                continue;
            }

            evaluatedPositions++;

            board.doMove(move, false);

            Node newNode = new Node(board, move);
            node.addChild(move, newNode);
            constructTreeKeepCapturing(newNode, remainingDepth - 1, remainingMaxDepth - 1);

            board.undoMove();
        }

        if (node.hasChildren()) {
            node.setScore(node.getBestChild().getScore());
        } else {
            node.setScore(BoardScorer.scoreBoard(board));
        }
    }
}
