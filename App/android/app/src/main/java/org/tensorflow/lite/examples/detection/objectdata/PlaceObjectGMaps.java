package org.tensorflow.lite.examples.detection.objectdata;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlaceObjectGMaps {
    protected String place_id;
    protected String business_status;
    protected String formatted_address;
    protected Geometry geometry;
    protected String name;
    protected float rating;
    protected String vicinity;
    protected String[] type;

    protected PlaceObjectGMaps() {

    }

    public static PlaceObjectGMaps CreateNewFromJson(String json) {
        PlaceObjectGMaps streetObjectGMAPS = new PlaceObjectGMaps();
        try
        {
            JSONObject jsonRoot = new JSONObject(json);
            streetObjectGMAPS.name = jsonRoot.getString("name");
            streetObjectGMAPS.place_id = jsonRoot.getString("place_id");
            streetObjectGMAPS.business_status = jsonRoot.getString("business_status");
            streetObjectGMAPS.formatted_address = jsonRoot.getString("formatted_address");
            streetObjectGMAPS.rating = (float)jsonRoot.getDouble("rating");
            streetObjectGMAPS.formatted_address = jsonRoot.getString("formatted_address");
            streetObjectGMAPS.vicinity = jsonRoot.getString("vicinity");
            streetObjectGMAPS.type = jsonRoot.getJSONArray("type").toString().split(",");

            streetObjectGMAPS.geometry = Geometry.NewFromJson(jsonRoot.getString("geometry"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return streetObjectGMAPS;
    }

}
