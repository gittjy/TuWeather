package com.android.tu.tuweather.util;

import android.text.TextUtils;

import com.android.tu.tuweather.db.City;
import com.android.tu.tuweather.db.County;
import com.android.tu.tuweather.db.Province;
import com.android.tu.tuweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tjy on 2017/2/28.
 */
public class Utility {

    /**
     * 解析和处理服务器返回的省级JSON数据
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray provinceArray=new JSONArray(response);
                for(int i=0;i<provinceArray.length();i++){
                    JSONObject mJsonObject=provinceArray.getJSONObject(i);
                    Province mProvince=new Province();
                    mProvince.setProvinceCode(mJsonObject.getInt("id"));
                    mProvince.setProvinceName(mJsonObject.getString("name"));
                    mProvince.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级JSON数据
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray cityArray=new JSONArray(response);
                for (int i = 0; i < cityArray.length(); i++) {
                    JSONObject mJsonObject=cityArray.getJSONObject(i);
                    City mCity=new City();
                    mCity.setCityName(mJsonObject.getString("name"));
                    mCity.setCityCode(mJsonObject.getInt("id"));
                    mCity.setProvinceId(provinceId);
                    mCity.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级JSON数据
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray countyArray=new JSONArray(response);
                for (int i = 0; i < countyArray.length(); i++) {
                    JSONObject mJsonObject=countyArray.getJSONObject(i);
                    County mCounty=new County();
                    mCounty.setCountyName(mJsonObject.getString("name"));
                    mCounty.setWeatherId(mJsonObject.getString("weather_id"));
                    mCounty.setCityId(cityId);
                    mCounty.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather5");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}
