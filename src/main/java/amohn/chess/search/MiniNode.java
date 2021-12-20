package amohn.chess.search;

import com.github.bhlangonijr.chesslib.move.Move;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MiniNode {
    final Move move;
    double score;
    MiniNode bestChild;

    public MiniNode(Move move) {
        this.move = move;
    }

    public MiniNode(Move move, double score) {
        this.move = move;
        this.score = score;
    }
}
