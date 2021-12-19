package amohn.chess.search;

import amohn.chess.BoardMoveCache;
import amohn.chess.evaluation.BoardScorer;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlphaBeta implements SearchEngine {
    private final Board board;

    @Getter
    private final Node root;

    @Getter
    private int evaluatedPositions = 0;

    public AlphaBeta(Board board) {
        this.board = board;
        this.root = new Node(board, null);
        root.setScore(BoardScorer.scoreBoard(board));
    }

    public void constructTree(int depth) {
        constructTree(root, depth, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    private double constructTree(Node node, int remainingDepth, double alpha, double beta) {
        if (remainingDepth == 0) {
            double nodeScore = BoardScorer.scoreBoard(board);
            node.setScore(nodeScore);
            return nodeScore;
        }

        Side sideToPlay = board.getSideToMove();

        double value = sideToPlay.equals(Side.WHITE) ? Double.MIN_VALUE : Double.MAX_VALUE;

        for (Move move : BoardMoveCache.getMovesForBoard(board)) {
            evaluatedPositions++;

            board.doMove(move, false);

            Node newNode = new Node(board, move);
            node.addChild(move, newNode);
            double newNodeScore = constructTree(newNode, remainingDepth - 1, alpha, beta);

            if (sideToPlay.equals(Side.WHITE)) {
                value = Math.max(value, newNodeScore);

                if (value >= beta) {
                    board.undoMove();
                    break;
                }
                alpha = Math.max(alpha, value);
            } else {
                value = Math.min(value, newNodeScore);

                if (value <= alpha) {
                    board.undoMove();
                    break;
                }
                beta = Math.min(beta, value);
            }

            board.undoMove();
        }

        node.setScore(value);
        return value;
    }
}
