package org.tensorflow.lite.examples.detection.objectdata;


import org.json.JSONException;
import org.json.JSONObject;

public class Geometry {
    Location location;
    ViewPort viewPort;

    public Location getLocation() {
        return location;
    }

    public ViewPort getViewPort() {
        return viewPort;
    }

    protected Geometry() {

    }

    public static Geometry NewFromJson(String json) {

        Geometry geometry = new Geometry();
        try
        {
            JSONObject jsonRoot = new JSONObject(json);
            geometry.location = Location.NewFromJson(jsonRoot.getString("location"));
            geometry.viewPort = ViewPort.NewFromJson(jsonRoot.getString("viewPort"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return geometry;
    }
}