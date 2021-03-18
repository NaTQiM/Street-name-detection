package org.tensorflow.lite.examples.detection.objectdata;

import org.json.JSONException;
import org.json.JSONObject;

public class StreetObjectGMaps {
    protected String formatted_address;
    protected Geometry geometry;
    protected String name;

    protected StreetObjectGMaps() {

    }

    public static StreetObjectGMaps CreateNewFromJson(String json) {
        StreetObjectGMaps streetObjectGMAPS = new StreetObjectGMaps();
        try
        {
            JSONObject jsonRoot = new JSONObject(json);
            streetObjectGMAPS.name = jsonRoot.getString("name");
            streetObjectGMAPS.geometry = Geometry.NewFromJson(jsonRoot.getString("geometry"));
            streetObjectGMAPS.formatted_address = jsonRoot.getString("formatted_address");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return streetObjectGMAPS;
    }
}
