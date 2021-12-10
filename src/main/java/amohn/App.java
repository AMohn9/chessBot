package amohn;

import amohn.ImageProcessing.BoardImageAnalyzer;

public class App {

    public static void main(String[] args ) {
        BoardImageAnalyzer boardImageAnalyzer = new BoardImageAnalyzer();

        for (int i = 0; i < 10; i++) {
            boardImageAnalyzer.processBoardImage();
        }

        long millis = System.currentTimeMillis();
        boardImageAnalyzer.processBoardImage();
        System.out.println("Board process millis: " + (System.currentTimeMillis() - millis));
    }
}
