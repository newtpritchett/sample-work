package com.example.stockwatch;
public class Stock{

    private String name;
    private String symbol;
    private double latestPrice;
    private double change;
    private double changePercent;

    public Stock(String name, String symbol, double price, double change, double changePercent){
        this.name = name;
        this.symbol = symbol;
        this.latestPrice = price;
        this.change = change;
        this.changePercent = changePercent;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getLatestPrice() {
        return latestPrice;
    }

    public double getChange() {
        return change;
    }

    public double getChangePercent() {
        return changePercent;
    }

}