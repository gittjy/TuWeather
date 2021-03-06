package com.android.tu.tuweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tjy on 2017/3/1.
 */
public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;

        public String code;
    }
}
