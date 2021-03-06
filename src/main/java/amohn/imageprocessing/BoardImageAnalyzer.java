package amohn.imageprocessing;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Slf4j
public class BoardImageAnalyzer {

  private final Map<PieceType, Mat> pieceTemplates;

  private static final boolean SCREENHOT_FROM_FILE = true;

  private static final ResourceLoader resourceLoader = new DefaultResourceLoader();

  public static void main(String[] args ) {
    String fen = new BoardImageAnalyzer().getFenFromImage();
    log.debug(fen);
    Board board = new Board();
    board.loadFromFen(fen);

    log.debug(String.format("There are %d legal moves", board.legalMoves().size()));

    for (Move move : board.legalMoves()) {
      log.debug(String.valueOf(move));
    }
  }

  public BoardImageAnalyzer() {
    OpenCV.loadLocally();
    pieceTemplates = getPieceTemplates();
  }

  public Board getBoardFromImage() {
    Board board = new Board();
    String fen = getFenFromImage();
    log.debug("FEN: " + fen);
    board.loadFromFen(fen);
    return board;
  }

  private String getFenFromImage() {
    Mat screenshot = getScreenshot();
    Mat processedScreenshot = ImageUtils.findEdges(screenshot);
    ImageUtils.saveImage(processedScreenshot, "out/processed_screenshot.jpg");

    int squareSize = (int) processedScreenshot.size().height / 8;

    StringBuilder fen = new StringBuilder();
    for (int y = 0; y < 8; y++) {
      int sinceLastPiece = 0;
      for (int x = 0; x < 8; x++) {
        Rect rectCrop = new Rect(squareSize * x, squareSize * y, squareSize, squareSize);
        Mat cellImage = new Mat(processedScreenshot, rectCrop);
        cellImage = cellImage.submat(5, cellImage.rows() - 5, 5, cellImage.cols() - 5);

        if (Core.countNonZero(cellImage) < 20) {
          log.debug(String.format("Place %d %d probably empty", x, y));
          sinceLastPiece++;
          continue;
        }

        PieceType pieceType = extractPieceType(cellImage, pieceTemplates);
        Side side = extractPieceColor(screenshot, squareSize, x, y);
        Piece piece = Piece.make(side, pieceType);

        log.debug(String.format("Place %d %d probably contains %s", x, y, piece));

        if (sinceLastPiece > 0) {
          fen.append(sinceLastPiece);
          sinceLastPiece = 0;
        }
        fen.append(piece.getFenSymbol());
      }
      if (sinceLastPiece > 0) {
        fen.append(sinceLastPiece);
      }
      if (y < 7) {
        fen.append("/");
      }
    }
//    fen += " w KQkq - 0 0";
    fen.append(" w - - 0 0");
    return fen.toString();
  }

  @SneakyThrows
  private static Mat getScreenshot() {
    if (SCREENHOT_FROM_FILE) {
      Resource resource = resourceLoader.getResource("classpath:example_screenshot.jpg");
      String imagePath = Paths.get(resource.getURI()).toFile().getAbsolutePath();
      return Imgcodecs.imread(imagePath);
    } else {
      Mat screenshot = ImageUtils.getScreenshot();
      ImageUtils.saveImage(screenshot, "out/screenshot.jpg");
      return screenshot;
    }
  }

  @SneakyThrows
  private static Map<PieceType, Mat> getPieceTemplates() {
    Map<PieceType, Mat> ret = new HashMap<>();

    for (PieceType pieceType : PieceType.values()) {
      if (pieceType.name().equals("NONE")) continue;

      Resource resource = resourceLoader.getResource("classpath:" + pieceType.name() + ".jpg");
      String imagePath = Paths.get(resource.getURI()).toFile().getAbsolutePath();

      Mat img = Imgcodecs.imread(imagePath);
      Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
      ret.put(pieceType, img);
    }
    return ret;
  }


  private PieceType extractPieceType(Mat cellImage, Map<PieceType, Mat> pieceTemplates) {
    double maxVal = 0;
    PieceType bestMatchedPiece = null;

    for (Map.Entry<PieceType, Mat> entry : pieceTemplates.entrySet()) {
      Mat outputImage = new Mat();
      int machMethod = Imgproc.TM_CCOEFF;
      Imgproc.matchTemplate(cellImage, entry.getValue(), outputImage, machMethod);
      Core.MinMaxLocResult mmr = Core.minMaxLoc(outputImage);

      if (mmr.maxVal > maxVal) {
        maxVal = mmr.maxVal;
        bestMatchedPiece = entry.getKey();
      }
    }

    return bestMatchedPiece;
  }

  private Side extractPieceColor(Mat screenshot, int squareSize, int x, int y) {
    // Extract the center 3rd of the cell
    Rect centerOfPeice = new Rect(squareSize * x + squareSize / 3, squareSize * y + squareSize / 3, squareSize / 3, squareSize / 3);
    Mat cellCenter = new Mat(screenshot, centerOfPeice);

    // Find the average pixel values of that region
    MatOfDouble meansrc = new MatOfDouble();
    MatOfDouble stdsrc = new MatOfDouble();
    Core.meanStdDev(cellCenter, meansrc, stdsrc);

    // Only care white or black, so get the average of colors too
    double totalMean = (meansrc.get(0,0)[0] + meansrc.get(1,0)[0] + meansrc.get(2,0)[0]) / 3;

    // Return if it's closer to white or black
    return totalMean > 125 ? Side.WHITE : Side.BLACK;
  }
}
