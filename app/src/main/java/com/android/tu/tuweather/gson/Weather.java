package com.android.tu.tuweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by tjy on 2017/3/1.
 */
public class Weather {

    public String status;

    public AQI aqi;

    public Basic basic;

    public Forecast forecast;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

}
