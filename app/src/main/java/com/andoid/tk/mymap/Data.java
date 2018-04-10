package com.andoid.tk.mymap;

/**
 * Created by TK on 3/30/2018.
 */

public class Data
{
    private String order;
    private String placeName;
    private String km;
    private String duration;



    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Data(String order, String placeName, String km, String duration) {
        this.order = order;
        this.placeName = placeName;
        this.km = km;
        this.duration = duration;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getDuration() {
    return duration;
}
}
