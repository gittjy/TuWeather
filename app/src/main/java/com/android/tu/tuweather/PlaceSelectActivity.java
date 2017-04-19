package com.android.tu.tuweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.android.tu.tuweather.adapter.PlaceGridAdapter;
import com.android.tu.tuweather.db.Location;
import com.android.tu.tuweather.model.PlaceItem;
import com.lljjcoder.citypickerview.widget.CityPicker;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tjy on 2017/4/19.
 */
public class PlaceSelectActivity extends AppCompatActivity{

    @BindView(R.id.place_select_recycle)
    RecyclerView placeRecyclerView;

    List<PlaceItem> placeItemList=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.place_select_layout);
        ButterKnife.bind(this);
        initData();
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,3);
        placeRecyclerView.setLayoutManager(gridLayoutManager);
        PlaceGridAdapter placeGridAdapter=new PlaceGridAdapter(placeItemList);
        placeGridAdapter.setmPlaceItemClickListener(new PlaceGridAdapter.placeItemClickListener() {
            @Override
            public void itemClick(int pos) {

            }
        });
        placeGridAdapter.setmAddItemClickListener(new PlaceGridAdapter.addItemClickListener() {
            @Override
            public void itemClick(int pos) {
                showCityPicker();
            }
        });

        placeRecyclerView.setAdapter(placeGridAdapter);
    }

    private void showCityPicker() {
        CityPicker cityPicker = new CityPicker.Builder(PlaceSelectActivity.this)
                .textSize(20)
                .title("地址选择")
                .backgroundPop(0x000000000)
                .titleBackgroundColor("#ffffff")
                .titleTextColor("#000000")
                .confirTextColor("#000000")
                .cancelTextColor("#000000")
                .province("北京")
                .city("北京市")
                .district("海淀区")
                .textColor(Color.parseColor("#000000"))
                .provinceCyclic(true)
                .cityCyclic(false)
                .districtCyclic(false)
                .visibleItemsCount(7)
                .itemPadding(10)
                .onlyShowProvinceAndCity(false)
                .build();
        cityPicker.show();

        //监听方法，获取选择结果
        cityPicker.setOnCityItemClickListener(new CityPicker.OnCityItemClickListener() {
            @Override
            public void onSelected(String... citySelected) {
                String selectCounty = citySelected[2].substring(0,citySelected[2].length()-1);
                Location location=new Location();
                location.setUserLocation(selectCounty);
                SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(PlaceSelectActivity.this);
                String prefsLoc=prefs.getString("bdloc",null);
                List<Location> locationList=new ArrayList<>();
                locationList= DataSupport.where("userlocation=?",selectCounty).find(Location.class);
                //判断添加的地区是否已经在数据库和sharepreference文件中存在，若存在则不重复添加
                if(locationList.size()<1&&!selectCounty.equals(prefsLoc)){
                    location.save();
                }
                //requestWeather(selectCounty);
            }

            @Override
            public void onCancel() {
                Toast.makeText(PlaceSelectActivity.this, "已取消", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initData() {
        placeItemList.add(new PlaceItem("海淀","晴"));
        placeItemList.add(new PlaceItem("巴东","晴"));
        placeItemList.add(new PlaceItem("哈尔滨","多云"));
        placeItemList.add(new PlaceItem("广州","小雨"));
        placeItemList.add(new PlaceItem("武汉","晴"));
        placeItemList.add(new PlaceItem("深圳","晴"));
    }

}
