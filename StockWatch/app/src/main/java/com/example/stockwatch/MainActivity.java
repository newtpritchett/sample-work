package com.example.stockwatch;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import java.util.ArrayList;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//written by Trevor Pritchett A20392360

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener, View.OnClickListener{

    private List<Stock> stockList;
    private SwipeRefreshLayout swipeLayout;
    private RecyclerView stockListRecycler;
    private StockAdapter stockAdapter;
    private DatabaseHandler databaseHandler;

    //Message Strings and URLs
    private String connectionErrorTitle = "No Network Connection";
    private String connectionErrorMsg = "Stocks cannot be added without a network connection.";
    private String symbolPrompt = "Enter a Stock Symbol";
    private String moreInfoURL = "http://www.marketwatch.com/investing/stock/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stockList = new ArrayList<>();
        stockAdapter = new StockAdapter(stockList, this);
        databaseHandler = new DatabaseHandler(this);

        stockListRecycler = findViewById(R.id.stocksRecyclerView);
        stockListRecycler.setAdapter(stockAdapter);
        stockListRecycler.setLayoutManager(new LinearLayoutManager(this));

        swipeLayout = findViewById(R.id.swipeRefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tryRefresh();
            }
        });

        if(checkConnection()==true){
            refreshStockList();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(connectionErrorTitle);
            builder.setMessage(connectionErrorMsg);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.Add:
                if(checkConnection()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    final MainActivity mainActivity = this;
                    final EditText userSymbolInput = new EditText(this);
                    userSymbolInput.setInputType(InputType.TYPE_CLASS_TEXT);
                    userSymbolInput.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
                    userSymbolInput.setGravity(Gravity.CENTER_HORIZONTAL);

                    builder.setView(userSymbolInput);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new NameDownloader(mainActivity).execute(userSymbolInput.getText().toString());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            //handles user cancellation
                        }
                    });
                    builder.setTitle("New Stock");
                    builder.setMessage(symbolPrompt);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(connectionErrorTitle);
                    builder.setMessage(connectionErrorMsg);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        String address = moreInfoURL;
        int pos = stockListRecycler.getChildLayoutPosition(view);
        String symbol = stockList.get(pos).getSymbol();
        String url = address + symbol;

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }

    @Override
    public boolean onLongClick(View view) {
        final int index = stockListRecycler.getChildLayoutPosition(view);
        final Stock stock = stockList.get(index);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                databaseHandler.deleteStock(stock.getSymbol());
                stockList.remove(index);
                stockAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setMessage("Delete Stock "+stock.getSymbol()+"("+stock.getName()+")?");
        builder.setTitle("Delete Stock");
        AlertDialog dialog = builder.create();
        dialog.show();

        return false;
    }


    private boolean checkConnection(){
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conManager.getActiveNetworkInfo();

        return (info!= null && info.isConnected());
    }

    private void tryRefresh() {
        if(checkConnection()){
            refreshStockList();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(connectionErrorTitle);
            builder.setMessage(connectionErrorMsg);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        swipeLayout.setRefreshing(false);
    }



    public void addNewStock(Stock stock) {
        if(stock != null){
            stockList.add(stock);
            Collections.sort(stockList, new Comparator<Stock>() {
                public int compare(Stock stockX, Stock stockY) {
                    return stockX.getSymbol().compareTo(stockY.getSymbol());
                }
            });
            databaseHandler.addStock(stock);
            stockAdapter.notifyDataSetChanged();
        }
    }

    private void refreshStockList(){
        ArrayList<String[]> temp = databaseHandler.loadStocks();
        stockList.clear();
        for(int i=0; i<temp.size();i++){
            new StockDownloader(this).execute(temp.get(i)[0]);
        }
    }

    public void processNewStock(String symbol){
        boolean duplicate = false;
        for(int i=0; i<stockList.size();i++){
            if(stockList.get(i).getSymbol().equals(symbol)){
                duplicate = true;
            }
        }

        if(duplicate){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Duplicate Stock");
            builder.setMessage("Stock symbol "+symbol+" is already displayed.");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            new StockDownloader(this).execute(symbol);
        }
    }

    @Override
    protected void onDestroy() {
        databaseHandler.shutDown();
        super.onDestroy();
    }
}
