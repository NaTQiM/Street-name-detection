package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

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
    private Interpreter interpreter = null;
    private boolean isInitialized = false;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private int inputImgWidth = 0;
    private int inputImgHeight = 0;
    private int inputModelSize = 0;
    private  Context context = null;

    public boolean isInitialized() {
        return isInitialized;
    }

    private final String LOG_TAG = "TextGeneration";

    public TextGeneration(Context context) {
        this.context = context;
    }

    public Task<Void> initialize() {

        TaskCompletionSource task = new TaskCompletionSource<Void>();

        executorService.execute((Runnable) new Runnable() {
//            @Override
            public final void run() {
                try {
                    TextGeneration.this.initializeTask();
                    task.setResult((Object)null);
                } catch (IOException error) {
                    task.setException((Exception)error);
                }
            }
        });

        return task.getTask();

    }

    private final void initializeTask() throws IOException {
        AssetManager assetManager = context.getAssets();
        Intrinsics.checkExpressionValueIsNotNull(assetManager, "assetManager");
        ByteBuffer model = this.loadModelFile(assetManager, "textgen.tflite");
        Interpreter.Options options = new Interpreter.Options();

        Interpreter interpreter = new Interpreter(model, options);

        int[] inputShape = interpreter.getInputTensor(0).shape();
        this.inputImgWidth = inputShape[1];
        this.inputImgHeight = inputShape[2];
        this.inputModelSize = 4 * this.inputImgWidth*this.inputImgHeight*1;
        this.interpreter = interpreter;

        this.isInitialized = true;
        Log.d(LOG_TAG, "Initialized TFLite interpreter.");
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String filename) throws IOException {
        AssetFileDescriptor assetFileDescriptor = assetManager.openFd(filename);
        AssetFileDescriptor fileDescriptor = assetFileDescriptor;
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declareLength = fileDescriptor.getDeclaredLength();

        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength);
        return (ByteBuffer) mappedByteBuffer;
    }

    private void close() {
        this.executorService.execute(() -> {
            this.interpreter.close();
            Log.d(LOG_TAG, "Closed TFLite interpreter.");
        });
    }

    public String generator(bit)
}
