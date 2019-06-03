package com.example.inspirationrewards;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class HistoryViewHolder extends RecyclerView.ViewHolder {
    public TextView reward_sender;
    public TextView points;
    public TextView date;
    public TextView comments;
    public HistoryViewHolder(@NonNull View view) {
        super(view);
        reward_sender = view.findViewById(R.id.rewardSender);
        points = view.findViewById(R.id.points);
        date = view.findViewById(R.id.date);
        comments = view.findViewById(R.id.comments);
    }
}
