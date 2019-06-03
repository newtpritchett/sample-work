package com.example.stockwatch;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class NameDownloader extends AsyncTask<String,Void,String> {
    private static final String TAG = "NameDownloader";
    private static final String inBG = "doInBackground: ";

    private MainActivity mainAct;
    private final String URLStub = "http://d.yimg.com/aq/autoc?region=US&lang=en-US&query=";
    private String userInputSymbol;

    public NameDownloader(MainActivity main) {
        mainAct = main;
    }

    @Override
    protected String doInBackground(String... strings) {
        userInputSymbol = strings[0];
        String address = URLStub + strings[0];
        address = Uri.parse(address).toString();

        Log.d(TAG, inBG+address);

        StringBuilder stringBuilder = new StringBuilder();
        String result = null;
        try {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream stream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String line;
            while((line=reader.readLine()) != null){
                stringBuilder.append(line).append('\n');
            }

            result = stringBuilder.toString();
            result = result.substring(result.indexOf("["),result.indexOf("]")+1);

            Log.d(TAG, inBG+result);
        } catch (Exception exception){
            Log.d(TAG, inBG+exception);
            return null;
        }

        Log.d(TAG, inBG + " returned string - "+result);
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        final ArrayList<String[]> sList = parseJSON(s);
        if(sList == null || sList.size()==0){
            AlertDialog.Builder builder = new AlertDialog.Builder(mainAct);
            builder.setTitle("Symbol Not Found");
            builder.setMessage("No data for stock symbol: "+ userInputSymbol);

            AlertDialog dialog = builder.create();
            dialog.show();
        } else if (sList.size()==1){
            mainAct.processNewStock(sList.get(0)[0]);
        } else {
            final CharSequence[] sArray = new CharSequence[sList.size()];
            for (int i = 0; i < sList.size(); i++)
                sArray[i] = sList.get(i)[0] + '\n' + sList.get(i)[1];

            AlertDialog.Builder builder = new AlertDialog.Builder(mainAct);
            builder.setTitle("Make a selection");
            builder.setItems(sArray, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String symbol = sList.get(which)[0];
                    mainAct.processNewStock(symbol);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Handles User Cancellation
                }
            });
            AlertDialog dialog = builder.create();

            dialog.show();
        }
    }

    private ArrayList<String[]> parseJSON(String s){
        ArrayList<String[]> results = new ArrayList<>();
        try{
            JSONArray jObjMain = new JSONArray(s);

            for(int i=0; i<jObjMain.length(); i++){
                JSONObject jStock = (JSONObject) jObjMain.get(i);
                String symbol = jStock.getString("symbol");
                String company = jStock.getString("name");
                String type = jStock.getString("type");

                if(!symbol.contains(".") && type.equals("S")){
                    String[] valid = {symbol,company};
                    results.add(valid);
                }
            }
            return results;

        } catch (Exception JSONParseException){
            Log.d(TAG, "parseJSON: "+JSONParseException.getMessage());
            JSONParseException.printStackTrace();
        }

        return null;
    }
}
