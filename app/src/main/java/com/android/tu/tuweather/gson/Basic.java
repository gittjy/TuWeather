package com.android.tu.tuweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tjy on 2017/3/1.
 */
public class Basic {
    @SerializedName("city")
    public String cityName; //JSON中的一些字段不适合做作为字段，使用注解的方式建立映射关系

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
