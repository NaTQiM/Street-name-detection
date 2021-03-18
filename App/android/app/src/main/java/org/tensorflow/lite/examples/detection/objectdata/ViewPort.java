package org.tensorflow.lite.examples.detection.objectdata;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewPort {
    Location northeast;
    Location southwest;

    protected ViewPort() {

    }

    protected ViewPort(@Nullable Location northeast, @Nullable Location southwest) {
        this.northeast = northeast == null ? new Location() : northeast;
        this.southwest = southwest == null ? new Location() : northeast;
    }

    public static ViewPort NewFromJson(String json) {
        Location _n = null, _s = null;
        try {
            JSONObject jsonRoot = new JSONObject(json);
            _n = Location.NewFromJson(jsonRoot.getString("northeast"));
            _s = Location.NewFromJson(jsonRoot.getString("southwest"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return new ViewPort(_n, _s);
    }
}