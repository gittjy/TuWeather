package com.android.tu.tuweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.tu.tuweather.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tjy on 2017/3/21.
 */
public class AddedAreaListAdapter extends ArrayAdapter<String>{

    private int resourceId;
    private ArrayList<String> mData;

    public AddedAreaListAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        resourceId=resource;
        mData= (ArrayList<String>) objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String tempData=mData.get(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,null);
        TextView addedAreaText= (TextView) view.findViewById(R.id.added_area_text);
        addedAreaText.setText(tempData);
        return view;
    }
}
