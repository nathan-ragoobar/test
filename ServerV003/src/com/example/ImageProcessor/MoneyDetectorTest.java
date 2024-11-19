package com.example.ImageProcessor;

//import MoneyDetector005.MoneyDetectorMainV5;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class MoneyDetectorTest {
    public static void main(String[] args) {
        // Load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("OpenCV library loaded successfully");

        // Path to the test image
        String filePath = "C:\\Users\\ticky\\IdeaProjects\\ServerV003\\src\\dollar\\image.jpg";

        // Read the image
        Mat inputDollar = Imgcodecs.imread(filePath);
        if (inputDollar.empty()) {
            System.out.println("Failed to load image");
            return;
        }

        // Create an instance of MoneyDetectorMainV5
        MoneyDetectorMainV5 detector = new MoneyDetectorMainV5();

        // Call the main method with the image file path
        String result = detector.main(filePath);

        // Print the result
        System.out.println(result);
    }
}