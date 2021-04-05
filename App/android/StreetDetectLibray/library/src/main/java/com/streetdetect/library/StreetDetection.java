package com.streetdetect.library;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.text.Text;

import java.nio.ByteBuffer;

public class
StreetDetection {
    private static DetectionListener listener;
    public static void ProcessImage(byte[] bytes, int width, int height, DetectionListener listener) {
//        listener.onSuccess("Hello! from Android Native " + width + " " + height + " " + bytes.length);
        Bitmap image = bytesToBitmap(bytes, width, height);
        TextGeneration textGen = new TextGeneration();
        textGen.AddListener(new TextGeneration.TextGenCallback() {
            @Override
            public void CallBackSuccess(Text text) {
                listener.onSuccess(text.getText());
            }

            @Override
            public void CallBackFailure(@NonNull Exception e) {

            }
        });

        textGen.Generate(image, 0);
    }

    static private Bitmap bytesToBitmap(byte [] bytes, int imageWidth, int imageHeight) {
        /*Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.RGBA_F16);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.rewind();
        bmp.copyPixelsFromBuffer(buffer);*/
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bmp;
    }
}
