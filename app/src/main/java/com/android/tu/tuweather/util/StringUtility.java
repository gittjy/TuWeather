package com.android.tu.tuweather.util;

/**
 * Created by tjy on 2017/3/7.
 */
public class StringUtility {

    public static boolean isZero(String checkstring,int index){
        return checkstring.substring(index).startsWith("0");
    }



}
