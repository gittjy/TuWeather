package com.android.tu.tuweather.model;

/**
 * Created by tjy on 2017/4/18.
 */
public class PlaceItem {

    private String placeName;

    private String placeProvince;


    private String placeWeatherId;

    public PlaceItem(String name,String province,String weatherid){
        this.placeName=name;
        this.placeProvince =province;
        this.placeWeatherId=weatherid;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceProvince() {
        return placeProvince;
    }

    public void setPlaceProvince(String placeProvince) {
        this.placeProvince = placeProvince;
    }

    public String getPlaceWeatherId() {
        return placeWeatherId;
    }

    public void setPlaceWeatherId(String placeWeatherId) {
        this.placeWeatherId = placeWeatherId;
    }
}
