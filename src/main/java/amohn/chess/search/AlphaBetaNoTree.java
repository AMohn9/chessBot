package amohn.chess.search;

import amohn.chess.evaluation.BoardScorer;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AlphaBetaNoTree {

    private final Board board;

    @Getter
    private final MiniNode root;

    @Getter
    private int evaluatedPositions = 0;

    @Getter
//    public Map<String, Double> seenPositions = new HashMap<>();
    public Map<Long, Double> seenPositions = new HashMap<>();

    public AlphaBetaNoTree(Board board) {
        this.board = board;
        this.root = new MiniNode(null, BoardScorer.scoreBoard(board));
    }

    public void constructTree(int depth) {
        constructTree(root, depth, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public void constructTreeKeepCapturing(int minDepth, int maxDepth) {
        constructTreeKeepCapturing(root, minDepth, maxDepth, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    private double constructTree(MiniNode node, int remainingDepth, double alpha, double beta) {
        if (remainingDepth == 0) {
            double nodeScore = BoardScorer.scoreBoard(board);
            node.setScore(nodeScore);
            return nodeScore;
        }

        Side sideToPlay = board.getSideToMove();

        MiniNode bestChild = null;
        double value = sideToPlay.equals(Side.WHITE) ? Double.MIN_VALUE : Double.MAX_VALUE;

        for (Move move : Utils.getOrderedMoves(board)) {
//        for (Move move : board.legalMoves()) {
            evaluatedPositions++;

            board.doMove(move, false);

            MiniNode newNode = new MiniNode(move);
            double newNodeScore;
            long key = board.getZobristKey();
            if (seenPositions.containsKey(key)) {
                newNodeScore = seenPositions.get(key);
            } else {
                newNodeScore = constructTree(newNode, remainingDepth - 1, alpha, beta);
                seenPositions.put(key, newNodeScore);
            }

            if (sideToPlay.equals(Side.WHITE)) {
                if (newNodeScore > value) {
                    bestChild = newNode;
                    value = newNodeScore;
                }

                if (value >= beta) {
                    board.undoMove();
                    break;
                }
                alpha = Math.max(alpha, value);
            } else {
                if (newNodeScore < value) {
                    bestChild = newNode;
                    value = newNodeScore;
                }

                if (value <= alpha) {
                    board.undoMove();
                    break;
                }
                beta = Math.min(beta, value);
            }

            board.undoMove();
        }

        node.setScore(value);
        node.setBestChild(bestChild);
        return value;
    }

    private double constructTreeKeepCapturing(MiniNode node, int remainingDepth, int remainingMaxDepth, double alpha, double beta) {
        if (remainingMaxDepth == 0) {
            double nodeScore = BoardScorer.scoreBoard(board);
            node.setScore(nodeScore);
            return nodeScore;
        }

        Side sideToPlay = board.getSideToMove();

        MiniNode bestChild = null;
        double value = sideToPlay.equals(Side.WHITE) ? Double.MIN_VALUE : Double.MAX_VALUE;

//        for (Move move : Utils.getOrderedMoves(board)) {
        for (Move move : Utils.getOrderedMoves(board)) {
            if (remainingDepth <= 0 && !Utils.isCapture(board, move)) {
                continue;
            }

            evaluatedPositions++;

            board.doMove(move, false);

            MiniNode newNode = new MiniNode(move);
            double newNodeScore;
//            newNodeScore = constructTreeKeepCapturing(newNode, remainingDepth - 1, remainingMaxDepth - 1, alpha, beta);
            long key = board.getZobristKey();
            if (seenPositions.containsKey(key)) {
                newNodeScore = seenPositions.get(key);
            } else {
                newNodeScore = constructTreeKeepCapturing(newNode, remainingDepth - 1, remainingMaxDepth - 1, alpha, beta);
                seenPositions.put(key, newNodeScore);
            }

            if (sideToPlay.equals(Side.WHITE)) {
                if (newNodeScore > value) {
                    bestChild = newNode;
                    value = newNodeScore;
                }

                if (value >= beta) {
                    board.undoMove();
                    break;
                }
                alpha = Math.max(alpha, value);
            } else {
                if (newNodeScore < value) {
                    bestChild = newNode;
                    value = newNodeScore;
                }

                if (value <= alpha) {
                    board.undoMove();
                    break;
                }
                beta = Math.min(beta, value);
            }

            board.undoMove();
        }

        if (bestChild != null) {
            node.setScore(value);
            node.setBestChild(bestChild);
            return value;
        } else {
            double nodeScore = BoardScorer.scoreBoard(board);
            node.setScore(nodeScore);
            return nodeScore;
        }
    }
}
