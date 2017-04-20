package com.android.tu.tuweather;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.tuweather.gson.Suggestion;
import com.android.tu.tuweather.gson.Weather;
import com.android.tu.tuweather.util.HttpUtil;
import com.android.tu.tuweather.util.ScreenSizeUtil;
import com.android.tu.tuweather.util.Utility;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by tjy on 2017/3/1.
 */
public class WeatherActivity extends AppCompatActivity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener{


    private static final String WEATHER_TAG="Weather_Activity";

    private static final int REQUEST_CODE_PICK_CITY = 0;

    /*@BindView(R.id.refresh_swipe)
    SwipeRefreshLayout refreshSwipe;*/
    /*@BindView(R.id.weather_layout)
    BounceScrollView weatherLayout;*/
    @BindView(R.id.load_frame)
    FrameLayout loadFrameLayout;
    @BindView(R.id.place_fragment_frame)
    FrameLayout placeFragmentFrame;
    @BindView(R.id.weather_main_frame)
    FrameLayout mainFrameLayout;

    @BindView(R.id.city_text)
    TextView cityText;
    @BindView(R.id.aqi_text)
    TextView aqiText;
    @BindView(R.id.time_text)
    TextView timeText;
    @BindView(R.id.degree_text)
    TextView degreeText;
    @BindView(R.id.condition_image)
    ImageView condImage;
    @BindView(R.id.weather_info_text)
    TextView weatherInfoText;
    @BindView(R.id.back_image)
    ImageView backImage;
    @BindView(R.id.nav_img_btn)
    ImageView navImageBtn;
    @BindView(R.id.update_img_btn)
    ImageView updateImageBtn;
    @BindView(R.id.main_drawer)
    DrawerLayout navDrawer;
    @BindView(R.id.nav_view)
    NavigationView navView;

    @BindView(R.id.forcast_layout1)
    LinearLayout forecastLayout1;
    @BindView(R.id.forcast_layout2)
    LinearLayout forecastLayout2;
    @BindView(R.id.forcast_layout3)
    LinearLayout forecastLayout3;
    @BindView(R.id.forcast_layout4)
    LinearLayout forecastLayout4;
    @BindView(R.id.forcast_layout5)
    LinearLayout forecastLayout5;
    @BindView(R.id.forcast_layout6)
    LinearLayout forecastLayout6;
    @BindView(R.id.forcast_layout7)
    LinearLayout forecastLayout7;
    @BindView(R.id.line_chart)
    LineChart mLineChart;
    
    @BindView(R.id.place_linear)
    LinearLayout placeLinear;
    @BindView(R.id.sug_image)
    ImageView sugImageBtn;

    private Context mContext;

    private LocationClient mLocationClient;

    private boolean isLocatedFlag=false; //是否定位成功的标志
    private ProgressDialog progressDialog;
    private String locWeatherId;
    private boolean isWait;//是否在等待定位的标志
    private String weatherId;
    private List<Integer> maxTempList=new ArrayList<>();
    private List<Integer> minTempList=new ArrayList<>();

    private Weather currentWeather;
    private Dialog placeDialog;
    private ChooseAreaFragment chooseAreaFragment;

