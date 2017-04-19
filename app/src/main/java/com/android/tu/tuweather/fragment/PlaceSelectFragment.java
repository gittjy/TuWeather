package com.android.tu.tuweather.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.tu.tuweather.R;
import com.android.tu.tuweather.adapter.PlaceGridAdapter;
import com.android.tu.tuweather.model.PlaceItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tjy on 2017/4/18.
 */
public class PlaceSelectFragment extends Fragment{

    @BindView(R.id.place_select_recycle)
    RecyclerView placeRecyclerView;

    List<PlaceItem> placeItemList=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.place_select_layout,container,false);
        ButterKnife.bind(this,view);
        initData();
        GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),3);
        placeRecyclerView.setLayoutManager(gridLayoutManager);
        PlaceGridAdapter placeGridAdapter=new PlaceGridAdapter(placeItemList);
        placeRecyclerView.setAdapter(placeGridAdapter);
        return view;
    }

    private void initData() {
        placeItemList.add(new PlaceItem("123","1"));
        placeItemList.add(new PlaceItem("123","1"));
        placeItemList.add(new PlaceItem("123","1"));
        placeItemList.add(new PlaceItem("123","1"));
        placeItemList.add(new PlaceItem("123","1"));
    }
}
