package amohn.chess.evaluation;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BoardScorer {
  private static LoadingCache<Board, Double> memo = CacheBuilder.newBuilder()
      .maximumSize(100000)
      .build(CacheLoader.from(BoardScorer::computeScore));

  private static final Map<PieceType, Integer> PIECE_SCORES = Map.of(
      PieceType.PAWN, 1,
      PieceType.KNIGHT, 3,
      PieceType.BISHOP, 3,
      PieceType.ROOK, 5,
      PieceType.QUEEN, 9,
      PieceType.KING, 0,
      PieceType.NONE, 0
  );

  private static final double MATERIAL_SCORE_COEF = 1;
  private static final double MOBILITY_SCORE_COEF = .1;

  public static double scoreBoard(Board board) {
    return memo.getUnchecked(board);
  }

  private static double computeScore(Board board) {
    if (board.isDraw() || board.isInsufficientMaterial() || board.isStaleMate()) {
      return 0.0;
    }
    if (board.isMated()) {
      return board.getSideToMove().equals(Side.WHITE) ? Double.MAX_VALUE : Double.MIN_VALUE;
    }


    double materialScore = MATERIAL_SCORE_COEF * getMaterialScore(board);
    log.debug("Material score: " + materialScore);
    double mobilityScore = MOBILITY_SCORE_COEF * getMobilityScore(board);
    log.debug("Mobility score: " + mobilityScore);

    return materialScore + mobilityScore;
  }

  private static int getMaterialScore(Board board) {
    int whiteScore = 0;
    int blackScore = 0;
    for (Square square : Square.values()) {
      Piece piece = board.getPiece(square);

      if (piece.equals(Piece.NONE)) {
        continue;
      }

      if (piece.getPieceSide() == Side.WHITE) {
        whiteScore += PIECE_SCORES.get(piece.getPieceType());
      } else {
        blackScore += PIECE_SCORES.get(piece.getPieceType());
      }
    }
    return whiteScore - blackScore;
  }

  private static int getMobilityScore(Board board) {

    Side side = board.getSideToMove();
    board.setSideToMove(Side.WHITE);
    int whiteMoveCount = board.legalMoves().size();
    log.debug("White moves: " + board.legalMoves());
    board.setSideToMove(Side.BLACK);
    int blackMoveCount = board.legalMoves().size();
    log.debug("Black moves: " + board.legalMoves());
    board.setSideToMove(side);

    return whiteMoveCount - blackMoveCount;
  }
}
