package com.lumivote.lollipop.ui;

/**
 * Created by alex on 9/19/15.
 */
public class Illness {

    String date;
    String name;
    String photoPath;

    public Illness(String date, String name, String photoPath){
        this.date = date;
        this.name = name;
        this.photoPath = photoPath;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getPhotoPath(){
        return photoPath;
    }

}
