package org.tensorflow.lite.examples.detection;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognition;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.cert.PKIXRevocationChecker;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.tensorflow.lite.Interpreter;

import kotlin.jvm.internal.Intrinsics;

public class TextGeneration {
    InputImage image;
    TextRecognizer recognizer;
    public TextGeneration(Bitmap bitmap, int rot) {
        image = InputImage.fromBitmap(bitmap, rot);
        recognizer = TextRecognition.getClient();
    }

    public TextGenListener Generate() {
        TextGenListener textGenListener = new TextGenListener();
        Task<Text> result =
            recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        // Task completed successfully
                        // ...
                        textGenListener.Invoke(visionText);
                    }
                })
                .addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });
        return textGenListener;
    }

    public class TextGenListener {
        private TextGenCallback textGenCallback;
        public void AddListener(TextGenCallback callback) {
            this.textGenCallback = callback;
        }
        public void Invoke(Text text) {
            textGenCallback.CallBack(text);
        }
    }

    public interface TextGenCallback {
        void CallBack(Text text);
    }

}


