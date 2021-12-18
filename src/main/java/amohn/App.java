package amohn;

import amohn.imageprocessing.BoardImageAnalyzer;
import amohn.chess.evaluation.BoardScorer;
import com.github.bhlangonijr.chesslib.Board;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

    public static void main(String[] args ) {
        computerVsComputer();
//        scoreImage();
    }

    private static void computerVsComputer() {
        Board board = new Board();
        log.info("Moves: " + board.legalMoves());
    }

    private static void scoreImage() {
        BoardImageAnalyzer boardImageAnalyzer = new BoardImageAnalyzer();

        long millis = System.currentTimeMillis();
        Board board = boardImageAnalyzer.getBoardFromImage();
        log.debug("Board process millis: " + (System.currentTimeMillis() - millis));

        log.info("Board score: " + BoardScorer.scoreBoard(board));
    }
}
