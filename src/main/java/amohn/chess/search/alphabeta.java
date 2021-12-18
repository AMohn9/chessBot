package amohn.chess.search;

import amohn.chess.evaluation.BoardScorer;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

public class alphabeta {

  private static final int DEPTH = 2;

  public static Move search(Board board) {

  }

  private static Move search(Board board, int depth) {
    if (depth == 0) {
      return BoardScorer.scoreBoard(board);
    }

  }
}
