package com.example.demo.util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class OpenCVUtil {
    
    private static boolean initialized = false;
    private static String version = null;
    
    static {
        loadOpenCV();
    }
    
    private static void loadOpenCV() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            System.out.println("\n========================================");
            System.out.println("Loading OpenCV on: " + os);
            System.out.println("========================================");
            
            boolean success = false;
            
            if (os.contains("linux")) {
                try {
                    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                    System.out.println("✅ OpenCV loaded from system library");
                    success = true;
                } catch (UnsatisfiedLinkError e) {
                    System.err.println("System library load failed: " + e.getMessage());
                }
            } else if (os.contains("win")) {
                String userDir = System.getProperty("user.dir");
                String dllPath = userDir + "\\opencv_java4120.dll";
                java.io.File dllFile = new java.io.File(dllPath);
                if (dllFile.exists()) {
                    System.load(dllPath);
                    System.out.println("✅ OpenCV loaded from: " + dllPath);
                    success = true;
                } else {
                    System.err.println("DLL not found at: " + dllPath);
                }
            } else if (os.contains("mac")) {
                try {
                    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                    System.out.println("✅ OpenCV loaded from system library");
                    success = true;
                } catch (UnsatisfiedLinkError e) {
                    System.err.println("System library load failed: " + e.getMessage());
                }
            }
            
            if (success) {
                initialized = true;
                version = Core.VERSION;
                System.out.println("🎉 OpenCV Version: " + version);
            } else {
                System.err.println("❌ Failed to load OpenCV");
            }
            System.out.println("========================================\n");
            
        } catch (Exception e) {
            System.err.println("OpenCV loading error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static boolean isInitialized() {
        return initialized;
    }
    
    public static String getVersion() {
        return initialized ? version : "Not initialized";
    }
    
    public static boolean testOpenCV() {
        if (!initialized) return false;
        try {
            Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
            boolean success = !mat.empty();
            mat.release();
            return success;
        } catch (Exception e) {
            return false;
        }
    }
}