package com.android.tu.tuweather;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.tu.tuweather.adapter.PlaceGridAdapter;
import com.android.tu.tuweather.fragment.ChooseAreaFragment;
import com.android.tu.tuweather.fragment.PlaceSelectFragment;
import com.android.tu.tuweather.model.PlaceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tjy on 2017/4/19.
 */
public class PlaceSelectActivity extends AppCompatActivity{


    List<PlaceItem> placeItemList=new ArrayList<>();
    private Context mContext;
    private PlaceGridAdapter placeGridAdapter;
    private FragmentManager fragmentManager;
    private ChooseAreaFragment chooseAreaFragment;

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
        mContext=this;
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        PlaceSelectFragment placeSelectFragment=new PlaceSelectFragment();
        fragmentTransaction.add(R.id.place_select_frame,placeSelectFragment);
        fragmentTransaction.commit();
    }

    public void addPlace(){
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        chooseAreaFragment = new ChooseAreaFragment();
        fragmentTransaction.add(R.id.place_select_frame, chooseAreaFragment);
        fragmentTransaction.commit();
    }

   public void returntoPlaceFragment(){
       FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
       fragmentTransaction.remove(chooseAreaFragment);
       fragmentTransaction.commit();
   }

}
