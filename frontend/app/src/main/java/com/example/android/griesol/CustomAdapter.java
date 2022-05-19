package com.example.android.griesol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    public interface OnViewClickListener {
        void onImageClick(JSONObject object);
        void onViewClick(JSONObject object);
    }

    private final OnViewClickListener listener;
    private JSONArray jsonArrayDept;

    public CustomAdapter(JSONArray jsonArray, OnViewClickListener l) {
        jsonArrayDept = jsonArray;
        listener = l;
    }

    public void refresh(JSONArray jsonArray){
        this.jsonArrayDept = jsonArray;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = null;
        try {
            JSONObject obj = jsonArrayDept.getJSONObject(position);
            title = obj.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.titleView.setText(title);
        holder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listener.onImageClick(jsonArrayDept.getJSONObject(position));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(jsonArrayDept == null) return 0;
        else return jsonArrayDept.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        ImageView deleteView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        listener.onViewClick(jsonArrayDept.getJSONObject(getAdapterPosition()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            titleView = (TextView) itemView.findViewById(R.id.titleView);
            deleteView = (ImageView) itemView.findViewById(R.id.deleteView);
        }
    }
}
