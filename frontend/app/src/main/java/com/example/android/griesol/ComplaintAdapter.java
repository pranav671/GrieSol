package com.example.android.griesol;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder>{


    public interface OnViewClickListener {
        void onImageClick(JSONObject object);
        void onViewClick(JSONObject object);
        void onUpVoteClick(JSONObject object,Boolean isUpvoted);
//        void onDownVoteClick(JSONObject object);
    }

    private final OnViewClickListener listener;
    private JSONArray jsonArrayComplaint;
    private Context context;
    private String roll;

    public ComplaintAdapter(Context c, JSONArray jsonArray, OnViewClickListener l,String roll) {
        this.roll = roll;
        jsonArrayComplaint = jsonArray;
        listener = l;
        context = c;
    }

    public void refresh(JSONArray jsonArray){
        this.jsonArrayComplaint = jsonArray;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comlaint_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintAdapter.ViewHolder holder, int position) {
        String title = "";
        String venue = "";
        String asset = "";
        String date = "";
        String status = "";
        String image = "";
        String votes = "0";
        Boolean upVoted = false;
        JSONArray votelist = new JSONArray();
        boolean resolved_student;
        boolean resolved_department;
        try {
            JSONObject obj = jsonArrayComplaint.getJSONObject(position);

            title = obj.getString("detail");
            venue = obj.getString("venue");
            asset = obj.getString("asset");
            date = obj.getString("date");
            status = obj.getString("status");
            image = obj.getString("image");
            votes = "+" + obj.getInt("votes");
            resolved_student = obj.getBoolean("resolved_student");
            resolved_department = obj.getBoolean("resolved_department");
            votelist = obj.getJSONArray("votelist");
            if (resolved_department && resolved_student){
                status = "Resolved";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (title.length() > 22){
            title = title.substring(0,21) + "...";
        }
        holder.titleView.setText(title);
        holder.assetView.setText(asset);
        holder.venueView.setText(venue);
        holder.dateView.setText(date);
        holder.statusView.setText(status);

        switch (status){
            case "Addressed":
            case "Resolved":
                holder.statusView.setTextColor(Color.parseColor("#679F45"));
                break;
            case "Ongoing":
                holder.statusView.setTextColor(Color.parseColor("#F0864D"));
                break;
            case "Irrelevant":
                holder.statusView.setTextColor(Color.parseColor("#8B0000"));
                break;
            case "Not Started":
                holder.statusView.setTextColor(Color.parseColor("#42B1CD"));
                break;
        }
        holder.voteView.setText(votes);

        for (int i = 0; i < votelist.length(); i++) {
            String val = null;
            try {
                val = votelist.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (val.equals(roll)) {
                upVoted = true;
            }
        }

        if(upVoted){
            holder.upVoteView.setImageResource(R.drawable.green_upvote_button);
            holder.upVoteView.setClickable(false);
            holder.voteView.setTextColor(Color.parseColor("#679F45"));
            holder.downVoteView.setVisibility(View.INVISIBLE);
        }else{
            holder.upVoteView.setImageResource(R.drawable.white_upvote_button);
            holder.upVoteView.setClickable(true);
            holder.voteView.setTextColor(Color.parseColor("#ffffff"));
            holder.downVoteView.setVisibility(View.INVISIBLE);
        }

        Log.d("TAG", "onBindViewHolder: "+ this.roll);
        if(this.roll.equals("")){
            holder.upVoteView.setVisibility(View.INVISIBLE);
            holder.downVoteView.setVisibility(View.INVISIBLE);
        }


        Boolean finalUpVoted = upVoted;
        holder.upVoteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listener.onUpVoteClick(jsonArrayComplaint.getJSONObject(position), finalUpVoted);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

//        holder.downVoteView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    listener.onDownVoteClick(jsonArrayComplaint.getJSONObject(position));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listener.onImageClick(jsonArrayComplaint.getJSONObject(position));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        holder.imageView.setImageResource(R.mipmap.noimage);
        if(!image.equals("")){
            Picasso.get().cancelRequest(holder.imageView);
            Picasso.get().load(image)
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        if(jsonArrayComplaint == null) return 0;
        else return jsonArrayComplaint.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        ImageView imageView;
        TextView assetView;
        TextView venueView;
        TextView dateView;
        TextView statusView;
        TextView voteView;
        ImageView upVoteView;
        ImageView downVoteView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        listener.onViewClick(jsonArrayComplaint.getJSONObject(getAdapterPosition()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            titleView = (TextView) itemView.findViewById(R.id.item_complaint_title);
            assetView = (TextView) itemView.findViewById(R.id.item_complaint_asset);
            venueView = (TextView) itemView.findViewById(R.id.item_complaint_venue);
            dateView = (TextView) itemView.findViewById(R.id.item_complaint_date);
            statusView = (TextView) itemView.findViewById(R.id.item_complaint_status);
            imageView = (ImageView) itemView.findViewById(R.id.item_complaint_image);
            voteView = (TextView) itemView.findViewById(R.id.complaintVotes);
            upVoteView = (ImageView)itemView.findViewById(R.id.upVote);
            downVoteView = (ImageView)itemView.findViewById(R.id.downVote);
        }
    }
}
