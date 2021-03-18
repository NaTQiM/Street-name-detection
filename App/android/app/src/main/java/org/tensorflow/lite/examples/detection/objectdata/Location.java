package org.tensorflow.lite.examples.detection.objectdata;

import org.json.JSONException;
import org.json.JSONObject;

public class Location {
    String lat = "0";
    String lng = "0";

    protected Location() {

    }

    public String _lat() {
        return lat;
    }

    public String _lng() {
        return lng;
    }


    protected Location(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public static Location NewFromJson(String json) {
        String _lat = "0", _lng = "0";
        try {
            JSONObject jsonRoot = new JSONObject(json);
            _lat = jsonRoot.getString("lat");
            _lng = jsonRoot.getString("lng");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new Location();
    }
}