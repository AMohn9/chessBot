package amohn;

import amohn.chess.search.AlphaBeta;
import amohn.chess.search.MiniMax;
import amohn.chess.search.Node;
import amohn.chess.search.SearchEngine;
import amohn.imageprocessing.BoardImageAnalyzer;
import amohn.chess.evaluation.BoardScorer;
import com.github.bhlangonijr.chesslib.Board;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

    private static final int DEPTH = 6;

    public static void main(String[] args ) {
        run();
    }

    private static void run() {
        BoardImageAnalyzer boardImageAnalyzer = new BoardImageAnalyzer();

        long millis = System.currentTimeMillis();
        Board board = boardImageAnalyzer.getBoardFromImage();
        log.info("Board process millis: " + (System.currentTimeMillis() - millis));

        log.info(board.getFen());
        log.info("Board score: " + BoardScorer.scoreBoard(board));
        log.info("\n");

//        SearchEngine engine = new MiniMax(board);
//        engine.constructTree(DEPTH);

        SearchEngine engine = new AlphaBeta(board);
        engine.constructTree(DEPTH);

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
    }
}
