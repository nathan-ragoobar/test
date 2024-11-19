package com.example.ImageProcessor;

public class ImageProcessor {

    public static String detectDollarValue(String filePath) throws Exception {
        MoneyDetectorMainV5 detector = new MoneyDetectorMainV5();
        return detector.main(filePath);
        //return "The dollar value is 1000";
    }
}


