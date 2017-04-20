package com.android.tu.tuweather;

import android.content.Context;
import android.content.Intent;
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
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by tjy on 2017/4/19.
 */
public class PlaceSelectActivity extends AppCompatActivity{

    @BindView(R.id.place_select_recycle)
    RecyclerView placeRecyclerView;

    List<PlaceItem> placeItemList=new ArrayList<>();
    private Context mContext;
    private PlaceGridAdapter placeGridAdapter;

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
        mContext=this;
        initData();
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,3);
        placeRecyclerView.setLayoutManager(gridLayoutManager);
        placeGridAdapter = new PlaceGridAdapter(placeItemList);
        placeGridAdapter.setmPlaceItemClickListener(new PlaceGridAdapter.placeItemClickListener() {
            @Override
            public void itemClick(int pos) {
                Intent intent=new Intent();
                intent.putExtra("place_name",placeItemList.get(pos).getPlaceName());
                setResult(RESULT_OK,intent);
                finish();
                if(Build.VERSION.SDK_INT<23){
                    overridePendingTransition(R.anim.in_left,R.anim.out_right);
                }
            }

            @Override
            public void itemLongClick(final int pos) {
                if(pos!=0){
                    SweetAlertDialog sweetAlertDialog=new SweetAlertDialog(mContext,SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("是否删除选中区域")
                            .showContentText(false)
                            .setConfirmText("是")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    DataSupport.deleteAll(Location.class,"otherplace=?",placeItemList.get(pos).getPlaceName());
                                    placeItemList.remove(pos);
                                    placeGridAdapter.notifyItemRemoved(pos);
                                    sweetAlertDialog.dismiss();
                                }
                            });
                    sweetAlertDialog.setCanceledOnTouchOutside(true);
                    sweetAlertDialog.show();
                }
                else{
                    SweetAlertDialog sweetAlertDialog=new SweetAlertDialog(mContext,SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("定位区域不可删除!")
                            .showContentText(false)
                            .setConfirmText("知道了")
                            .setConfirmClickListener(null);
                    sweetAlertDialog.show();
                }
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
                String selectProvince=citySelected[0];
                String selectCounty = citySelected[2].substring(0,citySelected[2].length()-1);
                Location location=new Location();
                location.setOtherPlace(selectCounty);
                location.setProvinceName(selectProvince);
                SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(PlaceSelectActivity.this);
                String prefsLoc=prefs.getString("bdloc",null);
                List<Location> locationList=new ArrayList<>();
                locationList= DataSupport.where("otherplace=?",selectCounty).find(Location.class);
                //判断添加的地区是否已经在数据库和sharepreference文件中存在，若存在则不重复添加
                if(locationList.size()<1&&!selectCounty.equals(prefsLoc)){
                    location.save();
                    placeItemList.add(placeItemList.size()-1,new PlaceItem(selectCounty,selectProvince));
                    placeGridAdapter.notifyItemInserted(placeItemList.size()-2);
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
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(mContext);
        String locPlaceName=prefs.getString("bdloc","null");
        placeItemList.add(new PlaceItem(locPlaceName,""));
        ArrayList<Location> locationList= (ArrayList<Location>) DataSupport.findAll(Location.class);
        if(locationList.size()>0){
            for (int i = 0; i <locationList.size() ; i++) {
                placeItemList.add(new PlaceItem(locationList.get(i).getOtherPlace(),locationList.get(i).getProvinceName()));
            }
        }
        placeItemList.add(new PlaceItem("",""));
    }

}
