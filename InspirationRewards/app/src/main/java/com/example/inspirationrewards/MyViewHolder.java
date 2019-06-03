package com.example.inspirationrewards;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    public ImageView profile_picture;
    public TextView username;
    public TextView position;
    public TextView total_points;
    public MyViewHolder(View view) {
        super(view);
        profile_picture = (ImageView) view.findViewById(R.id.profilePicture);
        username = (TextView) view.findViewById(R.id.username);
        position = (TextView) view.findViewById(R.id.position);
        total_points = (TextView) view.findViewById(R.id.pointsReceived);
    }
}
