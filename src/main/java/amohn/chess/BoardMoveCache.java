package amohn.chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.List;

public class BoardMoveCache {
    private static final LoadingCache<Board, List<Move>> memo = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(CacheLoader.from(Board::legalMoves));

    public static List<Move> getMovesForBoard(Board board) {
        return memo.getUnchecked(board);
    }
}
