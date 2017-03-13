package com.android.tu.tuweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.tu.tuweather.R;

import java.util.List;

/**
 * Created by tjy on 2017/3/11.
 */
public class AreaListAdapter extends ArrayAdapter<String>{
    private int resourceId;


    public AreaListAdapter(Context context, int textViewResourceId, List<String> objects) {
        super(context, textViewResourceId, objects);
        resourceId=textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String item=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(R.layout.arealist_item,null);
        TextView area= (TextView) view.findViewById(R.id.area_text);
        area.setText(item);
        return view;
    }
}
