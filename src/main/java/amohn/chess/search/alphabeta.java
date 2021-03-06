package amohn.chess.search;

import amohn.chess.BoardMoveCache;
import amohn.chess.evaluation.BoardScorer;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class AlphaBeta implements SearchEngine {
    private final Board board;

    @Getter
    private final Node root;

    @Getter
    private int evaluatedPositions = 0;

    @Getter
    public Set<String> seenPositions = new HashSet<>();

    public AlphaBeta(Board board) {
        this.board = board;
        this.root = new Node(board, null);
        root.setScore(BoardScorer.scoreBoard(board));
    }

    public void constructTree(int depth) {
        constructTree(root, depth, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public void constructTreeKeepCapturing(int minDepth, int maxDepth) {
        constructTreeKeepCapturing(root, minDepth, maxDepth, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    private double constructTree(Node node, int remainingDepth, double alpha, double beta) {
        if (remainingDepth == 0) {
            double nodeScore = BoardScorer.scoreBoard(board);
            node.setScore(nodeScore);
            return nodeScore;
        }

        Side sideToPlay = board.getSideToMove();

        double value = sideToPlay.equals(Side.WHITE) ? Double.MIN_VALUE : Double.MAX_VALUE;

        for (Move move : Utils.getOrderedMoves(board)) {
//        for (Move move : board.legalMoves()) {
            evaluatedPositions++;

            board.doMove(move, false);
            seenPositions.add(board.getFen().split(" " )[0]);

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

    private double constructTreeKeepCapturing(Node node, int remainingDepth, int remainingMaxDepth, double alpha, double beta) {
        if (remainingMaxDepth == 0) {
            double nodeScore = BoardScorer.scoreBoard(board);
            node.setScore(nodeScore);
            return nodeScore;
        }

        Side sideToPlay = board.getSideToMove();

        double value = sideToPlay.equals(Side.WHITE) ? Double.MIN_VALUE : Double.MAX_VALUE;

        for (Move move : BoardMoveCache.getMovesForBoard(board)) {
            if (remainingDepth <= 0 && !Utils.isCapture(board, move)) {
                continue;
            }

            evaluatedPositions++;

            board.doMove(move, false);

            Node newNode = new Node(board, move);
            node.addChild(move, newNode);
            double newNodeScore = constructTreeKeepCapturing(newNode, remainingDepth - 1, remainingMaxDepth - 1, alpha, beta);

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

        if (node.hasChildren()) {
            node.setScore(value);
            return value;
        } else {
            double nodeScore = BoardScorer.scoreBoard(board);
            node.setScore(nodeScore);
            return nodeScore;
        }
    }
}
