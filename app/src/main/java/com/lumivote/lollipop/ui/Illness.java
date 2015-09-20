package com.lumivote.lollipop.ui;

/**
 * Created by alex on 9/19/15.
 */
public class Illness {

    String date;
    String name;

    public Illness(String date, String name){
        this.date = date;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

}
