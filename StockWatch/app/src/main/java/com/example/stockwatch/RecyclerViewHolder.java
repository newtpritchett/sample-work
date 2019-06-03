package com.example.stockwatch;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    public TextView Symbol;
    public TextView Name;
    public TextView Price;
    public TextView PercentChange;
    public TextView Change;

    public RecyclerViewHolder(View view){
        super(view);
        Symbol = view.findViewById(R.id.symbol);
        Name = view.findViewById(R.id.name);
        Price = view.findViewById(R.id.price);
        PercentChange = view.findViewById(R.id.percent_change);
        Change = view.findViewById(R.id.change);
    }

}
