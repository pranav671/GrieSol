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

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    public interface OnViewClickListener {
        void onImageClick(JSONObject object);
    }

    private final OnViewClickListener listener;
    private JSONArray jsonArrayRequest;

    public RequestAdapter(JSONArray jsonArray, OnViewClickListener l) {
        jsonArrayRequest = jsonArray;
        listener = l;
    }

    public void refresh(JSONArray jsonArray){
        this.jsonArrayRequest = jsonArray;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String key = null;
        JSONArray value = new JSONArray();
        try {
            JSONObject obj = jsonArrayRequest.getJSONObject(position);
            key = obj.getString("key");
            value = obj.getJSONArray("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.requestKeyView.setText(key);
        holder.requestValueView.setText("");
        for (int i = 0; i < value.length(); i++) {
            String val = null;
            try {
                val = value.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (i == value.length()-1){
                holder.requestValueView.append((i+1) + ". " +val);
            }else{
                holder.requestValueView.append((i+1) + ". " +val + "\n");
            }
        }
        holder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listener.onImageClick(jsonArrayRequest.getJSONObject(position));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(jsonArrayRequest == null) return 0;
        else return jsonArrayRequest.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView requestKeyView;
        TextView requestValueView;
        ImageView deleteView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestKeyView = (TextView) itemView.findViewById(R.id.request_key);
            requestValueView = (TextView) itemView.findViewById(R.id.request_value);
            deleteView = (ImageView) itemView.findViewById(R.id.requestDeleteView);
        }
    }
}
