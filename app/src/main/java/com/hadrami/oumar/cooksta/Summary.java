package com.hadrami.oumar.cooksta;

/**
 * Created by oumar on 19/12/2016.
 */

public class Summary {
    String name,qty,price,time;

    public Summary(String name , String price, String qty , String time) {
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.time = time;
    }





    public String getNameee() {
        return name;
    }

    public String getPriceee() {
        return price;
    }

    public String getQtyyy() {
        return qty;
    }

    public String getTimeee() {
        return time;
    }
}
