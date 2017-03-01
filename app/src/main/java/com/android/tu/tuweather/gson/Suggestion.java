package com.android.tu.tuweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tjy on 2017/3/1.
 */
public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    public class Comfort {
        @SerializedName("txt")
        public String info;
    }

    @SerializedName("cw")
    public CarWash carWash;

    public class CarWash {
        @SerializedName("txt")
        public String info;
    }

    public Sport sport;

    public class Sport {
        @SerializedName("txt")
        public String info;
    }
}
