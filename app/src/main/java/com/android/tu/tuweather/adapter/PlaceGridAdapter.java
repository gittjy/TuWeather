package com.android.tu.tuweather.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.tuweather.R;
import com.android.tu.tuweather.model.PlaceItem;

import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by tjy on 2017/4/18.
 */
public class PlaceGridAdapter extends RecyclerView.Adapter<PlaceGridAdapter.ViewHolder>{


    private List<PlaceItem> placeItemList;

    private placeItemClickListener mPlaceItemClickListener;

    private addItemClickListener mAddItemClickListener;

    public void setmPlaceItemClickListener(PlaceGridAdapter.placeItemClickListener mPlaceItemClickListener) {
        this.mPlaceItemClickListener = mPlaceItemClickListener;
    }

    public void setmAddItemClickListener(PlaceGridAdapter.addItemClickListener mAddItemClickListener) {
        this.mAddItemClickListener = mAddItemClickListener;
    }
    

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView placeName;

        TextView placeWeather;

        LinearLayout placeInfoLinear;

        LinearLayout imageLinear;


        public ViewHolder(View itemView) {
            super(itemView);
            placeName= (TextView) itemView.findViewById(R.id.place_name);
            placeWeather= (TextView) itemView.findViewById(R.id.place_weather);
            placeInfoLinear= (LinearLayout) itemView.findViewById(R.id.place_info_linear);
            imageLinear= (LinearLayout) itemView.findViewById(R.id.add_image);
        }
    }

    public PlaceGridAdapter(List<PlaceItem> placeItems){
        placeItemList=placeItems;
    }


    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.place_grid_item,parent,false);
        final ViewHolder viewHolder=new ViewHolder(view);
        viewHolder.placeInfoLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos=viewHolder.getAdapterPosition();
                if(pos!=placeItemList.size()-1){
                    mPlaceItemClickListener.itemClick(pos);
                    Toasty.success(view.getContext(),"success", Toast.LENGTH_SHORT).show();
                }

            }
        });
        viewHolder.imageLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos=viewHolder.getAdapterPosition();
                if(pos==placeItemList.size()-1){
                    mAddItemClickListener.itemClick(pos);
                }

            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if(position!=placeItemList.size()-1){
            PlaceItem placeItem=placeItemList.get(position);
            holder.placeName.setText(placeItem.getPlaceName());
            holder.placeWeather.setText(placeItem.getPlaceWeather());
            holder.placeInfoLinear.setVisibility(View.VISIBLE);
            holder.imageLinear.setVisibility(View.GONE);
        }else{
            holder.placeInfoLinear.setVisibility(View.GONE);
            holder.imageLinear.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return placeItemList.size();
    }

    public interface placeItemClickListener{
        void itemClick(int pos);
    }

    public interface  addItemClickListener{
        void itemClick(int pos);
    }
    
}
