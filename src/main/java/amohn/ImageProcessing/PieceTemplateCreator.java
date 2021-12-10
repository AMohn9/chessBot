package amohn.ImageProcessing;

import amohn.chess.PieceOld;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class PieceTemplateCreator {

  public static void main(String[] args ) {
    OpenCV.loadShared();
    createPieceTemplates();
  }

  private static void createPieceTemplates() {
    // Get a screenshot and extract it's edges
    Mat screenshot = ImageUtils.getScreenshot();
    ImageUtils.saveImage(screenshot, "out/screenshot.jpg");
    Mat processedScreenshot = ImageUtils.findEdges(screenshot);
    ImageUtils.saveImage(processedScreenshot, "out/processed_screenshot.jpg");

    int squareSize = (int) processedScreenshot.size().height / 8;

    Map<PieceOld.PieceType, List<Integer>> initPieceLocations = getInitPieceLocations();

    // Extract the image of the cell for each piece type
    for (PieceOld.PieceType pieceType : PieceOld.PieceType.values()) {
      List<Integer> initLocation = initPieceLocations.get(pieceType);
      Rect rectCrop = new Rect(squareSize * initLocation.get(0), squareSize * initLocation.get(1), squareSize, squareSize);
      Mat cellImage = new Mat(processedScreenshot, rectCrop);

      // Crop the edges to avoid imperfect captures
      cellImage = cellImage.submat(5, cellImage.rows() - 5, 5, cellImage.cols() - 5);
      ImageUtils.saveImage(cellImage, String.format("out/%s.jpg", pieceType.name()));
    }
  }

  private static Map<PieceOld.PieceType, List<Integer>> getInitPieceLocations() {
    // An initial place for each piece type
    Map<PieceOld.PieceType, List<Integer>> ret = new HashMap<>();
    ret.put(PieceOld.PieceType.ROOK, List.of(0, 0));
    ret.put(PieceOld.PieceType.KNIGHT, List.of(1, 0));
    ret.put(PieceOld.PieceType.BISHOP, List.of(2, 0));
    ret.put(PieceOld.PieceType.QUEEN, List.of(3, 0));
    ret.put(PieceOld.PieceType.KING, List.of(4, 0));
    ret.put(PieceOld.PieceType.PAWN, List.of(0, 1));
    return ret;
  }
}
