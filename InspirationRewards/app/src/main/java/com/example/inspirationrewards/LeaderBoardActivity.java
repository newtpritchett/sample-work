package com.example.inspirationrewards;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderBoardActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private String username;
    private String password;
    private String studentId;
    private LeaderBoardAsyncTask leaderBoardAsyncTask;
    private UserInfo currentUserInfo;
    private List<UserInfo> users = new ArrayList<>();
    private RecyclerView recyclerView;
    private UserAdapter mAdapter;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        setTitle();

        recyclerView = findViewById(R.id.recycleLeaderBoard);
        progressBar = findViewById(R.id.progressBar);

        mAdapter = new UserAdapter(users, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUserInfo = (UserInfo) getIntent().getSerializableExtra("currentUserInfo");
        username = currentUserInfo.getUsername();
        password = currentUserInfo.getPassword();
        studentId = currentUserInfo.getStudentID();

        String[] params = new String[]{studentId, username, password};
        leaderBoardAsyncTask = new LeaderBoardAsyncTask(LeaderBoardActivity.this);
        leaderBoardAsyncTask.execute(params);

        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();

    }
    public void setTitle() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Inspiration Leaderboard");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_with_logo);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent data = new Intent();
                setResult(RESULT_OK, data);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }


    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        UserInfo m = users.get(pos);
        Intent intent = new Intent(LeaderBoardActivity.this, AwardActivity.class);
        intent.putExtra("targetedUserDetails", m);
        intent.putExtra("currentUserInfo", currentUserInfo);
        startActivityForResult(intent, 103);
    }


    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    public void setLeaderBoardParameters(List<UserInfo> userInfoList) {
        users.clear();
        if(userInfoList!=null) {
            Collections.sort(userInfoList);
        }
        users.addAll(userInfoList);
        mAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.INVISIBLE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 103) {
            if (resultCode == RESULT_OK) {
                users.clear();
                mAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);

                username = currentUserInfo.getUsername();
                password = currentUserInfo.getPassword();
                studentId = currentUserInfo.getStudentID();
                String[] parameters = new String[]{studentId, username, password};
                leaderBoardAsyncTask = new LeaderBoardAsyncTask(LeaderBoardActivity.this);
                leaderBoardAsyncTask.execute(parameters);
            }
        }
    }
}