    private int[] imageIds={R.mipmap.bg_1,R.mipmap.bg_2,R.mipmap.bg_3,R.mipmap.bg_4,R.mipmap.bg_5,
            R.mipmap.bg_6,R.mipmap.bg_7,R.mipmap.bg_8};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        mContext=this;
        mainFrameLayout.setVisibility(View.GONE);
        //Glide.with(this).load(R.mipmap.mountain).into(loadImage);
        loadFrameLayout.setVisibility(View.VISIBLE);
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager
                .PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.READ_PHONE_STATE)!=PackageManager
                .PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager
                .PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(permissionList.size()>0){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherActivity.this,permissions,1);
        }else{
            getBDLocation();
        }
        setOnClickListener();
        setBackImage();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        String bdLoc=prefs.getString("bdloc",null);
        if(weatherString!=null){
            Weather weather= Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
            weatherId =weather.basic.weatherId;
        }
        else if(bdLoc!=null){
            requestWeather(bdLoc);
            weatherId =bdLoc;
        }else{
            isWait = true;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //setBackImage();
        //requestWeather(weatherId);
    }

    private void setBackImage() {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAlbum=prefs.getBoolean("isAlbum",false);
        if(isAlbum){
            String imagePath=prefs.getString("imageUri",null);
            Glide.with(this).load(imagePath).into(backImage);
        }else{
            int number=new Random().nextInt(8);
            Log.d("number",String.valueOf(number));
            Glide.with(this).load(imageIds[number]).into(backImage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getBDLocation();
                }else{
                    finish();
                }
        }
    }

    /**
     * 注册所有必要的点击事件
     */
    private void setOnClickListener() {
        navImageBtn.setOnClickListener(this);
        updateImageBtn.setOnClickListener(this);
        placeLinear.setOnClickListener(this);
        sugImageBtn.setOnClickListener(this);
        navView.setNavigationItemSelectedListener(this);
    }

    private void getBDLocation() {
        LocationClientOption option=new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setScanSpan(2000);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    /*private void loadImage() {
        final String imgUrl="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(imgUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String bingPic=response.body().string();//获得必应图片的链接地址
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(R.mipmap.bg_6).into(backImage);
                    }
                });
            }
        });
    }*/

    /**
     * 请求天气数据
     * @param mWeatherId
     */
    public void requestWeather(String mWeatherId) {
        weatherId=mWeatherId;
        //loadImage();
        final String weatherUrl="https://free-api.heweather.com/v5/weather?city="+mWeatherId+"&key=1c3dd6a908d1467dbb730e3e6aaffcb6";
        Log.d(WEATHER_TAG,weatherUrl);
        HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.warning(mContext,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        updateImageBtn.clearAnimation();
                       // refreshSwipe.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText=response.body().string();
                Log.d("Weather",responseText);
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&weather.status.equals("ok")){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            updateImageBtn.clearAnimation();
                            Toasty.success(mContext,"更新成功",Toast.LENGTH_SHORT).show();
                        }else {
                            Toasty.warning(mContext,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                            updateImageBtn.clearAnimation();
                        }
                        //refreshSwipe.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 展示天气信息
     */
    private void showWeatherInfo(Weather weather) {
        setBackImage();
        currentWeather=weather;
        String cityName=weather.basic.cityName;
        String updateTime=dateStringUtility(weather.basic.update.updateTime);
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        cityText.setText(cityName);
        timeText.setText(updateTime);
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi+"   "+weather.aqi.city.qlty);
        }else{
            aqiText.setText("本地无数据");
        }
        degreeText.setText(degree);
        /*String imageUrl="http://files.heweather.com/cond_icon/"+weather.now.more.code+".png";
        Glide.with(mContext).load(imageUrl).into(condImage);*/
        if(weatherInfo.equals("雷阵雨伴有冰雹")){
            weatherInfo="雷阵雨";
        }
        if(weatherInfo.equals("毛毛雨/细雨")){
            weatherInfo="毛毛雨";
        }
        weatherInfoText.setText(weatherInfo);
        removeLayoutView();
        maxTempList.clear();
        minTempList.clear();
        for(int i=0;i<weather.forecastList.size();i++){
            View view;
            TextView dateText;
            TextView infoText;
            TextView maxText;
            TextView minText;
            maxTempList.add(Integer.valueOf(weather.forecastList.get(i).temperature.max));
            minTempList.add(Integer.valueOf(weather.forecastList.get(i).temperature.min));
            switch (i){
                case 0:
                    view= LayoutInflater.from(this).inflate(R.layout.forcast_item,forecastLayout1,false);
                    dateText= (TextView) view.findViewById(R.id.date_text);
                    infoText= (TextView) view.findViewById(R.id.info_text);
                    maxText= (TextView) view.findViewById(R.id.max_text);
                    minText=(TextView)view.findViewById(R.id.min_text);
                    dateText.setText("今天");
                    infoText.setText(weather.forecastList.get(i).more.info);
                    maxText.setText(weather.forecastList.get(i).temperature.max+"°");
                    minText.setText(weather.forecastList.get(i).temperature.min+"°");
                    forecastLayout1.addView(view);
                    break;
                case 1:
                    view= LayoutInflater.from(this).inflate(R.layout.forcast_item,forecastLayout2,false);
                    dateText= (TextView) view.findViewById(R.id.date_text);
                    infoText= (TextView) view.findViewById(R.id.info_text);
                    maxText= (TextView) view.findViewById(R.id.max_text);
                    minText=(TextView)view.findViewById(R.id.min_text);
                    dateText.setText(dateStringUtility(weather.forecastList.get(i).date));
                    infoText.setText(weather.forecastList.get(i).more.info);
                    maxText.setText(weather.forecastList.get(i).temperature.max+"°");
                    minText.setText(weather.forecastList.get(i).temperature.min+"°");
                    forecastLayout2.addView(view);
                    break;
                case 2:
                    view= LayoutInflater.from(this).inflate(R.layout.forcast_item,forecastLayout3,false);
                    dateText= (TextView) view.findViewById(R.id.date_text);
                    infoText= (TextView) view.findViewById(R.id.info_text);
                    maxText= (TextView) view.findViewById(R.id.max_text);
                    minText=(TextView)view.findViewById(R.id.min_text);
                    dateText.setText(dateStringUtility(weather.forecastList.get(i).date));
                    infoText.setText(weather.forecastList.get(i).more.info);
                    maxText.setText(weather.forecastList.get(i).temperature.max+"°");
                    minText.setText(weather.forecastList.get(i).temperature.min+"°");
                    forecastLayout3.addView(view);
                    break;
                case 3:
                    view= LayoutInflater.from(this).inflate(R.layout.forcast_item,forecastLayout4,false);
                    dateText= (TextView) view.findViewById(R.id.date_text);
                    infoText= (TextView) view.findViewById(R.id.info_text);
                    maxText= (TextView) view.findViewById(R.id.max_text);
                    minText=(TextView)view.findViewById(R.id.min_text);
                    dateText.setText(dateStringUtility(weather.forecastList.get(i).date));
                    infoText.setText(weather.forecastList.get(i).more.info);
                    maxText.setText(weather.forecastList.get(i).temperature.max+"°");
                    minText.setText(weather.forecastList.get(i).temperature.min+"°");
                    forecastLayout4.addView(view);
                    break;
                case 4:
                    view= LayoutInflater.from(this).inflate(R.layout.forcast_item,forecastLayout5,false);
                    dateText= (TextView) view.findViewById(R.id.date_text);
                    infoText= (TextView) view.findViewById(R.id.info_text);
                    maxText= (TextView) view.findViewById(R.id.max_text);
                    minText=(TextView)view.findViewById(R.id.min_text);
                    dateText.setText(dateStringUtility(weather.forecastList.get(i).date));
                    infoText.setText(weather.forecastList.get(i).more.info);
                    maxText.setText(weather.forecastList.get(i).temperature.max+"°");
                    minText.setText(weather.forecastList.get(i).temperature.min+"°");
                    forecastLayout5.addView(view);
                    break;
                case 5:
                    view= LayoutInflater.from(this).inflate(R.layout.forcast_item,forecastLayout6,false);
                    dateText= (TextView) view.findViewById(R.id.date_text);
                    infoText= (TextView) view.findViewById(R.id.info_text);
                    maxText= (TextView) view.findViewById(R.id.max_text);
                    minText=(TextView)view.findViewById(R.id.min_text);
                    dateStringUtility(weather.forecastList.get(i).date);
                    dateText.setText(dateStringUtility(weather.forecastList.get(i).date));
                    infoText.setText(weather.forecastList.get(i).more.info);
                    maxText.setText(weather.forecastList.get(i).temperature.max+"°");
                    minText.setText(weather.forecastList.get(i).temperature.min+"°");
                    forecastLayout6.addView(view);
                    break;
                case 6:
                    view= LayoutInflater.from(this).inflate(R.layout.forcast_item,forecastLayout7,false);
                    dateText= (TextView) view.findViewById(R.id.date_text);
                    infoText= (TextView) view.findViewById(R.id.info_text);
                    maxText= (TextView) view.findViewById(R.id.max_text);
                    minText=(TextView)view.findViewById(R.id.min_text);
                    dateText.setText(dateStringUtility(weather.forecastList.get(i).date));
                    infoText.setText(weather.forecastList.get(i).more.info);
                    maxText.setText(weather.forecastList.get(i).temperature.max+"°");
                    minText.setText(weather.forecastList.get(i).temperature.min+"°");
                    forecastLayout7.addView(view);
                    break;
                default:
                    break;
            }

        }
        loadFrameLayout.setVisibility(View.GONE);
        mainFrameLayout.setVisibility(View.VISIBLE);
        generateLineChart(maxTempList,minTempList);
    }

    private void updateBackImage() {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAlbum=prefs.getBoolean("isAlbum",false);
        if(!isAlbum){
            int number=new Random().nextInt(8);
            Glide.with(this).load(imageIds[number]).into(backImage);
        }

    }


    /**
     * 接收到的日期格式处理
     * @param date
     */
    private String dateStringUtility(String date) {
        String[] dateTemp=date.split("-");
        if(dateTemp[1].startsWith("0")){
            String month=dateTemp[1].substring(1);
            if(dateTemp[2].startsWith("0")){
                String day=dateTemp[2].substring(1);
                return month+"."+day;
            }else{
                return month+"."+dateTemp[2];
            }
        }else if(dateTemp[2].startsWith("0")){
            return dateTemp[1]+"."+dateTemp[2].substring(1);
        }else{
            return dateTemp[1]+"."+dateTemp[2];
        }


    }

    private void generateLineChart(List<Integer> maxTempList, List<Integer> minTempList) {
        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.getAxisLeft().setEnabled(false);
        mLineChart.getXAxis().setEnabled(false);
        mLineChart.getDescription().setEnabled(false);
        mLineChart.getXAxis().setDrawAxisLine(false);
        mLineChart.getLegend().setEnabled(false);
        mLineChart.setTouchEnabled(false);

        ArrayList<Entry> yValueMax=new ArrayList<>();
        for(int i=0;i<maxTempList.size();i++){
            yValueMax.add(new Entry(i,maxTempList.get(i)));
        }
        /*ArrayList<String> xValues = new ArrayList<>();
        for (int i = maxTempList.size()-1; i >=0; i--) {
            xValues.add(String.valueOf(i));
        }*/
        LineDataSet dataSetMax = new LineDataSet(yValueMax, "");
        dataSetMax.setColor(Color.rgb(247,9,104));
        dataSetMax.setCircleColor(Color.rgb(247,9,104));
        dataSetMax.setLineWidth(1f);
        dataSetMax.setCircleRadius(2f);
        dataSetMax.setDrawCircleHole(false);
        dataSetMax.setValueTextColor(Color.WHITE);
        dataSetMax.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.valueOf((int)value)+"°";
            }
        });

        //模拟第二组组y轴数据(存放y轴数据的是一个Entry的ArrayList) 他是构建LineDataSet的参数之一
        ArrayList<Entry> yValueMIn=new ArrayList<>();
        for(int i=0;i<minTempList.size();i++){
            yValueMIn.add(new Entry(i,minTempList.get(i)));
        }
        LineDataSet dataSetMin=new LineDataSet(yValueMIn,"");
        dataSetMin.setColor(Color.rgb(0,204,204));
        dataSetMin.setCircleColor(Color.rgb(0,204,204));
        dataSetMin.setLineWidth(1f);
        dataSetMin.setCircleRadius(2f);
        dataSetMin.setDrawCircleHole(false);
        dataSetMin.setValueTextColor(Color.WHITE);
        dataSetMin.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.valueOf((int)value);
            }
        });
        dataSetMin.setDrawValues(false);

        //构建一个类型为LineDataSet的ArrayList 用来存放所有 y的LineDataSet   他是构建最终加入LineChart数据集所需要的参数
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        //将数据加入dataSets
        dataSets.add(dataSetMax);
        dataSets.add(dataSetMin);

        //构建一个LineData  将dataSets放入
        LineData lineData = new LineData(dataSets);

        //将数据插入
        mLineChart.setData(lineData);
        mLineChart.animateY(1500);
    }

    private void removeLayoutView() {
        forecastLayout1.removeAllViews();
        forecastLayout2.removeAllViews();
        forecastLayout3.removeAllViews();
        forecastLayout4.removeAllViews();
        forecastLayout5.removeAllViews();
        forecastLayout6.removeAllViews();
        forecastLayout7.removeAllViews();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.update_img_btn:
                startAnim();
                requestWeather(weatherId);
                break;
            case R.id.place_linear:
                Intent intent=new Intent(WeatherActivity.this,PlaceSelectActivity.class);
                startActivityForResult(intent,1);
                if(Build.VERSION.SDK_INT<23){
                    overridePendingTransition(R.anim.in_right,R.anim.out_left);
                }
                break;
            case R.id.sug_image:
                showSugDialog();
                break;
            case R.id.nav_img_btn:
                navDrawer.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    String placeName=data.getStringExtra("place_name");
                    requestWeather(placeName);
                }
        }
    }

    /**
     * 显示生活信息的弹窗
     */
    private void showSugDialog() {
        View view=LayoutInflater.from(mContext).inflate(R.layout.sug_dialog_layout,null);//加载布局
        TextView comfortText= (TextView) view.findViewById(R.id.comfort_text);
        TextView carWashText= (TextView) view.findViewById(R.id.car_wash_text);
        TextView sportText= (TextView) view.findViewById(R.id.sport_text);
        if(currentWeather!=null){
            /*Suggestion.Comfort mComfort=currentWeather.suggestion.comfort;
            Suggestion.CarWash mCarwash=currentWeather.suggestion.carWash;
            Suggestion.Sport mSport=currentWeather.suggestion.sport;*/
            Suggestion suggestion=currentWeather.suggestion;
            if(suggestion==null){
                comfortText.setText("无数据(ㄒoㄒ)~~");
                carWashText.setText("无数据(ㄒoㄒ)~~");
                sportText.setText("无数据(ㄒoㄒ)~~");
            }else{
                String comfort=currentWeather.suggestion.comfort.info;
                String carwash=currentWeather.suggestion.carWash.info;
                String sport=currentWeather.suggestion.sport.info;
                comfortText.setText(comfort);
                carWashText.setText(carwash);
                sportText.setText(sport);
            }
        }
        Dialog dialog=new Dialog(mContext,R.style.Translucent_dialog);
        dialog.setContentView(view);
        //控制dialog
        Window window=dialog.getWindow();
        WindowManager.LayoutParams layoutParams=window.getAttributes();
        layoutParams.width= (int) (ScreenSizeUtil.getScreenWidth(mContext)*0.9);
        window.setAttributes(layoutParams);
        dialog.show();
    }

    /**
     * 显示地区选择的Dialog
     */
    private void showSelectPlaceDialog() {
        chooseAreaFragment = new ChooseAreaFragment(); //dialogfragment实例
        chooseAreaFragment.setCancelable(true);
        chooseAreaFragment.setStyle(DialogFragment.STYLE_NO_TITLE,R.style.Translucent_white_dialog);
        chooseAreaFragment.show(getSupportFragmentManager(),"tag");
    }

    public void dismissPlaceDialog(){
        chooseAreaFragment.dismiss();
    }

    private void startAnim() {
        Animation opreatAnim= AnimationUtils.loadAnimation(mContext,R.anim.update_rotate_anim);
        LinearInterpolator interpolator=new LinearInterpolator();
        opreatAnim.setInterpolator(interpolator);
        updateImageBtn.startAnimation(opreatAnim);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_comment:
                break;
            case R.id.nav_setting:
                Intent intentSetting=new Intent(WeatherActivity.this,SettingActivity.class);
                startActivity(intentSetting);
                break;
            case R.id.nav_about_us:
                Intent intentAbout=new Intent(WeatherActivity.this,AboutUsActivity.class);
                startActivity(intentAbout);
                break;
        }
        navDrawer.closeDrawers();
        return true;
    }


    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //progressDialog.dismiss();

            isLocatedFlag=true;
            String county=bdLocation.getDistrict();
            String tempCounty=county.substring(0,county.length()-1);
            //List<County> mCounty=DataSupport.where("countyname=?",tempCounty).find(County.class);
            if(isWait){
                requestWeather(tempCounty);
                weatherId=tempCounty;
                isWait=false;
            }
            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(mContext).edit();
            editor.putString("bdloc",tempCounty);
            editor.apply();
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    public int dp2px(float dipValue) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);

    }

}
