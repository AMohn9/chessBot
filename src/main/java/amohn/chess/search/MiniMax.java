package amohn.chess.search;

import amohn.chess.BoardMoveCache;
import amohn.chess.evaluation.BoardScorer;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MiniMax implements SearchEngine {
    private final Board board;

    @Getter
    private final Node root;

    @Getter
    private int evaluatedPositions = 0;

    public MiniMax (Board board) {
        this.board = board;
        this.root = new Node(board, null);
        root.setScore(BoardScorer.scoreBoard(board));
    }

    public void constructTree(int depth) {
        constructTree(root, depth);
    }

    private void constructTree(Node node, int remainingDepth) {
        if (remainingDepth == 0) {
            node.setScore(BoardScorer.scoreBoard(board));
            return;
        }

        BoardMoveCache.getMovesForBoard(board).forEach(move -> {
            evaluatedPositions++;

            board.doMove(move, false);

            Node newNode = new Node(board, move);
            node.addChild(move, newNode);
            constructTree(newNode, remainingDepth - 1);

            board.undoMove();
        });

        node.setScore(node.getBestChild().getScore());
    }

    private boolean isCapture(Board board, Move move) {
        return ! board.getPiece(move.getTo()).getPieceType().equals(PieceType.NONE);
    }
}
