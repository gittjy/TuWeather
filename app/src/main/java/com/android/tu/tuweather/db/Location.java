package com.android.tu.tuweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by tjy on 2017/3/6.
 */
public class Location extends DataSupport{

    private int id;

    private String otherPlace;


    private String provinceName;

    private String locweatherid;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOtherPlace() {
        return otherPlace;
    }

    public void setOtherPlace(String otherPlace) {
        this.otherPlace = otherPlace;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getLocweatherid() {
        return locweatherid;
    }

    public void setLocweatherid(String locweatherid) {
        this.locweatherid = locweatherid;
    }

}
