package com.streetdetect.test;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.streetdetect.library.DetectionListener;
import com.streetdetect.library.StreetDetection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bitmap bmp;
        try {
            // get input stream
            InputStream ims = getAssets().open("temp.jpg");
            // load image as Drawable
            bmp = BitmapFactory.decodeStream(ims);
        }
        catch(IOException ex) {
            return;
        }

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 0 /* Ignored for PNGs */, blob);

        byte[] image = blob.toByteArray();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Log.i(">>> image: ", width + " - " + height);

        StreetDetection.ProcessImage(image, width, height, new DetectionListener() {
            @Override
            public void onSuccess(String response) {
                Log.i(" >>>>>>>>>>>>>>>>> ", response);
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }
}