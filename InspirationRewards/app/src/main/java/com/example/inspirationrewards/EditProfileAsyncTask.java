package com.example.inspirationrewards;

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

import static java.net.HttpURLConnection.HTTP_OK;

public class EditProfileAsyncTask  extends AsyncTask<String, Void, String> {
    private static final String TAG = "CreateProfileAsyncTask";
    private static final String baseUrl =
            "http://inspirationrewardsapi-env.6mmagpm2pv.us-east-2.elasticbeanstalk.com";
    private static final String profileURLExt ="/profiles";
    private UserInfo userInfo;
    private EditProfileActivity editProfileActivity;

    public EditProfileAsyncTask(UserInfo userInfo, EditProfileActivity editProfileActivity) {
        this.userInfo = userInfo;
        this.editProfileActivity = editProfileActivity;
    }

    @Override
    protected void onPostExecute(String connectionResult) {
        editProfileActivity.redirectTOActivity(connectionResult);
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("studentId", userInfo.getStudentID());
            jsonObject.put("username", userInfo.getUsername());
            jsonObject.put("password", userInfo.getPassword());

            jsonObject.put("firstName", userInfo.getFirst_name());
            jsonObject.put("lastName", userInfo.getLast_name());

            jsonObject.put("pointsToAward", userInfo.getPoints_to_award());
            jsonObject.put("department", userInfo.getDepartment());
            jsonObject.put("story", userInfo.getStory());
            jsonObject.put("position", userInfo.getPosition());
            jsonObject.put("admin", userInfo.getAdmin());
            jsonObject.put("location", userInfo.getLocation());
            jsonObject.put("imageBytes", userInfo.getImage());
            JSONArray rewards = new JSONArray();

            for(RewardsInfo r : userInfo.getRewards()){
                JSONObject rJSON = new JSONObject();
                rJSON.put("name", r.getReward_sender());
                rJSON.put("date", r.getDate());
                rJSON.put("notes", r.getComments());
                rJSON.put("value", r.getPoints());
                rewards.put(rJSON);
            }
            jsonObject.put("rewardRecords", rewards);

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
            String urlString = baseUrl + profileURLExt;
            Uri uri = Uri.parse(urlString);
            URL url = new URL(uri.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
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

                if(result.toString().equals("Profile Updated\n")){
                    return "SUCCESS";
                } else
                    return "Async Task Error";

            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    result.append(line).append("\n");
                }
                return "Async Task Error";
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
}