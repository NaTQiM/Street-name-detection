package org.tensorflow.lite.examples.detection.objectdata;

import org.json.JSONException;
import org.json.JSONObject;

public class Location {
    public final String lat;
    public final String lng;

    protected Location(String lat, String lng) {
        this.lng = lng;
        this.lat = lat;
    }

    protected Location() {
        this.lng = "0.0";
        this.lat = "0.0";
    }

    public static Location NewFromJson(String json) {
        String _lat = "0.0", _lng = "0.0";
        try {
            JSONObject jsonRoot = new JSONObject(json);
            _lat = jsonRoot.getString("lat");
            _lng = jsonRoot.getString("lng");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new Location(_lat, _lng);
    }
}