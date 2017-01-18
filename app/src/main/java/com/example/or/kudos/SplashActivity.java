package com.example.or.kudos;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final RelativeLayout splashScreenRelativeLayout = (RelativeLayout)findViewById(R.id.activity_splash_relative_layout);
        final ImageView splashScreenImage = (ImageView)findViewById(R.id.splashScreenImageView);
        final Animation rotateAnimation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
        final Animation fadeOutAnimation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fadeout);


        splashScreenImage.startAnimation(rotateAnimation);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent loginIntent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(loginIntent);
                finish();
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
            }
        }, 3000);
    }
}
