package com.andoid.tk.mymap;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by TK on 3/29/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
{

    private List<Data> mData = Collections.emptyList();
    private LayoutInflater mLayoutInflater;
    private ItemClickListener mItemClickListener;

    public RecyclerViewAdapter(Context context, List<Data> mData) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mData = mData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.address_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Data data = mData.get(position);
        holder.tvOrder.setText(data.getOrder());
        holder.tvName.setText(data.getPlaceName());
        holder.tvDistance.setText(data.getKm());
        holder.tvDuration.setText(data.getDuration());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public Data getItem(int id)
    {
        return mData.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener)
    {
        this.mItemClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        protected TextView tvOrder;
        protected TextView tvName;
        protected TextView tvDistance;
        protected TextView tvDuration;

        public ViewHolder(View itemView) {
            super(itemView);
            tvOrder = (TextView)itemView.findViewById(R.id.tvOrder);
            tvName = (TextView)itemView.findViewById(R.id.tvName);
            tvDistance = (TextView) itemView.findViewById(R.id.distanceToDevice);
            tvDuration = (TextView) itemView.findViewById(R.id.durationToDevice);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mItemClickListener != null)
                mItemClickListener.onItemClick(view,getAdapterPosition());
        }
    }
}
