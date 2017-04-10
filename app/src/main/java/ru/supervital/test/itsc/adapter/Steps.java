package ru.supervital.test.itsc.adapter;


import java.util.Date;

/**
 * Created by Vitaly Oantsa on 10.04.2017.
 */

public class Steps {
    String date;
    Integer Count;

    public Steps(String date, Integer count) {
        this.date = date;
        Count = count;
    }

    public String getDate() {
        return date;
    }

    public Integer getCount() {
        return Count;
    }
}
