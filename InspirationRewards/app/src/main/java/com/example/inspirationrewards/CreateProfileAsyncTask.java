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
import java.util.ArrayList;
import java.util.List;
import static java.net.HttpURLConnection.HTTP_OK;

public class CreateProfileAsyncTask  extends AsyncTask<String, Void, String> {
    private static final String TAG = "CreateProfileAsyncTask";
    private static final String baseUrl =
            "http://inspirationrewardsapi-env.6mmagpm2pv.us-east-2.elasticbeanstalk.com";
    private static final String profileURLExt ="/profiles";
    private UserInfo userInfo;
    private CreateProfileActivity createProfileActivity;
    private String status = "SUCCESS";
    private String faultMessage = "ASYNC TASK ERROR";

    public CreateProfileAsyncTask(UserInfo userInfo, CreateProfileActivity createProfileActivity) {
        this.userInfo = userInfo;
        this.createProfileActivity = createProfileActivity;
    }

    @Override
    protected void onPostExecute(String connectionResult) {
        if(status == null || status.equalsIgnoreCase("FAIL")){
            createProfileActivity.redirectTOActivity(status, faultMessage);
        } else {
            Log.d(TAG, "onPostExecute: " + userInfo.toString());
            createProfileActivity.redirectTOActivity(userInfo);
        }
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("studentId", "A20392360");
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
            String webAddress = baseUrl + profileURLExt;

            Uri uri = Uri.parse(webAddress);
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

                JSONObject jsonObject1 = new JSONObject(result.toString());
                JSONObject err = jsonObject1.getJSONObject("errordetails");
                String errorString = err.getString("status");
                if(errorString.equalsIgnoreCase("BAD_REQUEST")) {
                    faultMessage = err.getString("faultMessage");
                }
                status = "FAIL";
                return null;
            }
        } catch (Exception e) {
            status = "FAIL";

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
        status = "FAIL";
        return "API call error";
    }

    private void parseJSON(String s) {

        try {
            JSONObject jObjMain = new JSONObject(s);
            List<RewardsInfo> rArr = new ArrayList<>();
            if(jObjMain.has("rewards")){
                JSONArray rewardsArr = jObjMain.getJSONArray("rewards");
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
