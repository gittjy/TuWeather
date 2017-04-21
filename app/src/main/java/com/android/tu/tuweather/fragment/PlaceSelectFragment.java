package com.android.tu.tuweather.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.tu.topbarlibrary.NormalTopBar;
import com.android.tu.tuweather.PlaceSelectActivity;
import com.android.tu.tuweather.R;
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
 * Created by tjy on 2017/4/18.
 */
public class PlaceSelectFragment extends Fragment{

    @BindView(R.id.place_select_recycle)
    RecyclerView placeRecyclerView;
    @BindView(R.id.place_fragment_title)
    NormalTopBar normalTopBar;

    List<PlaceItem> placeItemList=new ArrayList<>();
    private PlaceGridAdapter placeGridAdapter;

    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.place_select_fragment,container,false);
        if(Build.VERSION.SDK_INT>=21){
            View decroView=getActivity().getWindow().getDecorView();
            decroView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ButterKnife.bind(this,view);
        normalTopBar.setTopClickListener(new NormalTopBar.normalTopClickListener() {
            @Override
            public void onLeftClick(View view) {
                getActivity().finish();
            }

            @Override
            public void onRightClick(View view) {

            }
        });
        mContext=getContext();
        initData();
        GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),3);
        placeRecyclerView.setLayoutManager(gridLayoutManager);
        placeGridAdapter = new PlaceGridAdapter(placeItemList);
        placeGridAdapter.setmPlaceItemClickListener(new PlaceGridAdapter.placeItemClickListener() {
            @Override
            public void itemClick(int pos) {
                Intent intent=new Intent();
                intent.putExtra("place_name",placeItemList.get(pos).getPlaceName());
                PlaceSelectActivity placeSelectActivity= (PlaceSelectActivity) getActivity();
                placeSelectActivity.setResult(placeSelectActivity.RESULT_OK,intent);
                placeSelectActivity.finish();
                if(Build.VERSION.SDK_INT<23){
                    placeSelectActivity.overridePendingTransition(R.anim.in_left,R.anim.out_right);
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
                //showCityPicker();
                PlaceSelectActivity placeSelectActivity= (PlaceSelectActivity) getActivity();
                placeSelectActivity.addPlace();
            }
        });
        placeRecyclerView.setAdapter(placeGridAdapter);
        return view;
    }

    private void showCityPicker() {
        CityPicker cityPicker = new CityPicker.Builder(mContext)
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
                SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(mContext);
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
                Toast.makeText(mContext, "已取消", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initData() {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(mContext);
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
