package com.android.tu.tuweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by tjy on 2017/2/28.
 */
public class HttpUtil {

    /**
     * 发送服务器请求
     * @param address url地址
     * @param callback 注册回调函数返回可得到服务器返回的请求结果
     */

    public static void sendOkhttpRequest(String address,okhttp3.Callback callback){

        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

}
