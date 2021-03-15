package org.tensorflow.lite.examples.detection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognition;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.concurrent.Executor;


public class TextGeneration {
    TextRecognizer recognizer;
    boolean isGenerating = false;
    private ArrayList<TextGenCallback> textGenCallbacks = new ArrayList<TextGenCallback>();

    public TextGeneration() {
        recognizer = TextRecognition.getClient();
    }

    public Task<Text> Generate(Bitmap bitmap, int rotation) {
        if (isGenerating)
            return null;
        isGenerating = true;

        InputImage image = InputImage.fromBitmap(bitmap, rotation);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
                                InvokeSuccess(visionText);
                                isGenerating = false;
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        InvokeFailure(e);
                                        isGenerating = false;
                                    }
                                });
        return result;
    }


    public void AddListener(TextGenCallback callback) {
        if (!this.textGenCallbacks.contains(callback))
            this.textGenCallbacks.add(callback);
    }

    public void RemoveListener(TextGenCallback callback) {
        if (this.textGenCallbacks.contains(callback))
            this.textGenCallbacks.add(callback);

    }

    public void InvokeSuccess(Text text) {
        for (TextGenCallback callback: textGenCallbacks) {
            callback.CallBackSuccess(text);
        }
    }

    public void InvokeFailure(@NonNull Exception e) {
        for (TextGenCallback callback: textGenCallbacks) {
            callback.CallBackFailure(e);
        }
    }


    public interface TextGenCallback {
        void CallBackSuccess(Text text);
        void CallBackFailure(@NonNull Exception e);
    }

    public static Bitmap cropBitmap(Bitmap bitmap, Rect rect){
        int w=rect.right-rect.left;
        int h=rect.bottom-rect.top;
        Bitmap ret=Bitmap.createBitmap(w, h, bitmap.getConfig());
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(bitmap, -rect.left, -rect.top, null);
        return ret;
    }
}

