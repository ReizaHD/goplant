package com.example.goplant.object;

import java.io.Serializable;

public class History implements Serializable {
    private String soil, humidity, temp,date;

    public History(String soil, String humidity, String temp, String date) {
        this.soil = soil;
        this.humidity = humidity;
        this.temp = temp;
        this.date = date;
    }

    public String getSoil() {
        return soil;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getTemp() {
        return temp;
    }

    public String getDate() {
        return date;
    }
}
