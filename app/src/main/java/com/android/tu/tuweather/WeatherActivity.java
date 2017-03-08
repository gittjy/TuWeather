package com.android.tu.tuweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.tuweather.gson.Weather;
import com.android.tu.tuweather.service.AutoUpdateService;
import com.android.tu.tuweather.util.HttpUtil;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by tjy on 2017/3/1.
 */
public class WeatherActivity extends AppCompatActivity implements View.OnClickListener{

    /*@BindView(R.id.refresh_swipe)
    SwipeRefreshLayout refreshSwipe;*/
    /*@BindView(R.id.weather_layout)
    BounceScrollView weatherLayout;*/
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
    private LocationClient mLocationClient;

    private boolean isLocatedFlag=false; //是否定位成功的标志
    private ProgressDialog progressDialog;
    private String locWeatherId;
    private boolean isWait;//是否在等待定位的标志
    private String weatherId;
    private List<Integer> maxTempList=new ArrayList<>();
    private List<Integer> minTempList=new ArrayList<>();


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
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager
                .PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, android.Manifest.permission.READ_PHONE_STATE)!=PackageManager
                .PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager
                .PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(permissionList.size()>0){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherActivity.this,permissions,1);
        }else{
            getBDLocation();
            progressDialog = new ProgressDialog(WeatherActivity.this);
            progressDialog.show();
        }
        ButterKnife.bind(this);
        updateImageBtn.setOnClickListener(this);
        //refreshSwipe.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(R.mipmap.italy).into(backImage);
        }else{
            loadImage();
        }

        //while (!isLocatedFlag){}
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
            progressDialog.setTitle("天气信息获取...");
            isWait = true;
        }
        /*refreshSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });*/

    }

    private void getBDLocation() {
        LocationClientOption option=new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setScanSpan(2000);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    private void loadImage() {
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
                        Glide.with(WeatherActivity.this).load(R.mipmap.mountain).into(backImage);
                    }
                });
            }
        });
    }

    /**
     * 请求天气数据
     * @param weatherId
     */
    private void requestWeather(String weatherId) {
        loadImage();
        String weatherUrl="https://free-api.heweather.com/v5/weather?city="+weatherId+"&key=1c3dd6a908d1467dbb730e3e6aaffcb6";
        HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                       // refreshSwipe.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&weather.status.equals("ok")){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            updateImageBtn.clearAnimation();
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
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
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.substring(5);
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        String[] dateTemp;
        String[] monthday;
        cityText.setText(cityName);
        timeText.setText(updateTime);
        aqiText.setText(weather.aqi.city.aqi+"   "+weather.aqi.city.qlty);
        degreeText.setText(degree);
        String imageUrl="http://files.heweather.com/cond_icon/"+weather.now.more.code+".png";
        Glide.with(WeatherActivity.this).load(imageUrl).into(condImage);
        weatherInfoText.setText(weatherInfo);
        //forecastLinear.removeAllViews();
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
        generateLineChart(maxTempList,minTempList);
        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动建议："+weather.suggestion.sport.info;
        //weatherLayout.setVisibility(View.VISIBLE);
        Intent intent=new Intent(WeatherActivity.this, AutoUpdateService.class);
        startService(intent);
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
                return month+"-"+day;
            }else{
                return month+"-"+dateTemp[2];
            }
        }else if(dateTemp[2].startsWith("0")){
            return dateTemp[1]+"-"+dateTemp[2].substring(1);
        }else{
            return dateTemp[1]+"-"+dateTemp[2];
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
        dataSetMax.setDrawCircleHole(false);
        dataSetMax.setValueTextColor(Color.WHITE);
        dataSetMax.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
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
        dataSetMin.setDrawCircleHole(false);
        dataSetMin.setValueTextColor(Color.WHITE);
        dataSetMin.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
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
            default:
                break;
        }
    }

    private void startAnim() {
        Animation opreatAnim= AnimationUtils.loadAnimation(WeatherActivity.this,R.anim.update_rotate_anim);
        LinearInterpolator interpolator=new LinearInterpolator();
        opreatAnim.setInterpolator(interpolator);
        updateImageBtn.startAnimation(opreatAnim);
    }


    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            progressDialog.dismiss();
            isLocatedFlag=true;
            String county=bdLocation.getDistrict();
            String tempCounty=county.substring(0,county.length()-1);
            //List<County> mCounty=DataSupport.where("countyname=?",tempCounty).find(County.class);
            if(isWait){
                requestWeather(tempCounty);
                weatherId=tempCounty;
                isWait=false;
            }
            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
            editor.putString("bdloc",tempCounty);
            editor.apply();
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }
}
