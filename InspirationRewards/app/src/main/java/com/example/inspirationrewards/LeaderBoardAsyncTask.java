package com.example.inspirationrewards;

import android.net.Uri;
import android.os.AsyncTask;
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

public class LeaderBoardAsyncTask extends AsyncTask<String, Void, String> {
    private String studentID = "A20392360";
    private static final String baseUrl =
            "http://inspirationrewardsapi-env.6mmagpm2pv.us-east-2.elasticbeanstalk.com";
    private static final String leaderBoardURLExt = "/allprofiles";
    private List<UserInfo> users = new ArrayList<>();

    private LeaderBoardActivity leaderBoardActivity;

    public LeaderBoardAsyncTask(LeaderBoardActivity leaderBoardActivity) {
        this.leaderBoardActivity = leaderBoardActivity;
    }

    @Override
    protected void onPostExecute(String connectionResult) {
        leaderBoardActivity.setLeaderBoardParameters(users);
    }

    @Override
    protected String doInBackground(String... strings) {
        String username = strings[1];
        String password = strings[2];

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("studentId", studentID);
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            return doAPICall(jsonObject);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseJSON(String s) {

        try {
            JSONArray jsonArray = new JSONArray(s);
            for (int m = 0; m < jsonArray.length(); m++) {
                JSONObject jObjMain = (JSONObject) jsonArray.get(m);
                List<RewardsInfo> rArr = new ArrayList<>();
                if (jObjMain.optJSONArray("rewards") != null) {
                    JSONArray rewardsArr = jObjMain.getJSONArray("rewards");
                    for (int n = 0; n < rewardsArr.length(); n++) {
                        JSONObject o = (JSONObject) rewardsArr.get(n);
                        rArr.add(new RewardsInfo(
                                o.getString("studentId"),
                                o.getString("username"),
                                o.getString("date"),
                                o.getString("name"),
                                o.getString("value"),
                                o.getString("notes")
                        ));
                    }
                }

                UserInfo userInfo = new UserInfo(
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
                users.add(userInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private String doAPICall(JSONObject jsonObject) {
        HttpURLConnection connection = null;
        BufferedReader bufRead = null;

        try {
            StringBuilder result = new StringBuilder();
            String urlString = baseUrl + leaderBoardURLExt;
            Uri uri = Uri.parse(urlString);
            URL url = new URL(uri.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            OutputStreamWriter stream = new OutputStreamWriter(connection.getOutputStream());
            stream.write(jsonObject.toString());
            stream.close();

            if (connection.getResponseCode() == HTTP_OK) {

                bufRead = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while (null != (line = bufRead.readLine())) {  //this could be improved
                    result.append(line).append("\n");
                }
                parseJSON(result.toString());
                return null;

            } else {
                bufRead = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                while (null != (line = bufRead.readLine())) {
                    result.append(line).append("\n");
                }
                return null;
            }

        } catch (Exception e) {

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (bufRead != null) {
                try {
                    bufRead.close();
                } catch (IOException e) {
                }
            }
        }
        return "Async Task Error";
    }
}