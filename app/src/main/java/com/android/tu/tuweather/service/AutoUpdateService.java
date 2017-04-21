package com.android.tu.tuweather.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.tu.tuweather.R;
import com.android.tu.tuweather.gson.Weather;
import com.android.tu.tuweather.util.HttpUtil;
import com.android.tu.tuweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    private static final String SERVICE_TAG="AUTO_SERVICE";

    private static final int TIME_INTERVAL=60*1000;
    private Notification notification;

    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(SERVICE_TAG,"Create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(SERVICE_TAG,"StartCommand");
        updateWeather();
        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime= SystemClock.elapsedRealtime()+TIME_INTERVAL;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }



    private void updateWeather() {
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        String userLoc=sharedPreferences.getString("bdloc",null);
        if(userLoc!=null){
            String weatherUrl="https://free-api.heweather.com/v5/weather?city="+userLoc+"&key=1c3dd6a908d1467dbb730e3e6aaffcb6";
            HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText=response.body().string();
                    if(responseText!=null){
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("bing_pic",responseText);
                        editor.apply();
                        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                        boolean isShowNotify=sharedPreferences.getBoolean("isShowNotify",true);
                        if(isShowNotify){
                            Weather weather= Utility.handleWeatherResponse(responseText);
                            notification = new NotificationCompat.Builder(AutoUpdateService.this)
                                    .setContentTitle(weather.now.more.info+" "+weather.now.temperature+"℃")
                                    .setSmallIcon(R.mipmap.ic_app_logo)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_app_logo))
                                    .setContentText(weather.basic.cityName)
                                    .build();
                            startForeground(1, notification);
                        }else{
                            stopForeground(true);
                        }
                        //stopForeground(1);

                    }
                }
            });
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(SERVICE_TAG,"Destroy");
    }
}
