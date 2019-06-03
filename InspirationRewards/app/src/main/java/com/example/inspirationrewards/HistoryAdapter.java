package com.example.inspirationrewards;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    private ProfileActivity profileActivity;
    private List<RewardsInfo> historyList;

    public HistoryAdapter(List<RewardsInfo> historyList, ProfileActivity profileActivity) {
        this.historyList = historyList;
        this.profileActivity = profileActivity;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history, parent, false);

        itemView.setOnClickListener(profileActivity);
        itemView.setOnLongClickListener(profileActivity);

        return new HistoryViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        RewardsInfo r = historyList.get(position);
        holder.date.setText(r.getDate());
        holder.reward_sender.setText(r.getReward_sender());
        holder.points.setText(r.getPoints());
        holder.comments.setText(r.getComments());
    }
}
