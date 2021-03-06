package com.android.tu.tuweather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.tuweather.PlaceSelectActivity;
import com.android.tu.tuweather.R;
import com.android.tu.tuweather.adapter.AreaListAdapter;
import com.android.tu.tuweather.db.City;
import com.android.tu.tuweather.db.County;
import com.android.tu.tuweather.db.Location;
import com.android.tu.tuweather.db.Province;
import com.android.tu.tuweather.util.HttpUtil;
import com.android.tu.tuweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by tjy on 2017/2/28.
 */
public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE=0;

    public static final int LEVEL_CITY=1;

    public static final int LEVEL_COUNTY=2;

    private int currentLevel;  //当前选中的级别

    @BindView(R.id.title_text)
    TextView titleText;
    @BindView(R.id.img_back_btn)
    ImageButton backBtn;
    @BindView(R.id.area_list)
    ListView areaList;

    private ArrayAdapter<String> listAdapter;
    private List<String> datalist=new ArrayList<>();

    private List<Province> provinceList;

    private List<City> cityList;

    private List<County> countyList;

    private ProgressDialog progressDialog;

    private Province selectedProvince;

    private City selectedCity;
    private SweetAlertDialog progressSweetDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area_fragment,container,false);
        if(Build.VERSION.SDK_INT>=21){
            View decroView=getActivity().getWindow().getDecorView();
            decroView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ButterKnife.bind(this,view);
        listAdapter=new AreaListAdapter(getContext(),R.layout.arealist_item,datalist);
        areaList.setAdapter(listAdapter);
        return view;
    }

    @OnClick(R.id.img_back_btn)
    public void onButtonClick(){
        if(currentLevel==LEVEL_COUNTY){
            queryCity();
        }else if(currentLevel==LEVEL_CITY){
            queryProvince();
        }else if(currentLevel==LEVEL_PROVINCE){
            PlaceSelectActivity placeSelectActivity= (PlaceSelectActivity) getActivity();
            placeSelectActivity.returntoPlaceFragment();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);//获得屏幕的宽高i
        int mWidth= displayMetrics.widthPixels;
        int mHeight=displayMetrics.heightPixels;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        areaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(i);
                    queryCity();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(i);
                    queryCounty();
                }else if(currentLevel==LEVEL_COUNTY){
                    String selectCounty=countyList.get(i).getCountyName();
                    String selectWeatherId=countyList.get(i).getWeatherId();
                    Location location=new Location();
                    location.setOtherPlace(selectCounty);
                    location.setProvinceName(selectedProvince.getProvinceName());
                    location.setLocweatherid(countyList.get(i).getWeatherId());
                    SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String prefsLoc=prefs.getString("bdloc",null);
                    List<Location> locationList=new ArrayList<>();
                    locationList=DataSupport.where("otherplace=?",selectCounty).find(Location.class);
                    //判断添加的地区是否已经在数据库和sharepreference文件中存在，若存在则不重复添加
                    if(locationList.size()<1&&!selectCounty.equals(prefsLoc)){
                        location.save();
                    }
                    Intent intent=new Intent();
                    intent.putExtra("weather_id",selectWeatherId);
                    PlaceSelectActivity placeSelectActivity= (PlaceSelectActivity) getActivity();
                    placeSelectActivity.setResult(placeSelectActivity.RESULT_OK,intent);
                    placeSelectActivity.finish();
                    if(Build.VERSION.SDK_INT<23){
                        placeSelectActivity.overridePendingTransition(R.anim.in_left,R.anim.out_right);
                    }
                }
            }
        });
        queryProvince();
    }

    private void queryProvince() {
        titleText.setText("中国");
        backBtn.setVisibility(View.VISIBLE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            datalist.clear();
            for(Province mProvince:provinceList){
                datalist.add(mProvince.getProvinceName());
            }
            listAdapter.notifyDataSetChanged();
            areaList.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            String address="http://guolin.tech/api/china";
            queryFromServer(address,LEVEL_PROVINCE);
        }

    }


    private void queryCity() {
        titleText.setText(selectedProvince.getProvinceName());
        backBtn.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            datalist.clear();
            for (City mCity:cityList){
                datalist.add(mCity.getCityName());
            }
            listAdapter.notifyDataSetChanged();
            areaList.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int provinceId=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceId;
            queryFromServer(address,LEVEL_CITY);
        }

    }

    private void queryCounty() {
        titleText.setText(selectedCity.getCityName());
        backBtn.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            datalist.clear();
            for (County mCounty:countyList){
                datalist.add(mCounty.getCountyName());
            }
            listAdapter.notifyDataSetChanged();
            areaList.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int cityid=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+cityid;
            queryFromServer(address,LEVEL_COUNTY);
        }
    }

    private void queryFromServer(String address, final int level) {
        showProgressDialog();
        HttpUtil.sendOkhttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if(level==0){
                    result= Utility.handleProvinceResponse(responseText);
                }else if(level==1){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if(level==2){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if(level==LEVEL_PROVINCE){
                                queryProvince();
                            }else if(level==LEVEL_CITY){
                                queryCity();
                            }else if(level==LEVEL_COUNTY){
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }


    private void showProgressDialog() {

        if(progressSweetDialog==null){
            progressSweetDialog = new SweetAlertDialog(getContext(),SweetAlertDialog.PROGRESS_TYPE)
                    .setTitleText("加载中...")
                    .showContentText(false);
        }
        progressSweetDialog.show();
    }

    private void closeProgressDialog() {
        if(progressSweetDialog!=null){
            progressSweetDialog.dismiss();
        }
    }
}
