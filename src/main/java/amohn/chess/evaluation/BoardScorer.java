package amohn.chess.evaluation;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BoardScorer {
  private static final LoadingCache<Board, Double> memo = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .build(CacheLoader.from(BoardScorer::computeScore));

  private static final Map<PieceType, Integer> PIECE_SCORES = Map.of(
      PieceType.PAWN, 100,
      PieceType.KNIGHT, 320,
      PieceType.BISHOP, 330,
      PieceType.ROOK, 500,
      PieceType.QUEEN, 900,
      PieceType.KING, 20000,
      PieceType.NONE, 0
  );

  private static final double MATERIAL_SCORE_COEF = 1;
  private static final double PIECE_LOCATION_SCORE_COEF = 1;
  private static final double MOBILITY_SCORE_COEF = .1;

  public static double scoreBoard(Board board) {
    return memo.getUnchecked(board);
  }

  private static double computeScore(Board board) {
//    if (board.isDraw()) {
//      return 0.0;
//    }

    if (board.isMated()) {
      return board.getSideToMove().equals(Side.WHITE) ? Double.MAX_VALUE : Double.MIN_VALUE;
    }

    double materialScore = MATERIAL_SCORE_COEF * getMaterialScore(board);
    log.debug("Material score: " + materialScore);
    double pieceLocationScore = PIECE_LOCATION_SCORE_COEF * getPieceLocationScore(board);
    log.debug("Piece location score: " + pieceLocationScore);
    double mobilityScore = MOBILITY_SCORE_COEF * getMobilityScore(board);
    log.debug("Mobility score: " + mobilityScore);

    return materialScore + pieceLocationScore + mobilityScore;
  }

  private static int totalPieceScore(Board board) {
    int totalScore = 0;

    for (Square square : Square.values()) {
      Piece piece = board.getPiece(square);

      if (piece.equals(Piece.NONE) || piece.getPieceType().equals(PieceType.KING)) {
        continue;
      }

      totalScore += PIECE_SCORES.get(piece.getPieceType());
    }
    return totalScore;
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

  public static int getPieceLocationScore(Board board) {
    boolean isEndGame = totalPieceScore(board) < 50;

    int whiteScore = 0;
    int blackScore = 0;
    for (Square square : Square.values()) {
      Piece piece = board.getPiece(square);

      if (piece.equals(Piece.NONE)) {
        continue;
      }

      List<Integer> positionTable;
      switch (piece.getPieceType()) {
        case PAWN: positionTable = PositionTables.PAWN_POSITION; break;
        case KNIGHT: positionTable = PositionTables.KNIGHT_PST; break;
        case BISHOP: positionTable = PositionTables.BISHOP_PST; break;
        case ROOK: positionTable = PositionTables.ROOK_PST; break;
        case QUEEN: positionTable = PositionTables.QUEEN_PST; break;
        case KING:
          positionTable = isEndGame ? PositionTables.KING_END_PST : PositionTables.KING_OPENING_PST;
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + piece.getPieceType());
      }

      if (piece.getPieceSide().equals(Side.WHITE)) {
        log.debug("Square " + square + " has piece " + piece + " for score " + positionTable.get(63 - square.ordinal()));
        whiteScore += positionTable.get(63 - square.ordinal());
      } else {
        log.debug("Square " + square + " has piece " + piece + " for score " + positionTable.get(square.ordinal()));
        blackScore += positionTable.get(square.ordinal());
      }
    }
    return whiteScore - blackScore;
  }

  private static int getMobilityScore(Board board) {

    int currentSideMoveCount = board.legalMoves().size();
    board.doNullMove();

    int otherSideMoveCount = board.legalMoves().size();
    board.undoMove();

    int countDifference = currentSideMoveCount - otherSideMoveCount;

    return countDifference * (board.getSideToMove().equals(Side.WHITE) ? 1 : -1);
  }
}
