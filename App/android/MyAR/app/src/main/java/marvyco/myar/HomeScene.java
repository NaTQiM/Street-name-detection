package marvyco.myar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeScene extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Start into display activity
        setContentView(R.layout.home_scene);

        Button gotoStreetDetection = findViewById(R.id.button_street_detection);
        Button gotoMoneyDetection = findViewById(R.id.button_money_detection);

        final Context _this = this;

        gotoStreetDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentDetectionActivity = new Intent(_this, StreetDetectionActivity.class);
                startActivity(intentDetectionActivity);

            }
        });

        gotoMoneyDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intentDetectionActivity = new Intent(_this, MoneyDetectionActivity.class);
            }
        });
    }
}
