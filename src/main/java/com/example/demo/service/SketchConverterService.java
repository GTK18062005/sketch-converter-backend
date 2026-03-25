package com.example.demo.service;

import com.example.demo.util.OpenCVUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class SketchConverterService {

    private final Path uploadDir;
    private final Path outputDir;
    
    public enum SketchStyle {
        PENCIL, WATERCOLOR, OIL_PAINTING, CHARCOAL, SKETCH_ART, VINTAGE
    }

    public SketchConverterService() throws IOException {
        // Check if running on Render
        boolean isRender = System.getenv("RENDER") != null;
        
        if (isRender) {
            uploadDir = Paths.get("/tmp/uploads");
            outputDir = Paths.get("/tmp/outputs");
            System.out.println("Running on Render - using /tmp for storage");
        } else {
            uploadDir = Paths.get("uploads");
            outputDir = Paths.get("outputs");
            System.out.println("Running locally - using project directory");
        }
        
        System.out.println("\n========================================");
        System.out.println("SketchConverterService Initialized");
        System.out.println("========================================");
        
        if (OpenCVUtil.isInitialized()) {
            System.out.println("✅ OpenCV Status: LOADED");
            System.out.println("✅ OpenCV Version: " + OpenCVUtil.getVersion());
        } else {
            System.out.println("⚠️ OpenCV Status: NOT LOADED");
        }
        
        Files.createDirectories(uploadDir);
        Files.createDirectories(outputDir);
        System.out.println("📁 Upload Directory: " + uploadDir.toAbsolutePath());
        System.out.println("📁 Output Directory: " + outputDir.toAbsolutePath());
        System.out.println("========================================\n");
    }

    public String convertToSketch(MultipartFile file) throws IOException {
        return convertToSketchWithStyle(file, SketchStyle.PENCIL);
    }
    
    public String convertToSketchWithStyle(MultipartFile file, SketchStyle style) throws IOException {
        if (!OpenCVUtil.isInitialized()) {
            throw new RuntimeException("OpenCV is not loaded. Please check installation.");
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String filename = UUID.randomUUID().toString() + extension;
        Path inputPath = uploadDir.resolve(filename);
        
        Files.write(inputPath, file.getBytes());
        System.out.println("💾 File saved: " + inputPath.getFileName());

        String sketchName = "sketch_" + style.name().toLowerCase() + "_" + filename;
        Path outputPath = outputDir.resolve(sketchName);

        convertImageToSketchEnhanced(inputPath.toString(), outputPath.toString(), style);

        Files.deleteIfExists(inputPath);
        System.out.println("🗑️ Cleaned up: " + inputPath.getFileName());

        return sketchName;
    }

    private void convertImageToSketchEnhanced(String input, String output, SketchStyle style) {
        Mat image = null;
        Mat result = null;
        
        try {
            System.out.println("📷 Reading image: " + input);
            image = Imgcodecs.imread(input);
            
            if (image.empty()) {
                throw new RuntimeException("Failed to load image: " + input);
            }
            
            System.out.println("📐 Image dimensions: " + image.width() + "x" + image.height());
            System.out.println("🎨 Applying " + style + " style...");

            switch (style) {
                case PENCIL:
                    result = createPencilSketch(image);
                    break;
                case WATERCOLOR:
                    result = createWatercolorEffect(image);
                    break;
                case OIL_PAINTING:
                    result = createOilPaintingEffect(image);
                    break;
                case CHARCOAL:
                    result = createCharcoalEffect(image);
                    break;
                case SKETCH_ART:
                    result = createArtisticSketch(image);
                    break;
                case VINTAGE:
                    result = createVintageEffect(image);
                    break;
                default:
                    result = createPencilSketch(image);
            }
            
            if (result == null || result.empty()) {
                throw new RuntimeException("Failed to create sketch - result is empty");
            }
            
            boolean success = Imgcodecs.imwrite(output, result);
            if (success) {
                System.out.println("💾 Artistic sketch saved: " + output);
            } else {
                throw new RuntimeException("Failed to save sketch");
            }
            
        } catch (Exception e) {
            System.err.println("❌ OpenCV error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to convert image: " + e.getMessage(), e);
        } finally {
            if (image != null) image.release();
            if (result != null) result.release();
        }
    }
    
    private Mat createPencilSketch(Mat color) {
        Mat gray = new Mat();
        Mat inverted = new Mat();
        Mat blurred = new Mat();
        Mat invertedBlur = new Mat();
        Mat sketch = new Mat();
        
        try {
            Imgproc.cvtColor(color, gray, Imgproc.COLOR_BGR2GRAY);
            Core.bitwise_not(gray, inverted);
            Imgproc.GaussianBlur(inverted, blurred, new Size(21, 21), 0);
            Core.bitwise_not(blurred, invertedBlur);
            Core.divide(gray, invertedBlur, sketch, 255);
            return sketch;
        } finally {
            gray.release();
            inverted.release();
            blurred.release();
            invertedBlur.release();
        }
    }
    
    private Mat createWatercolorEffect(Mat color) {
        Mat watercolor = new Mat();
        Mat result = new Mat();
        
        try {
            Imgproc.bilateralFilter(color, watercolor, 15, 80, 80);
            Imgproc.GaussianBlur(watercolor, result, new Size(5, 5), 0);
            return result;
        } finally {
            watercolor.release();
        }
    }
    
    private Mat createOilPaintingEffect(Mat color) {
        Mat oil = new Mat();
        
        try {
            Imgproc.medianBlur(color, oil, 7);
            Imgproc.bilateralFilter(oil, oil, 9, 75, 75);
            return oil;
        } finally {
            if (oil != null) oil.release();
        }
    }
    
    private Mat createCharcoalEffect(Mat color) {
        Mat gray = new Mat();
        Mat median = new Mat();
        Mat inverted = new Mat();
        Mat blurred = new Mat();
        Mat invertedBlur = new Mat();
        Mat sketch = new Mat();
        
        try {
            Imgproc.cvtColor(color, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.medianBlur(gray, median, 5);
            Core.bitwise_not(median, inverted);
            Imgproc.GaussianBlur(inverted, blurred, new Size(25, 25), 0);
            Core.bitwise_not(blurred, invertedBlur);
            Core.divide(median, invertedBlur, sketch, 255);
            return sketch;
        } finally {
            gray.release();
            median.release();
            inverted.release();
            blurred.release();
            invertedBlur.release();
        }
    }
    
    private Mat createArtisticSketch(Mat color) {
        Mat gray = new Mat();
        Mat inverted = new Mat();
        Mat blurred = new Mat();
        Mat invertedBlur = new Mat();
        Mat sketch = new Mat();
        Mat edges = new Mat();
        
        try {
            Imgproc.cvtColor(color, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.Canny(gray, edges, 50, 150);
            Core.bitwise_not(gray, inverted);
            Imgproc.GaussianBlur(inverted, blurred, new Size(15, 15), 0);
            Core.bitwise_not(blurred, invertedBlur);
            Core.divide(gray, invertedBlur, sketch, 255);
            
            Mat edgesInverted = new Mat();
            Core.bitwise_not(edges, edgesInverted);
            Mat result = new Mat();
            Core.addWeighted(sketch, 0.85, edgesInverted, 0.15, 0, result);
            
            edgesInverted.release();
            edges.release();
            return result;
        } finally {
            gray.release();
            inverted.release();
            blurred.release();
            invertedBlur.release();
            sketch.release();
        }
    }
    
    private Mat createVintageEffect(Mat color) {
        Mat sepia = new Mat();
        Mat result = new Mat();
        
        try {
            Mat sepiaMatrix = new Mat(3, 3, CvType.CV_32F);
            float[] data = {0.393f, 0.769f, 0.189f, 0.349f, 0.686f, 0.168f, 0.272f, 0.534f, 0.131f};
            sepiaMatrix.put(0, 0, data);
            
            Core.transform(color, sepia, sepiaMatrix);
            Imgproc.GaussianBlur(sepia, result, new Size(3, 3), 0);
            
            sepiaMatrix.release();
            return result;
        } finally {
            sepia.release();
        }
    }

    public byte[] getSketchImage(String filename) throws IOException {
        Path path = outputDir.resolve(filename);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filename);
        }
        return Files.readAllBytes(path);
    }
    
    public String convertWithCustomParams(MultipartFile file, int blurSize, double contrast) throws IOException {
        if (!OpenCVUtil.isInitialized()) {
            throw new RuntimeException("OpenCV is not loaded");
        }
        
        if (blurSize < 3) blurSize = 3;
        if (blurSize % 2 == 0) blurSize++;
        
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String filename = UUID.randomUUID().toString() + extension;
        Path inputPath = uploadDir.resolve(filename);
        
        Files.write(inputPath, file.getBytes());
        
        String sketchName = "sketch_custom_" + filename;
        Path outputPath = outputDir.resolve(sketchName);
        
        convertWithCustomParams(inputPath.toString(), outputPath.toString(), blurSize, contrast);
        
        Files.deleteIfExists(inputPath);
        return sketchName;
    }
    
    private void convertWithCustomParams(String input, String output, int blurSize, double contrast) {
        Mat image = Imgcodecs.imread(input);
        if (image.empty()) {
            throw new RuntimeException("Failed to load image");
        }
        
        Mat gray = new Mat();
        Mat inverted = new Mat();
        Mat blurred = new Mat();
        Mat invertedBlur = new Mat();
        Mat sketch = new Mat();
        
        try {
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
            Core.bitwise_not(gray, inverted);
            Imgproc.GaussianBlur(inverted, blurred, new Size(blurSize, blurSize), 0);
            Core.bitwise_not(blurred, invertedBlur);
            Core.divide(gray, invertedBlur, sketch, 255);
            
            Mat temp = new Mat();
            sketch.convertTo(temp, CvType.CV_32F);
            Core.multiply(temp, new Scalar(contrast), temp);
            temp.convertTo(sketch, CvType.CV_8U);
            temp.release();
            
            Imgcodecs.imwrite(output, sketch);
            
        } finally {
            image.release();
            gray.release();
            inverted.release();
            blurred.release();
            invertedBlur.release();
            sketch.release();
        }
    }
}