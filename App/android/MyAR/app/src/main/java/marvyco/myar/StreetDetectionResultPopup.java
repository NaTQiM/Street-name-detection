package marvyco.myar;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import marvyco.myar.objectdata.StreetObjectGMaps;

public class StreetDetectionResultPopup extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.street_detection_result_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int w = dm.widthPixels;
        int h = dm.heightPixels;

        String currentStreetName = StreetDetectionActivity.currentStreetName.getName();
        float _w_coef = (0.38f/9)*currentStreetName.length();
        float _h_coef = 0.1f;

        getWindow().setLayout((int)(w*_w_coef),(int)(h*_h_coef));

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.x = 0;
        lp.y = 0;

        getWindow().setAttributes(lp);

        Button detect_result = findViewById(R.id.detect_result);
        detect_result.setText("ĐƯỜNG\n" + currentStreetName);
        detect_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentStreetView = new Intent(StreetDetectionResultPopup.this, StreetViewActivity.class);
                startActivity(intentStreetView);
            }
        });

        Log.i(StreetDetectionResultPopup.class.getName(), currentStreetName);
    }
}
