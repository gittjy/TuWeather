package com.android.tu.tuweather;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.tuweather.db.Location;
import com.android.tu.tuweather.gson.Weather;
import com.android.tu.tuweather.util.HttpUtil;
import com.android.tu.tuweather.util.Utility;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.lusfold.spinnerloading.SpinnerLoading;

import org.litepal.crud.DataSupport;

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
public class WeatherActivity extends AppCompatActivity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener{

    /*@BindView(R.id.refresh_swipe)
    SwipeRefreshLayout refreshSwipe;*/
    /*@BindView(R.id.weather_layout)
    BounceScrollView weatherLayout;*/
    @BindView(R.id.load_frame)
    FrameLayout loadFrameLayout;
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
    private SpinnerLoading spiLoadView;


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

        mainFrameLayout.setVisibility(View.GONE);
        //Glide.with(this).load(R.mipmap.mountain).into(loadImage);
        loadFrameLayout.setVisibility(View.VISIBLE);
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
            /*spiLoadView = (SpinnerLoading) findViewById(R.id.spi_load);
            spiLoadView.setPaintMode(1);
            spiLoadView.setCircleRadius(10);
            spiLoadView.setItemCount(8);
            spiLoadView.setVisibility(View.VISIBLE);*/
        }
        setOnClickListener();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(R.mipmap.storm).into(backImage);
        }else{
            loadImage();
        }
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
                        Glide.with(WeatherActivity.this).load(R.mipmap.storm).into(backImage);
                    }
                });
            }
        });
    }

    /**
     * 请求天气数据
     * @param mWeatherId
     */
    public void requestWeather(String mWeatherId) {
        weatherId=mWeatherId;
        loadImage();
        String weatherUrl="https://free-api.heweather.com/v5/weather?city="+mWeatherId+"&key=1c3dd6a908d1467dbb730e3e6aaffcb6";
        HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
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
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            updateImageBtn.clearAnimation();
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
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
        String imageUrl="http://files.heweather.com/cond_icon/"+weather.now.more.code+".png";
        Glide.with(WeatherActivity.this).load(imageUrl).into(condImage);
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
        //spiLoadView.setVisibility(View.GONE);
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
                showPlaceDialog();
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

    private void showPlaceDialog() {
        final Dialog dialog=new Dialog(WeatherActivity.this,R.style.Translucent_white_dialog);
        View view=LayoutInflater.from(WeatherActivity.this).inflate(R.layout.added_area_layout,null);
        RelativeLayout locTextLayout= (RelativeLayout) view.findViewById(R.id.loc_text_layout);
        TextView locTextView= (TextView) view.findViewById(R.id.loc_textview);
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String locName=prefs.getString("bdloc",null);
        if(locName!=null){
            locTextView.setText(locName);
        }
        SwipeMenuListView placeListView= (SwipeMenuListView) view.findViewById(R.id.added_area_listview);
        //构造listview的滑动出现的按钮
        SwipeMenuCreator creator=new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem swipeMenuItem=new SwipeMenuItem(WeatherActivity.this);
                swipeMenuItem.setBackground(new ColorDrawable(Color.RED));
                swipeMenuItem.setTitle("删除");
                swipeMenuItem.setTitleColor(Color.WHITE);
                swipeMenuItem.setTitleSize(12);
                swipeMenuItem.setWidth(dp2px(50));
                menu.addMenuItem(swipeMenuItem);
            }
        };
        placeListView.setMenuCreator(creator);
        ImageButton addImgBtn= (ImageButton) view.findViewById(R.id.add_image_btn);
        addImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectPlaceDialog();
                dialog.dismiss();
            }
        });
        final ArrayList<String> addedAreaList=new ArrayList<>();
        final ArrayList<String> addedWeatherIdList=new ArrayList<>();
        ArrayList<Location> locationList= (ArrayList<Location>) DataSupport.findAll(Location.class);
        if(locationList.size()>0){
            for(Location mLocation:locationList){
                addedAreaList.add(mLocation.getUserLocation());
                addedWeatherIdList.add(mLocation.getLocweatherid());
            }
        }
        //AddedAreaListAdapter mAdapter=new AddedAreaListAdapter(WeatherActivity.this,R.layout.added_area_item,addedAreaList);
        final ArrayAdapter<String> mAdapter=new ArrayAdapter<String>(WeatherActivity.this,android.R.layout.simple_list_item_1,addedAreaList);
        placeListView.setAdapter(mAdapter);
        placeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                weatherId=addedWeatherIdList.get(i);
                requestWeather(weatherId);
                dialog.dismiss();
            }
        });
        placeListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index){
                    case 0:
                        DataSupport.deleteAll(Location.class,"userlocation=?",addedAreaList.get(position));
                        addedAreaList.remove(position);
                        mAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });
        locTextLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                weatherId=prefs.getString("bdloc",null);
                requestWeather(weatherId);
                dialog.dismiss();
            }

        });
        dialog.setContentView(view);
        dialog.show();
    }

    /**
     * 显示生活信息的弹窗
     */
    private void showSugDialog() {
        View view=LayoutInflater.from(WeatherActivity.this).inflate(R.layout.sug_dialog_layout,null);//加载布局
        TextView comfortText= (TextView) view.findViewById(R.id.comfort_text);
        TextView carWashText= (TextView) view.findViewById(R.id.car_wash_text);
        TextView sportText= (TextView) view.findViewById(R.id.sport_text);
        if(currentWeather!=null){
            comfortText.setText(currentWeather.suggestion.comfort.info);
            carWashText.setText(currentWeather.suggestion.carWash.info);
            sportText.setText(currentWeather.suggestion.sport.info);
        }
        Dialog dialog=new Dialog(WeatherActivity.this,R.style.Translucent_dialog);
        dialog.setContentView(view);
       /* Window window=dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);*/
        dialog.show();
        //控制dialog的位置在底部
       /* Window window=dialog.getWindow();
        window.getDecorView().setPadding(0,0,0,0);
        WindowManager.LayoutParams layoutParams=window.getAttributes();
        layoutParams.width= WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity= Gravity.BOTTOM;
        window.setAttributes(layoutParams);*/
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
        Animation opreatAnim= AnimationUtils.loadAnimation(WeatherActivity.this,R.anim.update_rotate_anim);
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
            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
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
