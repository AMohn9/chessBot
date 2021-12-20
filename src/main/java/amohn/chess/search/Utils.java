package amohn.chess.search;

import amohn.chess.evaluation.BoardScorer;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Utils {

    private static final Map<PieceType, Integer> PIECE_SCORES = Map.of(
            PieceType.PAWN, 100,
            PieceType.KNIGHT, 320,
            PieceType.BISHOP, 330,
            PieceType.ROOK, 500,
            PieceType.QUEEN, 900,
            PieceType.KING, 20000,
            PieceType.NONE, 0
    );

    @Value
    private static class Capture {
        Move move;
        PieceType capturer;
        PieceType captured;

        int scoreCapture() {
            return PIECE_SCORES.get(captured) - PIECE_SCORES.get(capturer);
        }
    }


    static boolean isCapture(Board board, Move move) {
        return ! board.getPiece(move.getTo()).equals(Piece.NONE);
    }

    static boolean checkIsMoveCheck(Board board, Move move) {
        board.doMove(move);
        board.doNullMove();
        boolean isCheck = board.isKingAttacked();
        board.undoMove();
        board.undoMove();
        return isCheck;
    }

    static double scoreMove(Board board, Move move) {
        board.doMove(move);
        double score = BoardScorer.scoreBoard(board);
        board.undoMove();
        return score;
    }

    public static List<Move> getOrderedMoves(Board board) {
        List<Move> moves = board.legalMoves();

        // First pawn captures, then other captures, then non-captures
        List<Move> pawnCaptures = new ArrayList<>();
        List<Move> otherCaptures = new ArrayList<>();
        List<Move> nonCaptures = new ArrayList<>();

        for (Move move : moves) {
            try {
                if (isCapture(board, move)) {
                    if (board.getPiece(move.getFrom()).getPieceType().equals(PieceType.PAWN)) {
                        pawnCaptures.add(move);
                    } else {
                        otherCaptures.add(move);
                    }
                } else {
                    nonCaptures.add(move);
                }
            } catch (Exception e) {
                log.error(e.toString());
                log.error("Failed to process move: " + move);
                log.error(String.valueOf(board));
                log.error(board.getFen());
            }
        }

        List<Move> ret = new ArrayList<>(moves.size());
        ret.addAll(pawnCaptures);
        ret.addAll(otherCaptures);
        ret.addAll(nonCaptures);

        return ret;
    }

    public static List<Move> getOrderedMoves2(Board board) {
        List<Move> moves = board.legalMoves();

        // First checks, then captures ordered by piece score difference, then everything else
        List<Move> checks = new ArrayList<>();
        List<Capture> captures = new ArrayList<>();
        List<Move> nonCaptures = new ArrayList<>();

        for (Move move : moves) {
            try {
                if (isCapture(board, move)){
                    captures.add(new Capture(move, board.getPiece(move.getFrom()).getPieceType(), board.getPiece(move.getTo()).getPieceType()));
                } else {
                    nonCaptures.add(move);
                }

//                if (checkIsMoveCheck(board, move)) {
//                    checks.add(move);
//                } else if (isCapture(board, move)){
//                    captures.add(new Capture(move, board.getPiece(move.getFrom()).getPieceType(), board.getPiece(move.getTo()).getPieceType()));
//                } else {
//                    nonCaptures.add(move);
//                }
            } catch (Exception e) {
                log.error(e.toString());
                log.error("Failed to process move: " + move);
                log.error(String.valueOf(board));
                log.error(board.getFen());
            }
        }

        List<Move> ret = new ArrayList<>(moves.size());
        ret.addAll(checks);
        // Sort captures
        ret.addAll(
                captures.stream()
                        .sorted(Comparator.comparing(Capture::scoreCapture).reversed())
                        .map(Capture::getMove)
                        .collect(Collectors.toList())
        );
        ret.addAll(nonCaptures);

        return ret;
    }

    public static List<Move> getOrderedMoves3(Board board) {
        if (board.getSideToMove().equals(Side.WHITE)) {
            return board.legalMoves().stream()
                    .sorted(Comparator.comparing(m -> scoreMove(board, (Move) m)).reversed())
                    .collect(Collectors.toList());
        } else {
            return board.legalMoves().stream()
                    .sorted(Comparator.comparing(m -> scoreMove(board, m)))
                    .collect(Collectors.toList());
        }
    }

    public static List<Move> getOrderedMoves4(Board board) {
        List<Move> moves = board.legalMoves();

        // First free captures, then pawn captures, then other captures, then non-captures
        List<Move> freeCaptures = new ArrayList<>();
        List<Move> pawnCaptures = new ArrayList<>();
        List<Move> otherCaptures = new ArrayList<>();
        List<Move> nonCaptures = new ArrayList<>();

        for (Move move : moves) {
            try {
                if (isCapture(board, move)) {
                    if (board.squareAttackedBy(move.getTo(), board.getSideToMove().flip()) == 0L) {
                        freeCaptures.add(move);
                    } else if (board.getPiece(move.getFrom()).getPieceType().equals(PieceType.PAWN)) {
                        pawnCaptures.add(move);
                    } else {
                        otherCaptures.add(move);
                    }
                } else {
                    nonCaptures.add(move);
                }
            } catch (Exception e) {
                log.error(e.toString());
                log.error("Failed to process move: " + move);
                log.error(String.valueOf(board));
                log.error(board.getFen());
            }
        }

        List<Move> ret = new ArrayList<>(moves.size());
        ret.addAll(freeCaptures);
        ret.addAll(pawnCaptures);
        ret.addAll(otherCaptures);
        ret.addAll(nonCaptures);

        return ret;
    }
}
