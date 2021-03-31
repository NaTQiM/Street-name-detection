package com.streetdetect.testlibrary;

import android.util.Log;
import android.widget.Toast;

public class MyStreetNameSignDetection implements IStreetDetection {

    public static void ProcessImage(byte[] image, int width, int height, DetectionListener listener) {
        listener.onSuccess("Hello! from Android Native " + width + " " + height + " " + image.length);
    }

}

