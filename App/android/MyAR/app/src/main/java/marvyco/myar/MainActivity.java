package marvyco.myar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Start into display activity
        Intent intentWelcome = new Intent(this, DisplayWelcome.class);
        startActivity(intentWelcome);

        Log.i(MainActivity.class.toString(), " Entered");
    }
}