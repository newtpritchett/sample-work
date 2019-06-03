package com.example.inspirationrewards;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private LeaderBoardActivity leaderBoardActivity;
    private List<UserInfo> users;

    public UserAdapter(List<UserInfo> users, LeaderBoardActivity leaderBoardActivity) {
        this.users = users;
        this.leaderBoardActivity = leaderBoardActivity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.leader, parent, false);

        itemView.setOnClickListener(leaderBoardActivity);
        itemView.setOnLongClickListener(leaderBoardActivity);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UserInfo user = users.get(position);
        holder.username.setText(user.getLast_name() + ", " + user.getFirst_name());
        holder.position.setText(user.getPosition());
        holder.total_points.setText(String.valueOf(user.getPoints()));
        Bitmap bitmap = StringToBitMap(user.getImage());
        holder.profile_picture.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public Bitmap StringToBitMap(String bitString) {
        try {
            byte[] encodeByte = Base64.decode(bitString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0,encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}