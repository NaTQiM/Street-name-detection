package com.streetdetect.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.streetdetect.testlibrary.DetectionListener;
import com.streetdetect.testlibrary.MyStreetNameSignDetection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        byte [] image = {1,2,3,4,5,6,7};
        MyStreetNameSignDetection.ProcessImage(image, 0, 0, new DetectionListener() {
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