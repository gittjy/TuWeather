package com.android.tu.tuweather.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.tu.tuweather.R;
import com.android.tu.tuweather.model.PlaceItem;

import java.util.List;

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

        ImageView locImage;


        public ViewHolder(View itemView) {
            super(itemView);
            placeName= (TextView) itemView.findViewById(R.id.place_name);
            placeWeather= (TextView) itemView.findViewById(R.id.place_weather);
            placeInfoLinear= (LinearLayout) itemView.findViewById(R.id.place_info_linear);
            imageLinear= (LinearLayout) itemView.findViewById(R.id.add_image);
            locImage= (ImageView) itemView.findViewById(R.id.place_loc_image);
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
        viewHolder.placeInfoLinear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int pos=viewHolder.getAdapterPosition();
                mPlaceItemClickListener.itemLongClick(pos);
                return false;
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if(position!=placeItemList.size()-1){
            if(position==0){
                holder.locImage.setVisibility(View.VISIBLE);
                holder.placeWeather.setVisibility(View.GONE);
            }else{
                holder.locImage.setVisibility(View.GONE);
                holder.placeWeather.setVisibility(View.VISIBLE);
            }
            PlaceItem placeItem=placeItemList.get(position);
            holder.placeName.setText(placeItem.getPlaceName());
            holder.placeWeather.setText(placeItem.getPlaceProvince());
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

    /**
     * 区域item的点击事件接口
     */

    public interface placeItemClickListener{

        void itemClick(int pos);

        void itemLongClick(int pos);
    }

    /**
     * 添加item的点击事件接口
     */
    public interface  addItemClickListener{
        void itemClick(int pos);
    }
    
}
