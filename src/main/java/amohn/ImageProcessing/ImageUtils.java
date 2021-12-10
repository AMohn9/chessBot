package amohn.ImageProcessing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class ImageUtils {

  private static final int BOARD_TOP_LEFT_X = 276;
  private static final int BOARD_TOP_LEFT_Y = 200;
  private static final int BOARD_WIDTH = 768;
  private static final int BOARD_HEIGHT = 768;

  static void saveImage(Mat image, String filename) {
    Imgcodecs.imwrite(filename, image);
  }

  static Mat getScreenshot() {
    try {
      Rectangle screenRect = new Rectangle(BOARD_TOP_LEFT_X, BOARD_TOP_LEFT_Y, BOARD_WIDTH, BOARD_HEIGHT);
      BufferedImage capture = new Robot().createScreenCapture(screenRect);
      return ImageUtils.matify(capture);
    } catch (AWTException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static Mat matify(BufferedImage sourceImg) {

    DataBuffer dataBuffer = sourceImg.getRaster().getDataBuffer();
    byte[] imgPixels = null;
    Mat imgMat = null;

    int width = sourceImg.getWidth();
    int height = sourceImg.getHeight();

    if(dataBuffer instanceof DataBufferByte) {
      imgPixels = ((DataBufferByte)dataBuffer).getData();
    }

    if(dataBuffer instanceof DataBufferInt) {

      int byteSize = width * height;
      imgPixels = new byte[byteSize*3];

      int[] imgIntegerPixels = ((DataBufferInt)dataBuffer).getData();

      for(int p = 0; p < byteSize; p++) {
        imgPixels[p*3 + 0] = (byte) ((imgIntegerPixels[p] & 0x00FF0000) >> 16);
        imgPixels[p*3 + 1] = (byte) ((imgIntegerPixels[p] & 0x0000FF00) >> 8);
        imgPixels[p*3 + 2] = (byte) (imgIntegerPixels[p] & 0x000000FF);
      }
    }

    if(imgPixels != null) {
      imgMat = new Mat(height, width, CvType.CV_8UC3);
      imgMat.put(0, 0, imgPixels);
    }

    return imgMat;
  }

  static Mat findEdges(Mat img) {
    Mat grayImage = new Mat();
    Mat detectedEdges = new Mat();

    Imgproc.cvtColor(img, grayImage, Imgproc.COLOR_BGR2GRAY);
    Imgproc.blur(grayImage, detectedEdges, new Size(2, 2));

    int threshold = 3;
    Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3);

    Mat dest = new Mat();
    grayImage.copyTo(dest, detectedEdges);

    return dest;
  }

}
