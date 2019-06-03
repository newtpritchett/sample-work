package com.example.inspirationrewards;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

public class AwardAsyncTask   extends AsyncTask<String, Void, String> {
    private static final String baseUrl =
            "http://inspirationrewardsapi-env.6mmagpm2pv.us-east-2.elasticbeanstalk.com";
    private static final String awardURLExt ="/rewards";
    private AwardActivity awardActivity;

    public AwardAsyncTask(AwardActivity awardActivity) {
        this.awardActivity = awardActivity;
    }

    @Override
    protected void onPostExecute(String connectionResult) {
        awardActivity.toActivity(connectionResult);
    }


    @Override
    protected String doInBackground(String... params) {
        String targetStudentId = params[0];
        String targetUsername = params[1];
        String targetName = params[2];
        String targetDate = params[3];
        String targetNote = params[4];
        String targetValue = params[5];
        String userName = params[6];
        String password = params[7];
        String studentId = params[8];
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject target = new JSONObject();
            target.put("studentId", targetStudentId);
            target.put("username", targetUsername);
            target.put("name", targetName);
            target.put("date", targetDate);
            target.put("notes", targetNote);
            target.put("value", targetValue);
            JSONObject source = new JSONObject();
            source.put("studentId",studentId);
            source.put("username",userName);
            source.put("password",password);
            jsonObject.put("target", target);
            jsonObject.put("source", source);
            return doAPICall(jsonObject);
        } catch (Exception e) {

        }


        return null;
    }

    private String doAPICall(JSONObject jsonObject) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            StringBuilder result = new StringBuilder();
            String urlString = baseUrl + awardURLExt;
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

                if(result.toString().equals("Reward Added Successfully\n"))
                    return "SUCCESS";
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