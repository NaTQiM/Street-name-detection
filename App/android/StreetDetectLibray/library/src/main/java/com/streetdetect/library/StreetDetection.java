package com.streetdetect.library;

public class StreetDetection {

    public static void ProcessImage(byte[] image, int width, int height, DetectionListener listener) {
        listener.onSuccess("Hello! from Android Native " + width + " " + height + " " + image.length);
    }
}
