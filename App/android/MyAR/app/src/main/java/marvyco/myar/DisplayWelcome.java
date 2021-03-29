package marvyco.myar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Scene;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class DisplayWelcome extends AppCompatActivity {

    Scene mainScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hide title bar and enable full-screen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE); //hide the title
        getSupportActionBar().hide(); //hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen

        //Set main content view
        setContentView(R.layout.activity_main);
        ViewGroup root = findViewById(R.id.main_container);
        mainScene = Scene.getSceneForLayout(root, R.layout.welcome_layout, this);
        mainScene.enter();

        // Setting time to move on to HomeScene
        final Context _this = this;
        welcomeWaiting(5, new Runnable() {
            @Override
            public void run() {
                final Intent intentHome = new Intent(_this, HomeScene.class);
                startActivity(intentHome);
            }
        });

        Log.i(DisplayWelcome.class.toString(), " Entered");
    }

    void welcomeWaiting(int seconds, Runnable callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(seconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                callback.run();
            }
        }).start();

    }
}
