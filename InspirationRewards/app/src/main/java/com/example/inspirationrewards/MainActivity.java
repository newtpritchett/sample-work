package com.example.inspirationrewards;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private TextView create_profile_txt;
    private EditText login_name, login_password;
    private CheckBox checkBox;
    private ProgressBar progressBar;
    private LoginCredStorage loginCredStorage;
    private static final String TAG = "Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        Log.d(TAG, "super.onCreate called");
        setContentView(R.layout.activity_main);
        Log.d(TAG, "setContentView called");
        create_profile_txt = findViewById(R.id.newProfile);
        login_name = findViewById(R.id.inputUsername);
        login_password = findViewById(R.id.inputPassword);
        checkBox = findViewById(R.id.checkBoxAdmin);
        progressBar = findViewById(R.id.progressBar);

        if( (create_profile_txt == null) || (login_name == null) || (login_password == null)
        || (checkBox == null) || (progressBar == null)) {Log.d(TAG,"Layout Instantiation Failed");}

        Boolean connectStatus = checkConnection();
        if (connectStatus == false) {
            Log.d(TAG,"connectStatus == false");
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        } else{
        setTitle();
        loadData();}
    }

    public void onClickLogin(View v) {
        progressBar.setVisibility(View.VISIBLE);
        String name = login_name.getText().toString();
        String password = login_password.getText().toString();
        String[] params = new String[]{name, password};
        LoginAsyncTask loginAsyncTask = new LoginAsyncTask(this);
        loginAsyncTask.execute(params);
    }

    private void loadData() {
        loginCredStorage = new LoginCredStorage(this);
        if(loginCredStorage == null){Log.d(TAG, "loginCredStorage instantiation failed");}

        try {
            boolean isChecked = loginCredStorage.getBooleanValue("CHECKED");
        } catch(Error error) {
            Log.d(TAG, "loginCredStorage.getBooleanValue failed");
            boolean isChecked = false;
        }
        //if(isChecked) {
        if(true) {
            login_name.setText(loginCredStorage.getStringValue(getString(R.string.user_name_txt)));
            login_password.setText(loginCredStorage.getStringValue(getString(R.string.password_txt)));
            checkBox.setChecked(true);
        }
    }

    public void onClickCreateProfile(View v) {
        Log.d(TAG,"onClickCreateProfile was called");
        Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
        Log.d(TAG, "new Intent called");
        if(intent != null){Log.d(TAG, "Intent Instantiation Success");}
        try {startActivity(intent);} catch (Error error) {Log.d(TAG, "startActivity failed");}
    }

    public void onClickCheckBox(View v) {
        if(checkBox.isChecked()) {
            loginCredStorage.setValue(getString(R.string.user_name_txt), login_name.getText().toString());
            loginCredStorage.setValue(getString(R.string.password_txt), login_password.getText().toString());
            loginCredStorage.setBooleanValue(getString(R.string.checkbox_txt), true);
        } else {
            loginCredStorage.setValue(getString(R.string.user_name_txt), "");
            loginCredStorage.setValue(getString(R.string.password_txt), "");
            loginCredStorage.setBooleanValue(getString(R.string.checkbox_txt), false);
        }
    }
    public void setTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {Log.d(TAG,"getSupportActionBar Failed");}
        actionBar.setTitle("Rewards");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.icon);
    }
    public boolean checkConnection(){
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = networkInfo.isConnected();
        boolean isAvailable = networkInfo.isAvailable();

        if( isConnected|| isAvailable || networkInfo != null  ){
            return true;
        }
        return false;
    }

    public void redirectTOActivity(UserInfo userInfo, String status) {
        if(status.equalsIgnoreCase("SUCCESS")) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("userInfo", userInfo);
            progressBar.setVisibility(View.INVISIBLE);
            startActivity(intent);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            //AwardActivity.createToast(MainActivity.this, "Incorrect Password", Toast.LENGTH_SHORT);

        }


    }

}