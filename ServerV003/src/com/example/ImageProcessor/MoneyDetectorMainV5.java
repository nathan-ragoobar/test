package com.example.ImageProcessor;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.*;

public class MoneyDetectorMainV5 {
    static {
        // Set the path to the OpenCV native library
        System.setProperty("java.library.path", "C:\\Users\\ticky\\IdeaProjects\\ServerV003\\opencv\\build\\java\\x64\\opencv_java4100.dll");
        // Load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Mat WarpDollar(Mat inputDollar) {

        Imgproc.resize(inputDollar, inputDollar, new Size(), 2.5,1.25 );
        // Converting image to gray scale
        Mat GrayImage = new Mat(inputDollar.rows(), inputDollar.cols(), inputDollar.type());
        Mat EdgedImage = new Mat(inputDollar.rows(), inputDollar.cols(), inputDollar.type());
        Imgproc.cvtColor(inputDollar, GrayImage, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(GrayImage, EdgedImage, new Size(3, 3), 0);
        Imgproc.Canny(EdgedImage, EdgedImage, 100, 100 * 3);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Mat imgDilated = new Mat();
        Mat imgEroded = new Mat();
        Imgproc.dilate(EdgedImage, imgDilated, kernel, new Point(-1, -1), 2);
        Imgproc.erode(imgDilated, imgEroded, kernel, new Point(-1, -1), 1);
        Mat imgContours = inputDollar.clone();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(imgEroded, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(imgContours, contours, -1, new Scalar(0, 255, 0), 3);
        MatOfPoint2f m2f = new MatOfPoint2f(contours.get(0).toArray());
        double arc = Imgproc.arcLength(m2f, true);
        MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(m2f, approx, arc * 0.02, true);
        Moments moment = Imgproc.moments(approx);
        int x = (int) (moment.get_m10() / moment.get_m00());
        int y = (int) (moment.get_m01() / moment.get_m00());
        Point[] sortedPoints = new Point[4];
        double[] data;
        for (int i = 0; i < approx.rows(); i++) {
            data = approx.get(i, 0);
            double datax = data[0];
            double datay = data[1];
            if (datax < x && datay < y) {
                sortedPoints[0] = new Point(datax, datay);
            } else if (datax > x && datay < y) {
                sortedPoints[1] = new Point(datax, datay);
            } else if (datax < x && datay > y) {
                sortedPoints[2] = new Point(datax, datay);
            } else if (datax > x && datay > y) {
                sortedPoints[3] = new Point(datax, datay);
            }
        }
        MatOfPoint2f src = new MatOfPoint2f(
                sortedPoints[0],
                sortedPoints[1],
                sortedPoints[2],
                sortedPoints[3]
        );
        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(1000 - 1, 0),
                new Point(0, 300 - 1),
                new Point(1000 - 1, 300 - 1)
        );
        Mat warpMat = Imgproc.getPerspectiveTransform(src, dst);
        Mat destImage = new Mat();
        Imgproc.warpPerspective(imgContours, destImage, warpMat, imgContours.size());
        Mat AlteredWarpPerspective = new Mat(destImage.rows(), destImage.cols(), destImage.type());
        destImage.convertTo(AlteredWarpPerspective, -1, 1, -7);
       // HighGui.imshow("AlteredWarpPerspective", AlteredWarpPerspective);
        return AlteredWarpPerspective;
    }

    public static Mat ROI_FLAG(Mat AlteredWarpPerspective) {
        Rect box = new Rect(730, 25, 260, 100);
        Mat ROI = new Mat(AlteredWarpPerspective, box);
        Mat HSV_ROI = new Mat();
        Imgproc.cvtColor(ROI, HSV_ROI, Imgproc.COLOR_BGR2HSV);
        Scalar lowerRed1 = new Scalar(0, 100, 100);
        Scalar UpperRed1 = new Scalar(10, 255, 255);
        Scalar lowerRed2 = new Scalar(160, 100, 100);
        Scalar UpperRed2 = new Scalar(180, 255, 255);
        Scalar lowerBlack = new Scalar(0, 0, 0);
        Scalar UpperBlack = new Scalar(180, 255, 50);
        Mat mask1 = new Mat();
        Mat mask2 = new Mat();
        Mat mask3 = new Mat();
        Core.inRange(HSV_ROI, lowerRed1, UpperRed1, mask1);
        Core.inRange(HSV_ROI, lowerRed2, UpperRed2, mask2);
        Core.inRange(HSV_ROI, lowerBlack, UpperBlack, mask3);
        Mat redMask = new Mat();
        Core.add(mask1, mask2, redMask);
        Mat AllMasks = new Mat();
        Core.add(redMask, mask3, AllMasks);
        return AllMasks;
    }

    public static Mat ROI_NUM(Mat AlteredWarpPerspective) {
        Rect box = new Rect(770, 60, 200, 140);
        Mat ROI = new Mat(AlteredWarpPerspective, box);
        Mat binaryROI = new Mat();
        Mat GrayROI = new Mat(ROI.rows(), ROI.cols(), ROI.type());
        Imgproc.cvtColor(ROI, GrayROI, Imgproc.COLOR_RGB2GRAY);
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(1);
        clahe.apply(GrayROI, GrayROI);
        Imgproc.threshold(GrayROI, binaryROI, 130, 1000, Imgproc.THRESH_BINARY);
        return binaryROI;
    }

    public static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage BufferedROI = new BufferedImage(mat.cols(), mat.rows(), type);
        mat.get(0, 0, ((DataBufferByte) BufferedROI.getRaster().getDataBuffer()).getData());
        return BufferedROI;
    }

