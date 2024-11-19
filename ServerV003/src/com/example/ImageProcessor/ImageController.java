package com.example.ImageProcessor;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageController {

    static {
        // Set the path to the OpenCV native library
        System.setProperty("java.library.path", "C:\\Users\\ticky\\opencv\\build\\java\\x64\\opencv_java4100.dll");
        // Load the OpenCV native library
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    private static final String TEMP_FILE_PATH = "C:\\Users\\ticky\\IdeaProjects\\ServerV003\\src\\dollar\\image.jpg";

    @PostMapping("/process-image")
    public ResponseEntity<Map<String, String>> processImage(@RequestParam("image") MultipartFile image) {
        Map<String, String> response = new HashMap<>();
        File tempFile = new File(TEMP_FILE_PATH);
        try {
            // Save the uploaded image to a temporary file
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(image.getBytes());
            }

            // Read the image using OpenCV
            Mat originalImage = Imgcodecs.imread(tempFile.getAbsolutePath());

            // Check if the image was loaded successfully
            if (originalImage.empty()) {
                throw new IOException("Failed to load the image");
            }

            // Enhance the resolution by resizing the image
            Mat enhancedImage = new Mat();
            Size newSize = new Size(originalImage.cols() * 2, originalImage.rows() * 2); // Double the resolution
            Imgproc.resize(originalImage, enhancedImage, newSize, 0, 0, Imgproc.INTER_CUBIC);

            // Save the enhanced image to the specified file path
            Imgcodecs.imwrite(TEMP_FILE_PATH, enhancedImage);

            // Process the image using the ImageProcessor logic
            String result = ImageProcessor.detectDollarValue(TEMP_FILE_PATH);
            response.put("result", result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            response.put("error", "Error processing image: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            response.put("error", "Unexpected error: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

