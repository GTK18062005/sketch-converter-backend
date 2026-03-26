package com.example.demo.controller;

import com.example.demo.service.SketchConverterService;
import com.example.demo.util.OpenCVUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sketch")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class SketchController {

    private final SketchConverterService sketchConverterService;

    public SketchController(SketchConverterService sketchConverterService) {
        this.sketchConverterService = sketchConverterService;
    }

    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> convertToSketch(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "style", required = false, defaultValue = "PENCIL") String style) {

        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("success", false);
            response.put("error", "Please select an image file");
            return ResponseEntity.badRequest().body(response);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("success", false);
            response.put("error", "Invalid file type. Please upload an image.");
            return ResponseEntity.badRequest().body(response);
        }

        if (file.getSize() > 50 * 1024 * 1024) {
            response.put("success", false);
            response.put("error", "File size exceeds 50MB limit");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            System.out.println("\n=== Processing Image ===");
            System.out.println("File: " + file.getOriginalFilename());
            System.out.println("Size: " + file.getSize() + " bytes");
            System.out.println("Style: " + style);
            
            SketchConverterService.SketchStyle sketchStyle;
            try {
                sketchStyle = SketchConverterService.SketchStyle.valueOf(style.toUpperCase());
            } catch (IllegalArgumentException e) {
                sketchStyle = SketchConverterService.SketchStyle.PENCIL;
            }
            
            String sketchFileName = sketchConverterService.convertToSketchWithStyle(file, sketchStyle);
            
            response.put("success", true);
            response.put("filename", sketchFileName);
            response.put("style", sketchStyle.name());
            response.put("message", "Image converted to " + sketchStyle.name().toLowerCase() + " style successfully");
            response.put("downloadUrl", "/api/sketch/download/" + sketchFileName);
            
            System.out.println("✅ Conversion completed with " + sketchStyle.name() + " style");
            System.out.println("====================\n");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping(value = "/convert/custom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> convertWithCustomParams(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "blurSize", required = false, defaultValue = "21") int blurSize,
            @RequestParam(value = "contrast", required = false, defaultValue = "1.2") double contrast) {

        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("success", false);
            response.put("error", "Please select an image file");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            System.out.println("\n=== Custom Processing ===");
            System.out.println("File: " + file.getOriginalFilename());
            System.out.println("Blur Size: " + blurSize);
            System.out.println("Contrast: " + contrast);
            
            String sketchFileName = sketchConverterService.convertWithCustomParams(file, blurSize, contrast);
            
            response.put("success", true);
            response.put("filename", sketchFileName);
            response.put("message", "Image converted with custom parameters");
            response.put("downloadUrl", "/api/sketch/download/" + sketchFileName);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadSketch(@PathVariable String filename) {
        try {
            byte[] imageBytes = sketchConverterService.getSketchImage(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/styles")
    public ResponseEntity<Map<String, Object>> getAvailableStyles() {
        Map<String, Object> response = new HashMap<>();
        
        String[] styles = {"PENCIL", "WATERCOLOR", "OIL_PAINTING", "CHARCOAL", "SKETCH_ART", "VINTAGE"};
        response.put("styles", styles);
        
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("PENCIL", "✏️ Classic pencil sketch with smooth, refined lines");
        descriptions.put("WATERCOLOR", "🎨 Soft, flowing watercolor painting with blended colors");
        descriptions.put("OIL_PAINTING", "🖼️ Rich oil painting effect with thick brush strokes");
        descriptions.put("CHARCOAL", "⚫ Dark, dramatic charcoal drawing with deep contrasts");
        descriptions.put("SKETCH_ART", "🎨 Artistic sketch with enhanced details and depth");
        descriptions.put("VINTAGE", "📜 Vintage sepia tone with aged paper texture");
        response.put("description", descriptions);
        
        response.put("default", "PENCIL");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "API is working!");
        response.put("message", "Server is running on port 8080");
        response.put("opencv_version", OpenCVUtil.getVersion());
        response.put("opencv_loaded", String.valueOf(OpenCVUtil.isInitialized()));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test-opencv")
    public ResponseEntity<Map<String, String>> testOpenCV() {
        Map<String, String> response = new HashMap<>();
        try {
            boolean working = OpenCVUtil.testOpenCV();
            response.put("status", working ? "SUCCESS" : "FAILED");
            response.put("message", working ? "OpenCV is working correctly!" : "OpenCV test failed");
            response.put("version", OpenCVUtil.getVersion());
            response.put("loaded", String.valueOf(OpenCVUtil.isInitialized()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
        @GetMapping("/health")
public ResponseEntity<Map<String, String>> health() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("timestamp", String.valueOf(System.currentTimeMillis()));
    response.put("port", System.getProperty("server.port", "8080"));
    return ResponseEntity.ok(response);
}
    }
}
