package com.android.tu.tuweather.model;

/**
 * Created by tjy on 2017/4/18.
 */
public class PlaceItem {

    private String placeName;

    private String placeWeather;

    public PlaceItem(String name,String weather){
        this.placeName=name;
        this.placeWeather=weather;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceWeather() {
        return placeWeather;
    }

    public void setPlaceWeather(String placeWeather) {
        this.placeWeather = placeWeather;
    }
}
