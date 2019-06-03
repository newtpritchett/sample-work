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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements View.OnLongClickListener, View.OnClickListener {

    private List<RewardsInfo> historyList = new ArrayList<>();
    private RecyclerView recyclerView;
    private HistoryAdapter mAdapter;
    private TextView username;
    private TextView user_id;
    private TextView location;
    private TextView points_awarded;
    private TextView department;
    private TextView position;
    private TextView points_to_award;
    private TextView story;
    private TextView reward_history;
    private ImageView profile_picture;
    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username = findViewById(R.id.username);
        user_id = findViewById(R.id.userID);
        location = findViewById(R.id.location);
        points_awarded = findViewById(R.id.pointsAwarded);
        department = findViewById(R.id.department);
        position = findViewById(R.id.position);
        points_to_award = findViewById(R.id.pointsToAward);
        story = findViewById(R.id.story);
        reward_history = findViewById(R.id.rewardHistory);
        profile_picture = findViewById(R.id.profilePicture);



        setTitle();
        recyclerView = findViewById(R.id.recyclerHistory);
        mAdapter = new HistoryAdapter(historyList, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userInfo = (UserInfo) getIntent().getSerializableExtra("userInfo");
        setUserProfile(userInfo);
        if(userInfo != null)
            historyList.addAll(userInfo.getRewards());
    }

    private void setUserProfile(UserInfo userInfo) {

        username.setText(userInfo.getLast_name() + ", " + userInfo.getFirst_name());
        user_id.setText("(" + userInfo.getUsername() + ")");
        location.setText(userInfo.getLocation());
        points_awarded.setText(String.valueOf(userInfo.getPoints()));
        department.setText(userInfo.getDepartment());
        position.setText(userInfo.getPosition());
        points_to_award.setText(String.valueOf(userInfo.getPoints_to_award()));
        story.setText(userInfo.getStory());
        reward_history.setText("Reward History("+ userInfo.getRewards().size()+"): ");
        try {
            Bitmap bitmap = StringToBitMap(userInfo.getImage());
            profile_picture.setImageBitmap(bitmap);
        } catch (Exception e){

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.edit_icon:
                intent = new Intent(  ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("userInfo", userInfo);
                startActivityForResult(intent, 102);
                return true;
            case R.id.leaderboard_menu_icon:
                intent = new Intent(  ProfileActivity.this, LeaderBoardActivity.class);
                intent.putExtra("loggedInUserDetails", userInfo);
                startActivityForResult(intent, 101);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void setTitle() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Your Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.icon);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onActivityResult(int requestNum, int result, Intent data) {
        if (requestNum == 101) {
            if (result == RESULT_OK) {
                LoginAsyncTask loginAsyncTask = new LoginAsyncTask(ProfileActivity.this);
                String[] parameter = new String[]{userInfo.getUsername(), userInfo.getPassword()};
                loginAsyncTask.execute(parameter);

            } else {
            }

        } else if (requestNum == 102) {
            if (result == RESULT_OK) {
                LoginAsyncTask loginAsyncTask = new LoginAsyncTask(ProfileActivity.this);
                String[] param = new String[]{userInfo.getUsername(), userInfo.getPassword()};
                loginAsyncTask.execute(param);
            } else {
            }

        }

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

    public void resetUserDetails(UserInfo userInfo, String status) {
        if(status.equalsIgnoreCase("SUCCESS")) {
            this.userInfo = userInfo;
            setUserProfile(userInfo);
        } else {
            AwardActivity.createToast(
                    ProfileActivity.this, "Incorrect Password", Toast.LENGTH_LONG);

        }
    }



}