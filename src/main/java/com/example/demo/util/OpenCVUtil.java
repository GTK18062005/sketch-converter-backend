package com.example.demo.util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;

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
            System.out.println("Java Library Path: " + System.getProperty("java.library.path"));
            
            boolean success = false;
            
            if (os.contains("linux")) {
                // Try different possible locations for OpenCV
                String[] libPaths = {
                    "/usr/lib/libopencv_java4120.so",
                    "/usr/lib/x86_64-linux-gnu/libopencv_java.so",
                    "/usr/lib/x86_64-linux-gnu/libopencv_java4120.so",
                    "/usr/lib/jni/libopencv_java.so",
                    "/usr/local/lib/libopencv_java4120.so",
                    "/usr/lib/libopencv_java.so",
                    "/usr/share/java/opencv4/libopencv_java4120.so"
                };
                
                for (String path : libPaths) {
                    File libFile = new File(path);
                    if (libFile.exists()) {
                        System.out.println("Found OpenCV at: " + path);
                        try {
                            System.load(path);
                            System.out.println("✅ OpenCV loaded from: " + path);
                            success = true;
                            break;
                        } catch (UnsatisfiedLinkError e) {
                            System.err.println("Failed to load from " + path + ": " + e.getMessage());
                        }
                    }
                }
                
                if (!success) {
                    try {
                        System.loadLibrary("opencv_java4120");
                        System.out.println("✅ OpenCV loaded via System.loadLibrary");
                        success = true;
                    } catch (UnsatisfiedLinkError e) {
                        System.err.println("System.loadLibrary (opencv_java4120) failed: " + e.getMessage());
                    }
                }
                
                if (!success) {
                    try {
                        System.loadLibrary("opencv_java");
                        System.out.println("✅ OpenCV loaded via System.loadLibrary (opencv_java)");
                        success = true;
                    } catch (UnsatisfiedLinkError e) {
                        System.err.println("System.loadLibrary (opencv_java) failed: " + e.getMessage());
                    }
                }
            } else if (os.contains("win")) {
                String userDir = System.getProperty("user.dir");
                String dllPath = userDir + "\\opencv_java4120.dll";
                File dllFile = new File(dllPath);
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
                    System.err.println("Failed to load OpenCV: " + e.getMessage());
                }
            }
            
            if (success) {
                initialized = true;
                version = Core.VERSION;
                System.out.println("🎉 OpenCV Version: " + version);
            } else {
                System.err.println("❌ Failed to load OpenCV");
                System.err.println("Listing /usr/lib directory...");
                File libDir = new File("/usr/lib");
                if (libDir.exists()) {
                    String[] files = libDir.list();
                    for (String file : files) {
                        if (file.contains("opencv")) {
                            System.err.println("  Found: " + file);
                        }
                    }
                }
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
