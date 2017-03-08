package com.android.tu.tuweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by tjy on 2017/3/6.
 */
public class Location extends DataSupport{

    private int id;

    private String userLocation;

    private String weatherId;

    private int isLocate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int isLocate() {
        return isLocate;
    }

    public void setLocate(int locate) {
        isLocate = locate;
    }
}