    public static String doWarpOCR(BufferedImage BufferedROI) {
        ITesseract instance = new Tesseract();

        //Reading the dollarValue
        String dollarValue = null;
        String Voice = null;
        try {

            instance.setDatapath("C:\\Users\\ticky\\IdeaProjects\\ServerV003\\tessdata");
            instance.setTessVariable("tessedit_char_whitelist", "0125");

            dollarValue = instance.doOCR(BufferedROI).trim();

            System.out.println(new StringBuilder().append("Recognized digits: ").append(dollarValue).toString());
            String oneDollar = "1";
            String fiveDollar = "5";
            String tenDollar = "10";
            String twentyDollar = "20";
            String fiftyDollar = "50";
            String hundredDollar = "100";

            if (!dollarValue.equals(oneDollar) &&
                    !dollarValue.equals(fiveDollar) &&
                    !dollarValue.equals(tenDollar) &&
                    !dollarValue.equals(twentyDollar) &&
                    !dollarValue.equals(fiftyDollar) &&
                    !dollarValue.equals(hundredDollar)) {
                Voice = "No valid value detected";
            }
            else if (dollarValue.equals(oneDollar)) {
                Voice = "One dollar";
            }
            else if (dollarValue.equals(fiveDollar)) {
                Voice = "Five dollars";
            }
            else if (dollarValue.equals(tenDollar)) {
                Voice = "Ten dollars";
            }
            else if (dollarValue.equals(twentyDollar)) {
                Voice = "Twenty dollars";
            }
            else if (dollarValue.equals(fiftyDollar)) {
                Voice = "Fifty dollars";
            }
            else if (dollarValue.equals(hundredDollar)) {
                Voice = "One hundred dollars";
            }





        } catch (TesseractException e) {
            e.printStackTrace();
        }


        return Voice;
    }

    public static String toVoice (String Voice, Mat AllMasks) {

        String dollarOrientation = null;
        if (AllMasks.empty()) {
            dollarOrientation = "Dollar may be upside down or flipped over";

        } else {
            System.out.println(" Flag Detected");
            System.out.println(Voice);

        }


        return dollarOrientation + Voice;
    }

    public String main(String filename) {
        File imgFile = new File(filename);

        if (!imgFile.exists()) {
            throw new IllegalArgumentException("File not found: " + filename);
        }
        Mat inputDollar = Imgcodecs.imread(filename);
        if (inputDollar.empty()) {
            throw new IllegalArgumentException("Cannot read the image file: " + filename);
        }
        Mat Warp = WarpDollar(inputDollar);
        Mat ROI_Number = ROI_NUM(Warp);
        BufferedImage BuffWarp = matToBufferedImage(ROI_Number);
        String DollarValue = doWarpOCR(BuffWarp);
        Mat Flag = ROI_FLAG(Warp);
        toVoice(DollarValue,Flag);
        return DollarValue;

    }
}


