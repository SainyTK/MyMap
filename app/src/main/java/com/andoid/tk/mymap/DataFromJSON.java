package com.andoid.tk.mymap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by TK on 4/3/2018.
 */

public class DataFromJSON
{
    private List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
    private int duration;
    private int distance;

    public DataFromJSON()
    {

    }

    public List<List<HashMap<String, String>>> getRoutes() {
        return routes;
    }

    public void setRoutes(List<List<HashMap<String, String>>> routes) {
        this.routes = routes;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}

