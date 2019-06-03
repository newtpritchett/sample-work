package com.example.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockDownloader extends AsyncTask<String, Void, String> {
    //Strings and URLs
    private static final String TAG = "StockDownloader";
    private final String URLStub = "https://api.iextrading.com/1.0/stock/";
    private final String noDataTitle = "Data Search Error";
    private final String noDataMsg = "No Stock Data Found";
    private final String inBG = "doInBackground: ";

    private MainActivity mainAct;
    public StockDownloader(MainActivity ma) {
        mainAct = ma;
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG, inBG + "Finding stock data for "+strings[0]);
        String address = Uri.parse(URLStub + strings[0] + "/quote").toString();
        StringBuilder stringBuilder = new StringBuilder();

        try {
            URL url = new URL(address);

            Log.d(TAG, inBG + "connecting to url "+url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream stream = conn.getInputStream();

            Log.d(TAG, inBG + "getInputStream successful");
            BufferedReader reader = new BufferedReader((new InputStreamReader(stream)));

            Log.d(TAG, inBG + "reading downloaded content to string");
            stringBuilder.append(reader.readLine());

        } catch (Exception stringBuilderException) {
            Log.d(TAG, inBG +stringBuilderException.toString());
            return null;
        }
        Log.d(TAG, inBG + "JSON array: "+stringBuilder.toString());
        return "["+stringBuilder.toString()+"]";
    }

    private Stock parseJSON(String json){
        Stock stockX = null;
        if(json==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(mainAct);
            builder.setTitle(noDataTitle);
            builder.setMessage(noDataMsg);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            try {
                JSONArray jObjMain = new JSONArray(json);
                if (jObjMain.length() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mainAct);
                    builder.setTitle(noDataTitle);
                    builder.setMessage(noDataMsg);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    for (int i = 0; i < jObjMain.length(); i++) {
                        JSONObject jsonStock = (JSONObject) jObjMain.get(i);
                        String symbol = jsonStock.getString("symbol");
                        String name = jsonStock.getString("companyName");
                        double price = jsonStock.getDouble("latestPrice");
                        double change = jsonStock.getDouble("change");
                        double changePercent = jsonStock.getDouble("changePercent")*100;

                        stockX = new Stock(name, symbol, price, change, changePercent);
                    }
                }
                return stockX;
            } catch (Exception exception) {
                Log.d(TAG, "parseJSON: " + exception.getMessage());
            }
        }
        Log.d(TAG, "parseJSON: " + noDataMsg);
        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        Stock stockX = parseJSON(string);
        mainAct.addNewStock(stockX);
    }

}
