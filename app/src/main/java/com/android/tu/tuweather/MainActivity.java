package com.android.tu.tuweather;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_TIME=5000;

    private Handler handler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decroView=getWindow().getDecorView();
            decroView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        /*setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getBoolean("isFirst",true)){
            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putBoolean("isFirst",false);
            editor.apply();
            startWeatherActivityDelay();
        }else{
            startWeatherActivity();
        }*/
        startWeatherActivity();

    }

    private void startWeatherActivityDelay() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startWeatherActivity();
            }
        },SPLASH_TIME);
    }

    private void startWeatherActivity() {
        Intent intent=new Intent(MainActivity.this,WeatherActivity.class);
        startActivity(intent);
        finish();
    }

}
