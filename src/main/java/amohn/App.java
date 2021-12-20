package amohn;

import amohn.chess.search.*;
import amohn.imageprocessing.BoardImageAnalyzer;
import amohn.chess.evaluation.BoardScorer;
import com.github.bhlangonijr.chesslib.Board;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

    private static final int MIN_DEPTH = 6;
    private static final int MAX_DEPTH = 8;

    public static void main(String[] args ) {
//        BoardImageAnalyzer boardImageAnalyzer = new BoardImageAnalyzer();
//
//        long millis = System.currentTimeMillis();
//        Board board = boardImageAnalyzer.getBoardFromImage();
//        log.info("Board process millis: " + (System.currentTimeMillis() - millis));
//        log.info(board.getFen());

        Board board = new Board();
//        board.loadFromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
//        board.loadFromFen( "r3k2r/p1ppqpb1/Bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPB1PPP/R3K2R b KQkq - 0 1");
        board.loadFromFen( "r3k2r/p1ppqpb1/Bn2pnp1/4N3/1p2P3/2N2Q1p/PPPBPPPP/R3K2R b KQkq - 0 1");

//        run(board);
        runNoTree(board);

//        testMoveOrder();
    }

    private static void testMoveOrder() {
        Board board = new Board();
        board.loadFromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        log.info(String.valueOf(board.legalMoves()));
        log.info(String.valueOf(Utils.getOrderedMoves(board)));
        log.info(String.valueOf(Utils.getOrderedMoves2(board)));
        log.info(String.valueOf(Utils.getOrderedMoves3(board)));
    }

    private static void run(Board board) {
        long millis = System.currentTimeMillis();

        log.info("Board score: " + BoardScorer.scoreBoard(board));
        log.info("\n");

//        SearchEngine engine = new MiniMax(board);
//        engine.constructTree(MIN_DEPTH);
//        engine.constructTreeKeepCapturing(MIN_DEPTH, MAX_DEPTH);

        SearchEngine engine = new AlphaBeta(board);
        engine.constructTree(MIN_DEPTH);
//        engine.constructTreeKeepCapturing(MIN_DEPTH, MAX_DEPTH);

        Node bestMove = engine.getRoot().getBestChild();
        log.info("Best move: " + bestMove.getLastMove().toString());
        board.doMove(bestMove.getLastMove());
        log.info("Board after move:\n" + board);
        board.undoMove();
        log.info("Score after move: " + bestMove.getScore());
        log.info("\n");

        log.info("Continuation: ");
        while (bestMove.hasChildren()) {
            bestMove = bestMove.getBestChild();
            log.info(bestMove.getLastMove() + ": " + bestMove.getScore());
            log.info(board.getFen());
        }
        log.info("\n");

        log.info(String.format("Total runtime: %,d", (System.currentTimeMillis() - millis)));
        log.info(String.format("Evaluated positions: %,d", engine.getEvaluatedPositions()));
        log.info(String.format("Unique positions: %,d", engine.getSeenPositions().size()));
    }

    private static void runNoTree(Board board) {
        long millis = System.currentTimeMillis();

        log.info("Board score: " + BoardScorer.scoreBoard(board));
        log.info("\n");

        AlphaBetaNoTree engine = new AlphaBetaNoTree(board);
        engine.constructTree(MIN_DEPTH);
//        engine.constructTreeKeepCapturing(MIN_DEPTH, MAX_DEPTH);

        MiniNode bestMove = engine.getRoot().getBestChild();
        log.info("Best move: " + bestMove.getMove().toString());
        board.doMove(bestMove.getMove());
        log.info("Board after move:\n" + board);
        board.undoMove();
        log.info("Score after move: " + bestMove.getScore());
        log.info("\n");

        log.info("Continuation: ");
        while (bestMove.getBestChild() != null) {
            bestMove = bestMove.getBestChild();
            log.info(bestMove.getMove() + ": " + bestMove.getScore());
            log.info(board.getFen());
        }
        log.info("\n");

        log.info(String.format("Total runtime: %,d", (System.currentTimeMillis() - millis)));
        log.info(String.format("Evaluated positions: %,d", engine.getEvaluatedPositions()));
        log.info(String.format("Unique positions: %,d", engine.getSeenPositions().size()));
    }
}
