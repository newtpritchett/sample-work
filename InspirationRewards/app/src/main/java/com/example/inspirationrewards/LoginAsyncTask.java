package com.example.inspirationrewards;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

public class LoginAsyncTask extends AsyncTask<String, Void, String> {
    private static final String baseUrl =
            "http://inspirationrewardsapi-env.6mmagpm2pv.us-east-2.elasticbeanstalk.com";
    private static final String loginURLExt ="/login";
    private String status = "SUCCESS";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private ProfileActivity profileActivity;
    private EditProfileActivity editProfileActivity;

    private UserInfo userInfo;

    public LoginAsyncTask(MainActivity mainActivity) {

        this.mainActivity = mainActivity;
    }

    public LoginAsyncTask(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
    }

    public LoginAsyncTask(EditProfileActivity editProfileActivity) {
        this.editProfileActivity = editProfileActivity;
    }

    @Override
    protected void onPostExecute(String connectionResult) {
        if(mainActivity != null)
            mainActivity.redirectTOActivity(userInfo, status);
        else if(profileActivity != null)
            profileActivity.resetUserDetails(userInfo, status);
        else if(editProfileActivity != null)
            editProfileActivity.resetUserDetails(userInfo, status);
    }

    @Override
    protected String doInBackground(String... strings) {
        String studentID = "A20392360";
        String uName = strings[0];
        String password = strings[1];

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("studentId", studentID);
            jsonObject.put("username", uName);
            jsonObject.put("password", password);
            return doAPICall(jsonObject);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String doAPICall(JSONObject jsonObject) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            StringBuilder result = new StringBuilder();
            String urlString = baseUrl + loginURLExt;
            Uri uri = Uri.parse(urlString);
            URL url = new URL(uri.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("cache-control", "no-cache");
            connection.connect();

            OutputStreamWriter stream = new OutputStreamWriter(connection.getOutputStream());
            stream.write(jsonObject.toString());
            stream.close();

            if (connection.getResponseCode() == HTTP_OK) {

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    result.append(line).append("\n");
                }
                parseJSON(result.toString());
                return null;

            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    result.append(line).append("\n");
                }
                JSONObject j = new JSONObject(result.toString());
                JSONObject err = j.getJSONObject("errordetails");
                String s = err.getString("status");
                if(s.equalsIgnoreCase("UNAUTHORIZED")) {
                    status = "FAIL";
                }
                return null;
            }

        } catch (Exception e) {

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return "Async Task Error";
    }


    private void parseJSON(String s) {
        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray rewardsArr = jObjMain.getJSONArray("rewards");
            List<RewardsInfo> rArr = new ArrayList<>();
            for (int i = 0; i < rewardsArr.length(); i++){
                JSONObject o = (JSONObject) rewardsArr.get(i);
                rArr.add(new RewardsInfo(
                        o.getString("studentId"),
                        o.getString("username"),
                        o.getString("date"),
                        o.getString("name"),
                        o.getString("value"),
                        o.getString("notes")
                ));
            }
            userInfo = new UserInfo(
                    jObjMain.getString("studentId"),
                    jObjMain.getString("firstName"),
                    jObjMain.getString("lastName"),
                    jObjMain.getString("username"),
                    jObjMain.getString("department"),
                    jObjMain.getString("story"),
                    jObjMain.getString("position"),
                    jObjMain.getString("password"),
                    Integer.parseInt(jObjMain.getString("pointsToAward")),
                    jObjMain.getString("admin").equalsIgnoreCase("true"),
                    jObjMain.getString("location"),
                    jObjMain.getString("imageBytes"),
                    rArr
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}